package mg.itu.prom16.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import mg.itu.prom16.annotation.stereotype.Autowired;
import mg.itu.prom16.annotation.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ApplicationContext {
    private Map<Class<?>, Object> beanContainer = new HashMap<>();

    public static Set<Class<?>> scanMultiplePackages(String basePackages, Class<? extends Annotation> annotation) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        String[] packages = basePackages.split(",");
        for (String pkg : packages) {
            pkg = pkg.trim();
            try {
                List<Class<?>> classes1 = ClassScanner.scanClassesStereotype(pkg, annotation);
                classes.addAll(classes1);
            } catch (Exception e) {
                System.out.println("ERROR :" + e.getMessage());
            }
            
        }
        return classes;
    }
    
    public ApplicationContext(String basePackage) throws Exception {
        // Récupère toutes les classes annotées avec @Component, @Service, @Repository
        Set<Class<?>> classes = scanMultiplePackages(basePackage, Component.class);
    
        // Instanciation des beans et stockage dans le conteneur
        for (Class<?> clazz : classes) {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beanContainer.put(clazz, instance);
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'instancier " + clazz.getName(), e);
            }
        }    
        injectDependencies();
    }
    
    // Récupère un bean par son type
    public <T> T getBean(Class<T> beanClass) {
        return beanClass.cast(beanContainer.get(beanClass));
    }
    
    // Injection des dépendances dans les champs annotés avec @Autowired
    private void injectDependencies() {
        for (Object bean : beanContainer.values()) {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Autowired.class)) {
                    // Récupère le type de la dépendance
                    Class<?> dependencyType = field.getType();
                    // Cherche le bean correspondant dans le conteneur
                    Object dependency = getBean(dependencyType);
                    if (dependency == null) {
                        throw new RuntimeException("Aucun bean trouvé pour le type " + dependencyType.getName());
                    }
                    field.setAccessible(true);
                    try {
                        field.set(bean, dependency);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Impossible d'injecter la dépendance dans " + bean.getClass().getName(), e);
                    }
                }
            }
        }
    }
    
    // Méthode d'affichage des beans (optionnelle)
    public void printBeans() {
        beanContainer.forEach((clazz, instance) ->
            System.out.println("Bean: " + clazz.getName() + " -> " + instance)
        );
    }
}
