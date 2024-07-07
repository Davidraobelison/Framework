package mg.itu.prom16.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLConfigReader {

    public static String extractProjectName(String path) {
        // Supprimer les séparateurs de répertoire et tout ce qui suit après le nom du projet
        String[] parts = path.split("[\\\\/]");
        return parts[1];
    }

    public static String readBasePackage(String configFile) throws Exception {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(configFile));

        Element root = document.getDocumentElement();
        NodeList componentScanList = root.getElementsByTagName("context:component-scan");

        if (componentScanList.getLength() > 0) {
            Element componentScanElement = (Element) componentScanList.item(0);
            return componentScanElement.getAttribute("base-package");
        }

        return null; // or throw an exception if not found
    }
}

