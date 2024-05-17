package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import annotation.Controller;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.util.ClassScanner;

public class FrontController extends HttpServlet {

    private boolean isScanned; 
    private List<Class<?>> classes;
    private String basePackage ;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        isScanned = false;
        classes = new ArrayList<Class<?>>();
        // Obtenez la valeur du paramètre contextConfigLocation
        // Obtenez la valeur du package
        basePackage = config.getInitParameter("basePackageName");
    }

    protected void print(HttpServletResponse response) throws IOException{
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p>Hello ! </p>");
        out.println("</body></html>");
    }

    protected void initVariable() throws Exception {
        try {
            classes = ClassScanner.scanClasses(basePackage, Controller.class);
            isScanned = true;
        } catch (Exception e) {
            isScanned = false;
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                print(response);
                
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
            if (!isScanned) {
                initVariable();
            }

            out.println("Les Controllers disponibles : ");

            out.println("<ul>");
            for (Class<?> class1 : classes) {
                out.println("<li>" + class1.getSimpleName() + "</li>");
            }
            out.println("</ul>");
        } 
        
        catch (Exception e) {
            out.println(e.getMessage());
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
