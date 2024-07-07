package mg.itu.prom16.util;

import jakarta.servlet.http.HttpSession;

public class MySession {
    protected HttpSession session;

    public MySession() {
    }

    // Methode des sessions
    public void add(String key, Object value) {
        session.setAttribute(key, value);
    }

    public void remove(String key) {
        session.removeAttribute(key);
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    // getters an setters
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }
}
