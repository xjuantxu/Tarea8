package biblioteca.utilidades;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

// Clase de utilidad para leer, crear y guardar documentos XML usando DOM.
public class UtilidadesXML {

    // Constructor privado para no crear objetos de esta clase.
    private UtilidadesXML() {
    }

    // Convierte un fichero XML en un documento DOM.
    public static Document xmlToDom(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            throw new IllegalArgumentException("La ruta del XML no puede estar vacia");
        }

        try {
            DocumentBuilder builder = crearDocumentBuilder();
            Document dom = builder.parse(new File(ruta));
            dom.getDocumentElement().normalize();
            return dom;

        } catch (SAXException | IOException e) {
            throw new RuntimeException("Error al leer el fichero XML: " + ruta, e);
        }
    }

    // Convierte un documento DOM en un fichero XML.
    public static void domToXml(Document dom, String ruta) {
        if (dom == null) {
            throw new IllegalArgumentException("El documento DOM no puede ser null");
        }

        if (ruta == null || ruta.isEmpty()) {
            throw new IllegalArgumentException("La ruta del XML no puede estar vacia");
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(dom), new StreamResult(new File(ruta)));

        } catch (TransformerException e) {
            throw new RuntimeException("Error al escribir el fichero XML: " + ruta, e);
        }
    }

    // Crea un documento DOM vacio con la etiqueta raiz indicada.
    public static Document crearDomVacio(String etiquetaRaiz) {
        if (etiquetaRaiz == null || etiquetaRaiz.isEmpty()) {
            throw new IllegalArgumentException("La etiqueta raiz no puede estar vacia");
        }

        Document dom = crearDocumentBuilder().newDocument();
        Element raiz = dom.createElement(etiquetaRaiz);
        dom.appendChild(raiz);
        return dom;
    }

    // Crea el builder que se usa para trabajar con DOM.
    private static DocumentBuilder crearDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error al preparar el parser XML", e);
        }
    }
}
