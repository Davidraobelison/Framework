package itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet; // Importation de l'annotation WebServlet


public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p>Hello from MyServlet!</p>");
        out.println("</body></html>");
        
        // Récupérer l'URL tapée par l'utilisateur
        StringBuffer url = request.getRequestURL();
        
        // Vous pouvez également récupérer la chaîne de requête (query string) si nécessaire
        String queryString = request.getQueryString();
        
        // Afficher l'URL dans la console ou dans la réponse HTTP
        System.out.println("URL tapée par l'utilisateur : " + url.toString());
        System.out.println("Chaîne de requête : " + queryString);
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
