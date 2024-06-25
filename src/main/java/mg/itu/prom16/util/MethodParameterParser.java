package mg.itu.prom16.util;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotation.ModelParam;
import mg.itu.prom16.annotation.RequestParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodParameterParser {

    public static List<Object> matchValues(HttpServletRequest request, Method method) throws Exception{
        Enumeration<String> enumeration = request.getParameterNames(); 
        List<Object> parsedArgs = new ArrayList<>();
        List<String> nameParams = new ArrayList<>();
             
        while (enumeration.asIterator().hasNext()) {
            String nameForm = enumeration.nextElement();
            String nameForm2 = nameForm;
            boolean isObject = false;

            if (nameForm2.split("\\.").length == 2) {
                nameForm2 = nameForm2.split("\\.")[0];
                isObject = true;
            }

            for (int i = 0; i < method.getParameters().length;  i++) {
                Parameter parameter = method.getParameters()[i];
                Class<?> paramType = parameter.getType();
                String nameParameter;

                if (isObject) { // si c'est objet , 
                    ModelParam modelParam = parameter.getAnnotation(ModelParam.class);
                    if (modelParam != null && !modelParam.value().isEmpty()) {
                        nameParameter = modelParam.value();
                    }
                    else {
                        nameParameter = parameter.getName();
                    }
                }
                else {
                    RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                    if (requestParam != null && !requestParam.value().isEmpty()) {
                        nameParameter = requestParam.value();
                    }
                    else {
                        nameParameter = parameter.getName();
                    }
                }
                
                if (nameParameter.equals(nameForm2) && isObject) {

                    String part2Form = nameForm.split("\\.")[1];
                    Constructor<?> constructor = paramType.getDeclaredConstructor();
                    Object o = constructor.newInstance();

                    boolean isExist = nameParams.contains(nameParameter);
                    if (isExist) { // si le nom du param est deja existant dans la list param
                        o = parsedArgs.get(nameParams.indexOf(nameParameter));
                    }

                    Field[] fields = o.getClass().getDeclaredFields();

                    for (Field atr : fields) {
                        atr.setAccessible(true);
                        if (atr.getName().equals(part2Form)) {
                            String value = request.getParameter(nameParameter + "." + part2Form);
                            
                            if (value != null && isExist) {
                                atr.set(o, value);
                                break;
                            }
                            else if(value != null && !isExist){
                                atr.set(o, value);
                                nameParams.add(nameParameter);
                                parsedArgs.add(o);
                                break;
                            }
                        }
                    }
                }
                else if (nameParameter.equals(nameForm2) && !isObject) {
                    parsedArgs.add(request.getParameter(nameParameter));
                } 
            }
        }
        return parsedArgs;
    }

    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();

        for (Parameter arg : method.getParameters()) {
            
            RequestParam requestParam = arg.getAnnotation(RequestParam.class);
            ModelParam modelParam = arg.getAnnotation(ModelParam.class);

            Object value = null;
            if (modelParam != null) { // Object
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

            String annotName;
            if (requestParam != null) {
                if (requestParam.value().isEmpty()) {
                    annotName = arg.getName();
                }
                else {
                    annotName = requestParam.value();
                }
                value = request.getParameter(annotName);
            }
            parsedArgs.add(value);
        }
        return parsedArgs;
    }
}
