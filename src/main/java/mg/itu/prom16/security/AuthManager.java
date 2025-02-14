package mg.itu.prom16.security;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpSession;

public class AuthManager {

    private final ServletConfig context;

    public AuthManager(ServletConfig context) {
        this.context = context;
    }

    public boolean isAuthenticated(HttpSession session) {
        String authAttribute = context.getInitParameter("authSessionAttribute");

        Boolean isAuthenticated = (Boolean) session.getAttribute(authAttribute);
        return Boolean.TRUE.equals(isAuthenticated);
    }

    public boolean hasRoles(HttpSession session, String[] requiredRoles) {
        String roleAttribute = context.getInitParameter("roleSessionAttribute");
        String userRole = (String) session.getAttribute(roleAttribute);
        if (userRole == null) {
            return false;
        }
        userRole = normalizeRole(userRole);

        for (String roleRequired : requiredRoles) {
            if (userRole.equals(roleRequired)) {
                return true;
            }
        }
        return false;
    }

    public static String normalizeRole(String role) {
        return role != null ? role.toUpperCase() : null;
    }


}