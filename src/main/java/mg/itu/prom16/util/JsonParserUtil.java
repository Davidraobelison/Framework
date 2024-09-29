package mg.itu.prom16.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonParserUtil {

    // Instance de Gson
    private static final Gson gson = new Gson();

    // Méthode pour convertir un objet Java en JSON
    public static String objectToJson(Object object) {
        return gson.toJson(object);
    }

    // Méthode pour convertir une chaîne JSON en un objet Java de type spécifié
    public static <T> T jsonToObject(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            System.out.println("Erreur lors de la conversion du JSON en objet : " + e.getMessage());
            return null;  // Ou gérer l'erreur d'une autre manière
        }
    }

}

