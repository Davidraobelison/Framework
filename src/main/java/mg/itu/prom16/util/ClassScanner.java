package mg.itu.prom16.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import mg.itu.prom16.exception.PackageNotFoundException;

import java.net.URL;

public class ClassScanner {

    public static List<Class<?>> scanClasses(String packageName, Class<? extends Annotation>  classToGet) throws PackageNotFoundException, Exception {

        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
    
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new PackageNotFoundException(packageName);
        }
    
        File directory = new File(url.toURI());
        File[] files = directory.listFiles();
    
        for (File file : files) {
            String fileName = file.getName();

            if (fileName.endsWith(".class")) {
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
    
                try {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class<?> loadedClass = classLoader.loadClass(className);

                    if (loadedClass.isAnnotationPresent(classToGet)) {
                        classes.add(loadedClass);
                    } 
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

}

