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
import java.util.HashMap;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.annotation.Post;
import mg.itu.prom16.exception.DuplicateUrlException;
import mg.itu.prom16.exception.InvalidReturnTypeException;
import mg.itu.prom16.exception.PackageNotFoundException;
import mg.itu.prom16.util.ClassScanner;
import mg.itu.prom16.util.Mapping;
import mg.itu.prom16.util.MethodParameterParser;
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
            // throw new ServletException("Build failed : " , e);
        }
        catch (Exception ex){
            throw new ServletException(ex);
        }
    }

    protected void print(HttpServletResponse response) throws IOException{
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p>Hello ! </p>");
        out.println("</body></html>");
    }

    protected void displayListMapping(PrintWriter out) {
        for (Map.Entry<String, Mapping> e : listMapping.entrySet()) {
            String key = e.getKey();
            Mapping value = e.getValue();

            out.println("<ul> URL : " + key + "</ul>");
            out.println("<li> Class name :  "+ value.getClass1().getSimpleName() +" </li> <li> Method name : "+ value.getMethod().getName() +"</li>");
        }
    }

    protected void dispatcher (HttpServletRequest request , HttpServletResponse response,  Object valueFunction) throws InvalidReturnTypeException, Exception{
        try{
            PrintWriter out = response.getWriter();
            if (valueFunction instanceof ModelView) {

                ModelView modelAndView = (ModelView)valueFunction;

                String nameView = modelAndView.getViewName();
                out.print(nameView);
                HashMap<String, Object> listKeyAndValue = modelAndView.getData();

                for (Map.Entry<String, Object> map : listKeyAndValue.entrySet()) {
                    request.setAttribute(map.getKey(),  map.getValue());
                }

                RequestDispatcher dispatcher = request.getRequestDispatcher(nameView);
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
                if (method.isAnnotationPresent(Get.class)) {
                    String valueAnnotation = method.getAnnotation(Get.class).value();
                    Mapping mapping = new Mapping(class1, method);
                    
                    if (!listMapping.containsKey(valueAnnotation)) {
                        this.listMapping.put(valueAnnotation, mapping);
                    }
                    else {
                        throw new DuplicateUrlException(valueAnnotation);
                    }
                }
            }
        }
        
    }

    protected String getMethod(Method method) {
        if (method.getAnnotation(Get.class) != null) {
            return "GET";
        }      
        else if (method.getAnnotation(Post.class) != null) {
            return "POST";
        }  
        return "OTHER";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 

        String relativeURI = request.getServletPath();
        try {
            boolean isPresent = listMapping.containsKey(relativeURI);
            
            if (!isPresent) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;           
            }

            Mapping mapping =  listMapping.get(relativeURI);

            System.out.println();
            
            Object instance = mapping.getClass1().getDeclaredConstructor().newInstance();
            List<Object> listArgs = MethodParameterParser.parseParameters(request, mapping.getMethod());

            
            Object valueFunction = mapping.getMethod().invoke(instance, listArgs.toArray());
            dispatcher(request, response, valueFunction);
            
        } 
        catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);            
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                System.out.println(request.getServletPath());
        processRequest(request, response);
    }

}
