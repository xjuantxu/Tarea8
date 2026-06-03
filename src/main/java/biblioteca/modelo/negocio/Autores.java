package biblioteca.modelo.negocio;

import biblioteca.fichero.GestorBackup;
import biblioteca.modelo.dominio.Autor;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Autores {

    private static Autores instancia;
    private Connection conexion;

    private Autores() {
    }

    public static Autores getInstancia() {
        if (instancia == null) {
            instancia = new Autores();
        }
        return instancia;
    }

    public void comenzar() {
        conexion = MySQL.getInstancia().getConexion();
        leerXML();
    }

    public void terminar() {
        escribirXML();
        conexion = null;
    }

    public void alta(Autor autor) {
        if (autor == null) {
            throw new IllegalArgumentException("El autor no puede ser nulo");
        }

        String sql = """
                INSERT INTO autor (nombre, apellidos, nacionalidad)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellidos());
            ps.setString(3, autor.getNacionalidad());
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("El autor ya existe");
            }

            throw new RuntimeException("Error al insertar autor.", e);
        }
    }

    public Autor buscar(Autor autor) {
        if (autor == null) {
            return null;
        }

        String sql = """
                SELECT nombre, apellidos, nacionalidad
                FROM autor
                WHERE nombre = ? AND apellidos = ? AND nacionalidad = ?
                """;

        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellidos());
            ps.setString(3, autor.getNacionalidad());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearAutor(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar autor.", e);
        }

        return null;
    }

    public List<Autor> todos() {
        List<Autor> autores = new ArrayList<>();

        String sql = """
                SELECT nombre, apellidos, nacionalidad
                FROM autor
                ORDER BY apellidos, nombre
                """;

        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                autores.add(crearAutor(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar autores.", e);
        }

        return autores;
    }

    // Convierte un nodo XML en un objeto Autor.
    public Autor elementToAutor(Element elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento autor no puede ser null");
        }

        return new Autor(
                obtenerTexto(elemento, "nombre"),
                obtenerTexto(elemento, "apellidos"),
                obtenerTexto(elemento, "nacionalidad")
        );
    }

    // Lee el XML de autores e inserta cada autor en la base de datos.
    public void leerXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        String ruta = GestorBackup.getRutaAutores();
        if (!new File(ruta).exists()) {
            return;
        }

        Document dom = UtilidadesXML.xmlToDom(ruta);
        NodeList nodosAutores = dom.getElementsByTagName("autor");

        for (int i = 0; i < nodosAutores.getLength(); i++) {
            Element elemento = (Element) nodosAutores.item(i);
            Autor autor = elementToAutor(elemento);

            if (buscar(autor) == null) {
                alta(autor);
            }
        }
    }

    // Convierte un Autor en un nodo XML.
    public Element autorToElement(Document dom, Autor autor) {
        if (dom == null || autor == null) {
            throw new IllegalArgumentException("El DOM y el autor no pueden ser null");
        }

        Element elementoAutor = dom.createElement("autor");
        elementoAutor.appendChild(crearElementoTexto(dom, "nombre", autor.getNombre()));
        elementoAutor.appendChild(crearElementoTexto(dom, "apellidos", autor.getApellidos()));
        elementoAutor.appendChild(crearElementoTexto(dom, "nacionalidad", autor.getNacionalidad()));
        return elementoAutor;
    }

    // Escribe todos los autores de la base de datos en el XML.
    public void escribirXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        Document dom = UtilidadesXML.crearDomVacio("autores");
        Element raiz = dom.getDocumentElement();

        for (Autor autor : todos()) {
            raiz.appendChild(autorToElement(dom, autor));
        }

        UtilidadesXML.domToXml(dom, GestorBackup.getRutaAutores());
    }

    // Borra todos los autores de la base de datos.
    public void borrarTodos() {
        String sqlLibroAutor = "DELETE FROM libro_autor";
        String sqlAutor = "DELETE FROM autor";

        try (
                PreparedStatement psLibroAutor = getConexion().prepareStatement(sqlLibroAutor);
                PreparedStatement psAutor = getConexion().prepareStatement(sqlAutor)
        ) {
            psLibroAutor.executeUpdate();
            psAutor.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar autores.", e);
        }
    }

    private Connection getConexion() {
        if (conexion == null) {
            conexion = MySQL.getInstancia().getConexion();
        }
        return conexion;
    }

    private Autor crearAutor(ResultSet rs) throws SQLException {
        return new Autor(
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("nacionalidad")
        );
    }

    // Crea un elemento XML con texto.
    private Element crearElementoTexto(Document dom, String etiqueta, String texto) {
        Element elemento = dom.createElement(etiqueta);
        elemento.setTextContent(texto);
        return elemento;
    }

    // Obtiene el texto de una etiqueta dentro de un elemento XML.
    private String obtenerTexto(Element elemento, String etiqueta) {
        NodeList nodos = elemento.getElementsByTagName(etiqueta);
        if (nodos.getLength() == 0) {
            return "";
        }
        return nodos.item(0).getTextContent();
    }
}
