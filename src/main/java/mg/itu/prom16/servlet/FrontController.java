package mg.itu.prom16.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.util.ClassScanner;
import mg.itu.prom16.util.Mapping;
import mg.itu.prom16.util.ModelView;


public class FrontController extends HttpServlet {
    
    private String basePackage ;
    HashMap<String , Mapping> listMapping;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Obtenez la valeur du package
        basePackage = config.getInitParameter("basePackageName");
        initHashMap();
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

    protected void display404NotFound(PrintWriter out, String requette) {
        out.println("<style>body{font-family:Arial,sans-serif;text-align:center;}h1{color: #333;}img {max-width: 100%;height:auto; margin-top:20px; }</style>");
        out.println("<h1> 404 - Page not found </h1><p>La requette "+ requette +"</p>");
        out.println("<p>Sorry, the page you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p>");
    }

    protected void initHashMap() throws ServletException {
        try 
        {
            List<Class<?>> classes = ClassScanner.scanClasses(basePackage, Controller.class);
            listMapping = new HashMap<String, Mapping>();

            for (Class<?> class1 : classes) {
                Method[] methods = class1.getDeclaredMethods();
            
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Get.class)) {
                        String valueAnnotation = method.getAnnotation(Get.class).value();
                        Mapping mapping = new Mapping(class1, method);
                        this.listMapping.put(valueAnnotation, mapping);
                    }
                }
            }
        } 
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        String relativeURI = request.getServletPath();

        try {
            boolean isPresent = listMapping.containsKey(relativeURI);
            
            if (!isPresent) {
                response.sendError(404);
                return;           
            }

            // Mapping correspondant a la requette tapee qui a  (Method, Class)
            Mapping mapping =  listMapping.get(relativeURI);

            // invoke methode 
            Object instance = mapping.getClass1().getDeclaredConstructor().newInstance();
            Object valueFunction = mapping.getMethod().invoke(instance);

            if (valueFunction instanceof ModelView) {

                ModelView modelAndView = (ModelView)valueFunction;

                String nameView = modelAndView.getViewName();
                HashMap<String, Object> listKeyAndValue = modelAndView.getData();

                for (Map.Entry<String, Object> map : listKeyAndValue.entrySet()) {
                    request.setAttribute(map.getKey(),  map.getValue());
                }

                RequestDispatcher dispatcher = request.getRequestDispatcher(nameView);
                dispatcher.forward(request, response);
            }
            else{
                print(response);
                out.println("<ul><h2> URL : " + relativeURI + "</h2>");
                out.println("<li> Controller class name :  "+ mapping.getClass1().getName() +" </li><li> Method name : "+ mapping.getMethod().getName() +"</li></ul>");
                out.println("<ul><li> Valeur de la fonction :  "+ valueFunction.toString() + "</li></ul>");

            }

        } 
        catch (Exception e) {
            e.printStackTrace();
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
