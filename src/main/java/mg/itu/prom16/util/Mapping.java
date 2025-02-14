package mg.itu.prom16.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public class Mapping {
    HashMap<String, ApiRequest> apiRequests;

    public void addRequest(String httpMethod, ApiRequest apiRequest) {
        this.apiRequests.put(httpMethod, apiRequest);
    }

    public ApiRequest getRequest(String httpMethod) {
        return this.apiRequests.get(httpMethod);
    }


    public void isValidVerb(HttpServletRequest request) throws Exception {
        String httpVerb = request.getMethod();
        if (!this.apiRequests.containsKey(httpVerb)) {
            StringBuilder keysString = new StringBuilder();
            for (String key : this.apiRequests.keySet()) {
                keysString.append(key).append(", ");
            }
            if (keysString.length() > 0) {
                keysString.setLength(keysString.length() - 2); // Retirer les deux derniers caractères (", ")
            }
            throw new Exception("HTTP method mismatch: expected " + keysString.toString() + " but received "+ httpVerb);
        }
    }

    public boolean containsKey(String httpMethod) {
        return this.apiRequests.containsKey(httpMethod);
    }

    public Mapping(HashMap<String, ApiRequest> apiRequests) {
        this.apiRequests = apiRequests;
    }

    public Mapping() {
        this.apiRequests = new HashMap<>();
    }


    public HashMap<String, ApiRequest> getApiRequests() {
        return apiRequests;
    }

    public void setApiRequests(HashMap<String, ApiRequest> apiRequests) {
        this.apiRequests = apiRequests;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Ajouter le nom de la classe

        // Ajouter les méthodes du HashMap
        sb.append("API Requests:\n");
        for (Map.Entry<String, ApiRequest> entry : apiRequests.entrySet()) {
            String key = entry.getKey();
            ApiRequest method = entry.getValue();
            sb.append("Class: ").append(method.getClass1().getName()).append("\n");

            sb.append("  - ").append(key)
              .append(": ")
              .append(method.getMethod().getName())
              .append(" (")
              .append(method.getMethod().getReturnType().getSimpleName())
              .append(")\n");
        }

        return sb.toString();
    }
}
