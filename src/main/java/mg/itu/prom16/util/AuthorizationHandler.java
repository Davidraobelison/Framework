package mg.itu.prom16.util;

import java.lang.reflect.Method;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.annotation.authorization.AuthorizedRoles;
import mg.itu.prom16.annotation.authorization.RequireLogin;
import mg.itu.prom16.exception.UnauthorizedException;
import mg.itu.prom16.security.AuthManager;

public class AuthorizationHandler {

    public static void isAuthorizedGeneric(Class<?> clazz, HttpServletRequest request, ServletConfig context) throws Exception {
        AuthManager authManager = new AuthManager(context);
        HttpSession session = request.getSession();

        if (session == null) {
            throw new UnauthorizedException("Access denied, Session not exist!!");
        }

        // Vérifier si RequireLogin et AuthorizedRoles sont présentes en même temps
        boolean hasRequireLogin = clazz.isAnnotationPresent(RequireLogin.class);
        boolean hasAuthorizedRoles = clazz.isAnnotationPresent(AuthorizedRoles.class);

        if (hasRequireLogin && hasAuthorizedRoles) {
            throw new IllegalArgumentException("Les annotations @RequireLogin et @AuthorizedRoles ne peuvent pas être présentes en même temps dans " + clazz.getSimpleName());
        }

        // Vérifier RequireLogin
        if (hasRequireLogin && session != null) {
            if (!authManager.isAuthenticated(session)) {
                throw new UnauthorizedException("Access denied, Login required");
            }
        }
        // Vérifier AuthorizedRoles
        else if (hasAuthorizedRoles && session != null) {
            if (!authManager.isAuthenticated(session)) {
                throw new UnauthorizedException("Access denied, Login required !");
            }

            // Récupérer les rôles depuis l'annotation sur la classe
            String[] roles = clazz.getAnnotation(AuthorizedRoles.class).roles();

            if (!authManager.hasRoles(session, roles)) {
                throw new IllegalArgumentException("Access denied, ROLES required !!");
            }
        }
    }


    public static void isAuthorized(Method method, HttpServletRequest request, ServletConfig context) throws Exception {
        AuthManager authManager = new AuthManager(context);
        
        HttpSession session1 = request.getSession();
        if (session1 == null) {
            throw new UnauthorizedException("Access denied, Login required !!");
        }
        if (method.isAnnotationPresent(RequireLogin.class) 
            && method.isAnnotationPresent(AuthorizedRoles.class)) {
            throw new IllegalArgumentException("Les deux annotations ne peuvent pas presentes en meme dans la methode "+ method.getName());
        }
        if (method.isAnnotationPresent(RequireLogin.class)) {
            if (!authManager.isAuthenticated(session1)) {
                throw new UnauthorizedException("Access denied, Login required");
            }
        } else if (method.isAnnotationPresent(AuthorizedRoles.class)) {
            if (!authManager.isAuthenticated(session1)) {
                throw new UnauthorizedException("Access denied, Login required !");
            }
            String[] roles = method.getAnnotation(AuthorizedRoles.class).roles();
            if (!authManager.hasRoles(session1, roles)) {
                throw new IllegalArgumentException("Access denied, ROLES required !!");
            }
        }
    }

    
}
