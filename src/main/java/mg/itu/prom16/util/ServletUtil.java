package mg.itu.prom16.util;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotation.ModelParam;
import mg.itu.prom16.annotation.MultiPartFile;
import mg.itu.prom16.annotation.RequestFile;
import mg.itu.prom16.annotation.RequestParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ServletUtil {
    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();

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

            if (modelParam != null) {
                String valueParam = modelParam.value();
                if (valueParam.isEmpty()) {
                    valueParam = arg.getName();
                }

                Class<?> paramaType = arg.getType();
                Constructor<?> constructor = paramaType.getDeclaredConstructor();
                Object o = constructor.newInstance();

                for (Field atr : o.getClass().getDeclaredFields()) {
                    atr.setAccessible(true);
                    String val = request.getParameter(valueParam + "." + atr.getName());
                    atr.set(o, val);
                }
                value = o;
            }
            else if (requestParam != null) {
                if (requestParam.value().isEmpty()) {
                    annotName = arg.getName();
                }
                else {
                    annotName = requestParam.value();
                }
                value = request.getParameter(annotName);
            }
            else {
                throw new Exception("Annotation not found");
            }
            parsedArgs.add(value);
        }
        return parsedArgs;
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
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        else {
            throw new IllegalArgumentException("Type de paramètre non supporté: " + type);
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
