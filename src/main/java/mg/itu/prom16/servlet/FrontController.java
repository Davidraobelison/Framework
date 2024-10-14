package mg.itu.prom16.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
                        
                        HashMap<String, Method> apiRequests = new HashMap<String, Method>();
                        apiRequests.put(verb, method);
                        Mapping mapping = new Mapping(class1, apiRequests);

                        this.listMapping.put(valueAnnotation, mapping);
                    }
                    else {
                        Mapping map = listMapping.get(valueAnnotation);
                        HashMap<String, Method> apiRequests2 = map.getApiRequests();

                        if (!apiRequests2.containsKey(verb)) {
                            apiRequests2.put(verb, method);
                            this.listMapping.put(valueAnnotation, map);
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

    protected Method getMethodByVerb(HttpServletRequest request, Mapping mapping) throws Exception {
        String verb = request.getMethod();
        HashMap<String, Method> liHashMap = mapping.getApiRequests();
        Method method = liHashMap.get(verb);
        if (method != null) {
            isValidVerb(request, method);
            return method;
        }

        StringBuilder keysString = new StringBuilder();
        for (String key : liHashMap.keySet()) {
            keysString.append(key).append(", ");
        }
        if (keysString.length() > 0) {
            keysString.setLength(keysString.length() - 2); // Retirer les deux derniers caractères (", ")
        }

        throw new Exception("HTTP method mismatch: expected " + keysString.toString() + " but received "+ verb);
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
            
            Mapping mapping =  listMapping.get(relativeURI);
            Method method = getMethodByVerb(request, mapping);

            Object instance = mapping.getClass1().getDeclaredConstructor().newInstance();
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
