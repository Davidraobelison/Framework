package mg.itu.prom16.util;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotation.RequestParam;
import java.lang.reflect.Method;

public class MethodParameterParser {

    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();

        int i = 0;
        for (Parameter arg : method.getParameters()) {
            RequestParam requestParam = arg.getAnnotation(RequestParam.class);
            System.out.println(arg.getName()); 
            
            String annotName;
            if (!requestParam.value().isEmpty()) {
                annotName = requestParam.value();
            }
            else {
                annotName = arg.getName();
            }
            
            String value = request.getParameter(annotName);
            Class<?> type = arg.getType();
            Object parsedValue = parseValue(value, type);
            parsedArgs.add(parsedValue);
            i++;
        }
        return parsedArgs;
    }

    private static Object parseValue(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        // Ajouter plus de types selon les besoins
        // Par exemple, Long, Float, etc.
        else {
            throw new IllegalArgumentException("Type de paramètre non supporté: " + type);
        }
    }
}
