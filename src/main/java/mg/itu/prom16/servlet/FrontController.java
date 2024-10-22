package mg.itu.prom16.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.GET;
import mg.itu.prom16.annotation.POST;
import mg.itu.prom16.annotation.RestApi;
import mg.itu.prom16.annotation.URL;
import mg.itu.prom16.exception.DuplicateUrlException;
import mg.itu.prom16.exception.InvalidReturnTypeException;
import mg.itu.prom16.exception.PackageNotFoundException;
import mg.itu.prom16.util.ApiRequest;
import mg.itu.prom16.util.ClassScanner;
import mg.itu.prom16.util.JsonParserUtil;
import mg.itu.prom16.util.Mapping;
import mg.itu.prom16.util.ServletUtil;
import mg.itu.prom16.util.ModelView;


@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 50 // 50 MB
)
public class FrontController extends HttpServlet {
    private String basePackage ;
    private HashMap<String , Mapping> listMapping;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Obtenez la valeur du package
        basePackage = config.getInitParameter("basePackageName");
        try {
            initHashMap();
        } 
        catch (PackageNotFoundException | DuplicateUrlException e) {
            e.printStackTrace();
            throw new Error(e.getMessage());
        }
        catch (Exception ex){
            throw new ServletException(ex);
        }
    }

    protected void doRestApi(Object valueFunction, HttpServletResponse response) throws Exception {
        try {
            if (valueFunction instanceof ModelView) {
                ModelView modelView = (ModelView) valueFunction;
                HashMap<String, Object> listKeyAndValue = modelView.getData();
                String dataString = JsonParserUtil.objectToJson(listKeyAndValue);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
            else {
                String dataString = JsonParserUtil.objectToJson(valueFunction);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void dispatcher (HttpServletRequest request , HttpServletResponse response,  Object valueFunction) throws InvalidReturnTypeException, Exception{
        try{
            PrintWriter out = response.getWriter();
            if (valueFunction instanceof ModelView) {

                ModelView modelAndView = (ModelView)valueFunction;

                String nameView = modelAndView.getViewName();
                HashMap<String, Object> listKeyAndValue = modelAndView.getData();
                
                for (Map.Entry<String, Object> map : listKeyAndValue.entrySet()) {
                    request.setAttribute(map.getKey(),  map.getValue());
                }

                String queryString = request.getQueryString();
                RequestDispatcher dispatcher = request.getRequestDispatcher(nameView +"?" + queryString);
                dispatcher.forward(request, response);
            }
            else if (valueFunction instanceof String) { // si string
                out.println("<ul><li> Valeur de la fonction :  "+ valueFunction.toString() + "</li></ul>");
            }
            else {
                throw new InvalidReturnTypeException(valueFunction.toString());
            }
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }


    void showListMap () {
        System.out.println("\n Affichage ");
        for (Map.Entry<String, Mapping> entry : listMapping.entrySet()) {
            String keyUrl = entry.getKey();
            Mapping mapping = entry.getValue();

            System.out.println("URL =" + keyUrl);
            System.out.println(mapping.toString());
        }
        System.out.println("end \n");

    }

    protected void initHashMap() throws DuplicateUrlException, PackageNotFoundException, Exception {
        List<Class<?>> classes = ClassScanner.scanClasses(basePackage, Controller.class);
        listMapping = new HashMap<String, Mapping>();

        for (Class<?> class1 : classes) {
            Method[] methods = class1.getDeclaredMethods();
        
            for (Method method : methods) {
                if (method.isAnnotationPresent(URL.class)) {
                    String valueAnnotation = method.getAnnotation(URL.class).value();
                    
                    String verb = getMethod(method);
                    if (!listMapping.containsKey(valueAnnotation)) {

                        ApiRequest api = new ApiRequest(class1, method);
                        Mapping map = new Mapping();
                        map.addRequest(verb, api);

                        this.listMapping.put(valueAnnotation, map);
                    }
                    else {
                        Mapping map = listMapping.get(valueAnnotation);

                        if (!map.containsKey(verb)) {
                            map.addRequest(verb, new ApiRequest(class1, method));
                        }
                        else {
                            throw new DuplicateUrlException(valueAnnotation, verb);
                        }
                        
                    }
                }
            }
        }
    }

    protected String getMethod(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return "GET";
        }      
        else if (method.getAnnotation(POST.class) != null) {
            return "POST";
        }  
        return "GET"; //par defaut si il y a pas de verb
    }

    protected void isValidVerb(HttpServletRequest request, Method method) throws Exception {
        String verbRequest = request.getMethod();
        String verbMethod = getMethod(method);

        System.out.println("REQUEST : "+ verbRequest);
        System.out.println("Method : "+ verbMethod);

        if(!verbMethod.equals(verbRequest)) {
            throw new Exception("HTTP method mismatch: expected " + verbMethod + ", but received " + verbRequest);
        }
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        String relativeURI = request.getServletPath();
        String queryString = request.getQueryString();
        
        System.out.println(relativeURI);
        System.out.println(queryString);


        try {
            boolean isPresent = listMapping.containsKey(relativeURI);
            if (!isPresent) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;           
            }
            
            Mapping mapping =  this.listMapping.get(relativeURI);
            mapping.isValidVerb(request);

            ApiRequest apiRequest = mapping.getRequest(request.getMethod());
            Method method = apiRequest.getMethod();

            Object instance = apiRequest.getClass1().getDeclaredConstructor().newInstance();
            List<Object> listArgs = ServletUtil.parseParameters(request, method);

            ServletUtil.putSession(request,  instance);
            Object valueFunction = method.invoke(instance, listArgs.toArray());
            
            RestApi restApi = method.getAnnotation(RestApi.class);
            if (restApi != null) {
                response.setContentType("text/json");
                doRestApi(valueFunction, response);
            } else {
                dispatcher(request, response, valueFunction);
            }
        } 
        catch (Exception e) {   
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");    
            response.setStatus(500);
            PrintWriter out = response.getWriter();
            out.println("<p>" + e.getMessage() + "</p>");
            out.close();  
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
