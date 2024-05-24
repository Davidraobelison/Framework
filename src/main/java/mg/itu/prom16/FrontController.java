package mg.itu.prom16;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.util.ClassScanner;
import mg.itu.prom16.util.Mapping;


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
            out.println("<li> Class name :  "+ value.getClassName() +" </li> <li> Method name : "+ value.getMethodName() +"</li>");
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
                        Mapping mapping = new Mapping(class1.getSimpleName(), method.getName());
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
        
        // Récupérer l'URL tapée par l'utilisateur
        StringBuffer url = request.getRequestURL();

        // Récupérer l'URL apres le port et le host
        // Récupérer le contexte (nom) de l'application
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Retirer le contexte de l'application si nécessaire
        String relativeURI = requestURI.substring(contextPath.length());

        // Afficher l'URL dans la console ou dans la réponse HTTP
        System.out.println("\n URL tapée par l'utilisateur : " + url.toString());
        System.out.println(" Partie de l'URL après le nom d'hôte et le port : " + relativeURI);

        try {
            boolean isPresent = listMapping.containsKey(relativeURI);
            if (!isPresent) {
                // 404 not found 
               display404NotFound(out, url.toString());
               return;           
            }

            Mapping mapping =  listMapping.get(relativeURI);
            print(response);
            out.println("<ul><h2> URL : " + relativeURI + "</h2>");
            out.println("<li> Controller class name :  "+ mapping.getClassName() +" </li> <li> Method name : "+ mapping.getMethodName() +"</li></ul>");
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
