package mg.itu.prom16.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.GET;
import mg.itu.prom16.annotation.POST;
import mg.itu.prom16.annotation.RestApi;
import mg.itu.prom16.annotation.URL;
import mg.itu.prom16.exception.DuplicateUrlException;
import mg.itu.prom16.exception.InvalidConstraintException;
import mg.itu.prom16.exception.InvalidReturnTypeException;
import mg.itu.prom16.exception.PackageNotFoundException;
import mg.itu.prom16.exception.UnauthorizedException;
import mg.itu.prom16.util.ApiRequest;
import mg.itu.prom16.util.ApplicationContext;
import mg.itu.prom16.util.AuthorizationHandler;
import mg.itu.prom16.util.ClassScanner;
import mg.itu.prom16.util.JsonParserUtil;
import mg.itu.prom16.util.Mapping;
import mg.itu.prom16.util.ServletUtil;
import mg.itu.prom16.util.UrlMappingResolver;
import mg.itu.prom16.validation.BindingResult;
import mg.itu.prom16.validation.annotation.ErrorHandlerUrl;
import mg.itu.prom16.util.ModelView;


@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 50 // 50 MB
)
public class FrontController extends HttpServlet {
    protected String basePackage;
    protected String componentScanPackages;
    protected HashMap<String , Mapping> listMapping;
    protected ServletConfig config;
    protected ApplicationContext applicationContext; // Ajoutez un champ pour votre contexte

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;        // Obtenez la valeur du package
        basePackage = config.getInitParameter("basePackageName");
        componentScanPackages = config.getInitParameter("componentScanPackages");
        try {
            applicationContext = new ApplicationContext(componentScanPackages);
            initHashMap();
        } 
        catch (PackageNotFoundException | DuplicateUrlException e) {
            e.printStackTrace();
            throw new Error(e.getMessage());
        }
        catch (Exception ex){
            throw new ServletException(ex);
        }
    }

    protected void doRestApi(Object valueFunction, HttpServletResponse response) throws Exception {
        try {
            if (valueFunction instanceof ModelView) {
                ModelView modelView = (ModelView) valueFunction;
                HashMap<String, Object> listKeyAndValue = modelView.getData();
                String dataString = JsonParserUtil.objectToJson(listKeyAndValue);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
            else {
                String dataString = JsonParserUtil.objectToJson(valueFunction);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void dispatcher(HttpServletRequest request , HttpServletResponse response,  Object valueFunction) throws InvalidReturnTypeException, Exception{
        try{
            PrintWriter out = response.getWriter();
            if (valueFunction instanceof ModelView) {

                ModelView modelAndView = (ModelView)valueFunction;

                String nameView = modelAndView.getViewName();
                HashMap<String, Object> listKeyAndValue = modelAndView.getData();
                
                for (Map.Entry<String, Object> map : listKeyAndValue.entrySet()) {
                    request.setAttribute(map.getKey(),  map.getValue());
                }

                String queryString = request.getQueryString();
                RequestDispatcher dispatcher = request.getRequestDispatcher(nameView +"?" + queryString);
                dispatcher.forward(request, response);
            }
            else if (valueFunction instanceof String) { // si string
                out.println("<ul><li> Valeur de la fonction :  "+ valueFunction.toString() + "</li></ul>");
            }
            else {
                throw new InvalidReturnTypeException(valueFunction.toString());
            }
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void initHashMap() throws DuplicateUrlException, PackageNotFoundException, Exception {
        List<Class<?>> classes = ClassScanner.scanClasses(basePackage, Controller.class);
        listMapping = new HashMap<String, Mapping>();

        for (Class<?> class1 : classes) {
            Method[] methods = class1.getDeclaredMethods();
        
            for (Method method : methods) {
                if (method.isAnnotationPresent(URL.class)) {
                    String valueAnnotation = method.getAnnotation(URL.class).value();
                    
                    String verb = getMethod(method);
                    if (!listMapping.containsKey(valueAnnotation)) {

                        ApiRequest api = new ApiRequest(class1, method);
                        Mapping map = new Mapping();
                        map.addRequest(verb, api);

                        this.listMapping.put(valueAnnotation, map);
                    }
                    else {
                        Mapping map = listMapping.get(valueAnnotation);

                        if (!map.containsKey(verb)) {
                            map.addRequest(verb, new ApiRequest(class1, method));
                        }
                        else {
                            throw new DuplicateUrlException(valueAnnotation, verb);
                        }
                        
                    }
                }
            }
        }
    }

    protected String getMethod(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return "GET";
        }      
        else if (method.getAnnotation(POST.class) != null) {
            return "POST";
        }  
        return "GET"; //par defaut si il y a pas de verb
    }


    protected BindingResult hasErrors(List<Object> argObjects) {
        for (Object object : argObjects) {
            if (object instanceof BindingResult) {
                BindingResult br = (BindingResult) object;
                if (br.hasErros()) {
                    return br;
                }
            }
        }
        return null;
    }


    protected void setBackPage(Map<String, String> pathVariables, HttpServletRequest request,HttpServletResponse response,  BindingResult rs, Method method) throws Exception
    {
        if (method.isAnnotationPresent(ErrorHandlerUrl.class)) {
            ServletUtil servletUtil = new ServletUtil(applicationContext);
            ErrorHandlerUrl errorHandlerUrl = method.getAnnotation(ErrorHandlerUrl.class);
            String handlerUrlPage = errorHandlerUrl.url();
            String verb = errorHandlerUrl.verb();

            request = new HttpServletRequestWrapper(request) {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };

            Mapping mapping =  this.listMapping.get(handlerUrlPage);
            Object value = servletUtil.invokeMethod(pathVariables, config, mapping, request,response, verb);

            if (value instanceof ModelView) {
                rs.setBackPage((ModelView) value);  
            } else {
                throw new Exception("La page a retourner doit retourne une valeur de type ModelView sinon aza mampiasa ny spring nay ");
            }
        } else {
            throw new Exception("Annotation ErrorHandlerUrl not found on this methode name :" + method.getName());
        }
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        String relativeURI = request.getServletPath();
        
        try {
            UrlMappingResolver resolver = new UrlMappingResolver(listMapping);
            UrlMappingResolver.MappingResult result = resolver.resolve(relativeURI);

            if (result == null) {
                // Aucun mapping trouvé
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // Récupération du mapping et des variables extraites
            Mapping mapping = result.getMapping();
            Map<String, String> pathVariables = result.getPathVariables();
            mapping.isValidVerb(request);

            ApiRequest apiRequest = mapping.getRequest(request.getMethod());
            Class<?> class1 = apiRequest.getClass1();
            AuthorizationHandler.isAuthorizedGeneric(class1, request, config);
            Method method = apiRequest.getMethod();

            Object instance = applicationContext.getBean(apiRequest.getClass1());
            List<Object> listArgs = ServletUtil.parseParameters(pathVariables, request, method);

            BindingResult br = hasErrors(listArgs);
            if (br != null) setBackPage(pathVariables, request,response, br, method);
            
            ServletUtil.putSession(request,  instance);
            AuthorizationHandler.isAuthorized(method, request, config);
            Object valueFunction = method.invoke(instance, listArgs.toArray());
            
            RestApi restApi = method.getAnnotation(RestApi.class);
            if (restApi != null) {
                response.setContentType("text/json");
                doRestApi(valueFunction, response);
            } else {
                dispatcher(request, response, valueFunction);
            }
        } 
        catch (InvalidConstraintException  e) {
            e.printStackTrace();
            throw new ServletException(e);
        } catch (Exception e) {   
            if (e instanceof UnauthorizedException) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                return;
            }
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");    
            response.setStatus(500);
            PrintWriter out = response.getWriter();
            out.println("<p>" + e.getMessage() + "</p>");
            out.close();  
        } 
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
