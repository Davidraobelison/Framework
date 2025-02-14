package mg.itu.prom16.util;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe dédiée à la résolution d'URL en fonction d'un mapping défini avec des placeholders.
 */
public class UrlMappingResolver {

    // La map qui associe les patterns d'URL (avec placeholders) aux mappings (ex : méthodes du contrôleur)
    private Map<String, Mapping> mappingRegistry;

    public UrlMappingResolver(Map<String, Mapping> mappingRegistry) {
        this.mappingRegistry = mappingRegistry;
    }

    /**
     * Tente de résoudre l'URI de la requête et retourne le mapping correspondant ainsi que les variables extraites.
     * 
     * @param requestUri L'URI de la requête, par exemple "/api/testPathVariable/5"
     * @return Un MappingResult contenant le mapping et les path variables, ou null si aucun mapping ne correspond.
     */
    public MappingResult resolve(String requestUri) {
        for (String pattern : mappingRegistry.keySet()) {
            Pattern regex = convertPathToRegex(pattern);
            Matcher matcher = regex.matcher(requestUri);
            if (matcher.matches()) {
                Map<String, String> pathVariables = extractPathVariables(pattern, requestUri);
                Mapping mapping = mappingRegistry.get(pattern);
                return new MappingResult(mapping, pathVariables);
            }
        }
        return null; // Aucun mapping trouvé
    }

    /**
     * Convertit un pattern d'URL avec des placeholders en une expression régulière.
     * Par exemple, "/api/testPathVariable/{id}" devient "^/api/testPathVariable/([^/]+)$"
     *
     * @param path Le pattern d'URL avec placeholders.
     * @return Le Pattern correspondant.
     */
    public static Pattern convertPathToRegex(String path) {
        String regex = path.replaceAll("\\{[^/]+\\}", "([^/]+)");
        regex = "^" + regex + "$";
        return Pattern.compile(regex);
    }

    /**
     * Extrait les valeurs des placeholders depuis l'URI de la requête.
     *
     * @param pattern    Le pattern d'URL (ex: "/api/testPathVariable/{id}").
     * @param requestUri L'URI de la requête (ex: "/api/testPathVariable/5").
     * @return Une map associant le nom du placeholder à sa valeur extraite.
     */
    public static Map<String, String> extractPathVariables(String pattern, String requestUri) {
        Pattern regex = convertPathToRegex(pattern);
        Matcher matcher = regex.matcher(requestUri);
        Map<String, String> variables = new HashMap<>();

        if (matcher.matches()) {
            // Pour récupérer les noms des variables dans le pattern
            Pattern varPattern = Pattern.compile("\\{([^/]+)\\}");
            Matcher varMatcher = varPattern.matcher(pattern);
            int index = 1; // Le groupe 0 correspond à l'ensemble de la correspondance
            while (varMatcher.find()) {
                String varName = varMatcher.group(1);
                String varValue = matcher.group(index);
                variables.put(varName, varValue);
                index++;
            }
        }
        return variables;
    }

    /**
     * Classe interne servant à encapsuler le résultat d'une résolution d'URL.
     */
    public static class MappingResult {
        private Mapping mapping;
        private Map<String, String> pathVariables;

        public MappingResult(Mapping mapping, Map<String, String> pathVariables) {
            this.mapping = mapping;
            this.pathVariables = pathVariables;
        }

        public Mapping getMapping() {
            return mapping;
        }

        public Map<String, String> getPathVariables() {
            return pathVariables;
        }
    }
}
