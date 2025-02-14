package mg.itu.prom16.util;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotation.ModelParam;
import mg.itu.prom16.annotation.MultiPartFile;
import mg.itu.prom16.annotation.PathVariable;
import mg.itu.prom16.annotation.RequestFile;
import mg.itu.prom16.annotation.RequestParam;
import mg.itu.prom16.annotation.authorization.RequireLogin;
import mg.itu.prom16.exception.InvalidConstraintException;
import mg.itu.prom16.validation.BindingResult;
import mg.itu.prom16.validation.FieldError;
import mg.itu.prom16.validation.annotation.Valid;
import mg.itu.prom16.validation.constraints.Email;
import mg.itu.prom16.validation.constraints.Max;
import mg.itu.prom16.validation.constraints.Min;
import mg.itu.prom16.validation.constraints.NotBlank;
import mg.itu.prom16.validation.constraints.Size;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ServletUtil {
    protected ApplicationContext context;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";


    public ServletUtil(ApplicationContext context) {
        this.context = context;
    }
    public static boolean isValidEmail(String email) {
        return Pattern.matches(EMAIL_REGEX, email);
    }


    public static String getParameterValue(String query, String key) {
        if (query == null || key == null) {
            return null;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(key)) {
                return keyValue[1];
            }
        }
        return null; // Retourne null si la clé n'est pas trouvée
    }

    // validation
    public static void setParamsModel(HttpServletRequest request, Object o, String valParam, boolean haveValidAnnot, List<FieldError> errors) throws Exception {
        for (Field atr : o.getClass().getDeclaredFields()) {
            atr.setAccessible(true);
            String val = request.getParameter(valParam + "." + atr.getName());
            Object valeurATr = parseValue(val, atr.getType());
            atr.set(o, valeurATr);
            if (haveValidAnnot) {
                checkValidation(atr, o, errors);
            }
        }
    }

    public static String getErrorsStr(List<FieldError> errors) {
        StringBuilder errorMessages = new StringBuilder();
        errorMessages.append("\n");
        
        // Parcourir la liste des erreurs
        for (FieldError error : errors) {
            errorMessages.append("Champ : ").append(error.getField()).append("\n")
                         .append("Message : ").append(error.getErrorMessage()).append("\n");
        }
        
        // Retourner toutes les erreurs sous forme de chaîne
        return errorMessages.toString();
    }


    public static void checkValidation(Field atr, Object o, List<FieldError> fieldErrors) throws InvalidConstraintException, Exception {
        if (atr.isAnnotationPresent(NotBlank.class)) {
            Object value = atr.get(o);

            if (value instanceof String) {
                String stringValue = (String) value;
                String message = atr.getAnnotation(NotBlank.class).message();

                if (stringValue.trim().isEmpty()) {
                    fieldErrors.add(new FieldError(atr.getName(), message, stringValue, "NotBlank"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @NotBlank doit être de type String." ));
            }
        }
        if (atr.isAnnotationPresent(Min.class)) {
            Object value = atr.get(o);
            Min minAnnot = atr.getAnnotation(Min.class); // Obtenir l'annotation @Min

            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d < minAnnot.value()) {
                    fieldErrors.add(new FieldError(atr.getName(), minAnnot.message(), d, "@Min"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Min doit être de type numérique." ));
            }
        }
        if (atr.isAnnotationPresent(Max.class)) {
            Object value = atr.get(o);
            Max maxAnnot = atr.getAnnotation(Max.class); // Obtenir l'annotation @Min

            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d > maxAnnot.value()) {
                    fieldErrors.add(new FieldError(atr.getName(), maxAnnot.message(), d, "@Max"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Max doit être de type numérique." ));
            }
        } 
        if (atr.isAnnotationPresent(Email.class)) {
            Object value = atr.get(o);

            if (value instanceof String) {
                boolean emailValid = isValidEmail(value.toString());
                if (!emailValid) {

                    Email emailAnnot = atr.getAnnotation(Email.class);
                    fieldErrors.add(new FieldError(atr.getName(), emailAnnot.message(), value, "@Email"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Email doit être de type String." ));
            }
        }
        if (atr.isAnnotationPresent(Size.class)) {
            Object value = atr.get(o);
            if (value instanceof String || value instanceof Collection 
                || value instanceof Object[] || value instanceof Map || value instanceof List 
                || value instanceof ArrayList) 
            {
                Size sizeAnnot = atr.getAnnotation(Size.class);

                if (value instanceof String) {
                    int nombreCaracteres = value.toString().length();
                    if (sizeAnnot.min() > nombreCaracteres ||  sizeAnnot.max() < nombreCaracteres) {
                        fieldErrors.add(new FieldError(atr.getName(), sizeAnnot.message(), value, "@Size"));
                    }
                } else if (value instanceof List) {
                    int nombreCaracteres = ((List) value).size();
                    if (sizeAnnot.min() > nombreCaracteres ||  sizeAnnot.max() < nombreCaracteres) {
                        fieldErrors.add(new FieldError(atr.getName(), sizeAnnot.message(), value, "@Size"));
                    }
                }
            }
        }
    }
    // end valdation

    protected static Object getPathVariableValue(String queryString, 
                                                Map<String, String> pathVariables, 
                                                String paramName, 
                                                Class<?> type) {
        String value = null;
        
        // D'abord, on essaie d'extraire depuis le queryString (s'il existe)
        if (queryString != null && !queryString.isEmpty()) {
            value = getParameterValue(queryString, paramName);
        }
        
        // Si non trouvé dans le queryString, on regarde dans les pathVariables
        if ((value == null || value.isEmpty()) && pathVariables != null && pathVariables.containsKey(paramName)) {
            value = pathVariables.get(paramName);
        }
        
        // Si la valeur est toujours absente ou vide...
        if (value == null || value.isEmpty()) {
            // Pour les types primitifs, c'est une erreur
            if (type.isPrimitive()) {
                throw new IllegalArgumentException("La variable requise '" + paramName + "' est absente dans le path.");
            }
            // Sinon, on retourne null (pour les types objets)
            return null;
        }
        
        // Conversion de la chaîne extraite dans le type attendu
        return parseValue(value, type);
    }


    protected static Object getPathVariableValue(String pathString, String paramName, Class<?> type) {
        if (pathString == null || pathString.isEmpty()) {
            throw new IllegalArgumentException("Aucune variable de chemin fournie.");
        }
    
        String value = getParameterValue(pathString, paramName);
    
        // Si la valeur est absente ou vide
        if (value == null || value.isEmpty()) {
            if (type.isPrimitive()) {
                throw new IllegalArgumentException("La variable requise '" + paramName + "' est absente dans le path.");
            }
            return null; // Retourne null pour les objets (Integer, Double, etc.)
        }
    
        // Parse la valeur en fonction du type attendu
        return parseValue(value, type);
    }
    

    public static List<Object> parseParameters(Map<String, String> pathVariables, HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();
        List<FieldError> fieldErrors = new ArrayList<>();
        boolean validAnnotExist = false;

        String pathString = request.getQueryString();

        for (Parameter arg : method.getParameters()) {
            
            if (arg.getType().equals(MySession.class)) {
                Object object = MySession.class.getDeclaredConstructor().newInstance();
                MySession session = (MySession) object;
                session.setSession(request.getSession());
                parsedArgs.add(session);
                continue;
            }

            if (arg.isAnnotationPresent(RequestFile.class)) {
                setMultipartFile(arg, request, parsedArgs);
                continue;
            }
            
            String annotName;
            Object value = null;
            RequestParam requestParam = arg.getAnnotation(RequestParam.class);
            ModelParam modelParam = arg.getAnnotation(ModelParam.class);
            PathVariable pathVariable = arg.getAnnotation(PathVariable.class);


            if (pathVariable != null) {
                if (pathVariable.value().isEmpty()) {
                    annotName = arg.getName();
                } else {
                    annotName = pathVariable.value();
                }
                value = getPathVariableValue(pathString, pathVariables, annotName, arg.getType());
                System.out.println("Value PathVariable: " + value);
            }
            else if (modelParam != null) {
                Valid valid = arg.getAnnotation(Valid.class);
                validAnnotExist = true;
                String valueParam = modelParam.value();
                if (valueParam.isEmpty()) {
                    valueParam = arg.getName();
                }

                Class<?> paramaType = arg.getType();
                Constructor<?> constructor = paramaType.getDeclaredConstructor();
                Object instance = constructor.newInstance();
                setParamsModel(request, instance, valueParam, valid != null, fieldErrors); // nouveau
                value = instance;
            }
            else if (requestParam != null) {
                if (requestParam.value().isEmpty()) {
                    annotName = arg.getName();
                }else {
                    annotName = requestParam.value();
                }
                value = request.getParameter(annotName);
            } 
            else if (arg.getType().equals(BindingResult.class) && validAnnotExist) {
                BindingResult br = getBindingResult(fieldErrors);
                parsedArgs.add(br);
                continue;
            }
            else {
                throw new Exception("Annotation not found");
            }
            parsedArgs.add(value);
        }
        return parsedArgs;
    }

    public Object invokeMethod(Map<String, String> pathVariables, ServletConfig context, Mapping mapping , HttpServletRequest request,HttpServletResponse response,  String verb) throws Exception {
        
        ApiRequest apiRequest = mapping.getRequest(verb);
        Method method = apiRequest.getMethod();
        AuthorizationHandler.isAuthorized(method, request, context);
        mapping.isValidVerb(request);
        Object instance = this.context.getBean(apiRequest.getClass1());
        List<Object> listArgs = parseParameters(pathVariables, request, method);
        putSession(request,  instance);
        Object valueFunction = method.invoke(instance, listArgs.toArray());
        return valueFunction;
    }

    private static BindingResult getBindingResult(List<FieldError> fieldErrors) {
        return new BindingResult(fieldErrors);
    }

    private static void setMultipartFile(Parameter argParameter, HttpServletRequest request, List<Object> values) throws Exception {
        RequestFile requestFile = argParameter.getAnnotation(RequestFile.class);
        String nameFileInput = "";
        if (requestFile == null || requestFile.value().isEmpty()) {
            nameFileInput = argParameter.getName();
        }
        else {
            nameFileInput = requestFile.value();
        }
    
        Part part = request.getPart(nameFileInput);
        if (part == null) {
            values.add(null);
            return;
        }

        if (argParameter.getType().isAssignableFrom(MultiPartFile.class)) {
            Class<?> paramaType = argParameter.getType();
            Constructor<?> constructor = paramaType.getDeclaredConstructor();
            Object o = constructor.newInstance();
        
            MultiPartFile multiPartFile = (MultiPartFile) o;
            multiPartFile.buildInstance(part, "1859");
            values.add(multiPartFile);
        } else {
            throw new Exception("Parameter not valid Exception for File!");
        }
    }

    private static Object parseValue(String value, Class<?> type) {
        try {
            if (type.equals(String.class)) {
                return value;
            } else if (type.equals(Integer.class) || type.equals(int.class)) {
                return Integer.parseInt(value);
            } else if (type.equals(Double.class) || type.equals(double.class)) {
                return Double.parseDouble(value);
            } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return Boolean.parseBoolean(value);
            } else {
                throw new IllegalArgumentException("Type de paramètre non supporté: " + type);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format invalide pour le type: " + type + " -> " + value);
        }
    }

    public static void putSession(HttpServletRequest request, Object obj) throws Exception {
       Field[] fields = obj.getClass().getDeclaredFields();
       
       for (Field field : fields) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                Object object = field.get(obj);

                if (object == null) {
                    object = MySession.class.getDeclaredConstructor().newInstance();
                    field.set(obj, object);
                    MySession session = (MySession) object;
                    session.setSession(request.getSession());
                    break;
                }
            }
       }
    }
}
