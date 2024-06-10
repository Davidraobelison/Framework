package mg.itu.prom16.exception;

public class PackageNotFoundException extends Exception {
    
    public PackageNotFoundException(String packageName) {
        super(" Package " + packageName + "not found");
    }
}
