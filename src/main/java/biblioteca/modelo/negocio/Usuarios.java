package biblioteca.modelo.negocio;

import biblioteca.fichero.GestorBackup;
import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;
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

public class Usuarios {

    private static Usuarios instancia;
    private Connection conexion;

    private Usuarios() {
    }

    public static Usuarios getInstancia() {
        if (instancia == null) {
            instancia = new Usuarios();
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

    public void alta(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        String sqlUsuario = """
            INSERT INTO usuario (dni, nombre, email)
            VALUES (?, ?, ?)
            """;

        String sqlDireccion = """
            INSERT INTO direccion (dni, via, numero, cp, localidad)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (
                PreparedStatement psUsuario = conexion.prepareStatement(sqlUsuario);
                PreparedStatement psDireccion = conexion.prepareStatement(sqlDireccion)
        ) {

            psUsuario.setString(1, usuario.getDni());
            psUsuario.setString(2, usuario.getNombre());
            psUsuario.setString(3, usuario.getEmail());
            psUsuario.executeUpdate();

            Direccion direccion = usuario.getDireccion();
            psDireccion.setString(1, usuario.getDni());
            psDireccion.setString(2, direccion.getVia());
            psDireccion.setString(3, direccion.getNumero());
            psDireccion.setString(4, direccion.getCp());
            psDireccion.setString(5, direccion.getLocalidad());
            psDireccion.executeUpdate();

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                String mensaje = e.getMessage().toLowerCase();

                if (mensaje.contains("uk_usuario_email")) {
                    throw new IllegalArgumentException("El email ya está en uso");
                }

                throw new IllegalArgumentException("El usuario ya existe (DNI duplicado)");
            }

            throw new RuntimeException("Error al insertar usuario.", e);
        }
    }

    public boolean baja(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        String sql = "DELETE FROM usuario WHERE dni = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getDni());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            // Tiene préstamos asociados (FK RESTRICT)
            if (e.getErrorCode() == 1451) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el usuario: tiene préstamos asociados"
                );
            }

            throw new RuntimeException("Error al borrar usuario.", e);
        }
    }

    public Usuario buscar(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        String sql = """
                SELECT u.dni, u.nombre, u.email,
                       d.via, d.numero, d.cp, d.localidad
                FROM usuario u
                JOIN direccion d ON u.dni = d.dni
                WHERE u.dni = ?
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getDni());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario.", e);
        }

        return null;
    }

    public List<Usuario> todos() {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
                SELECT u.dni, u.nombre, u.email,
                       d.via, d.numero, d.cp, d.localidad
                FROM usuario u
                JOIN direccion d ON u.dni = d.dni
                ORDER BY u.nombre
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(crearUsuario(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al mostrar usuarios.", e);
        }
        return usuarios;
    }

    // Convierte un nodo XML en un objeto Usuario.
    public Usuario elementToUsuario(Element elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento usuario no puede ser null");
        }

        String dni = elemento.getAttribute("dni");
        String nombre = obtenerTexto(elemento, "nombre");
        String email = obtenerTexto(elemento, "email");

        Usuario usuario = new Usuario(dni, nombre);
        usuario.setEmail(email);

        Element elementoDireccion = (Element) elemento.getElementsByTagName("direccion").item(0);
        if (elementoDireccion != null) {
            Direccion direccion = new Direccion(
                    obtenerTexto(elementoDireccion, "via"),
                    obtenerTexto(elementoDireccion, "numero"),
                    obtenerTexto(elementoDireccion, "cp"),
                    obtenerTexto(elementoDireccion, "localidad")
            );
            usuario.setDireccion(direccion);
        }

        return usuario;
    }

    // Lee el XML de usuarios e inserta cada usuario en la base de datos.
    public void leerXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        String ruta = GestorBackup.getRutaUsuarios();
        if (!new File(ruta).exists()) {
            return;
        }

        Document dom = UtilidadesXML.xmlToDom(ruta);
        NodeList nodosUsuarios = dom.getElementsByTagName("usuario");

        for (int i = 0; i < nodosUsuarios.getLength(); i++) {
            Element elemento = (Element) nodosUsuarios.item(i);
            Usuario usuario = elementToUsuario(elemento);

            if (buscar(usuario) == null) {
                alta(usuario);
            }
        }
    }

    // Convierte un Usuario en un nodo XML.
    public Element usuarioToElement(Document dom, Usuario usuario) {
        if (dom == null || usuario == null) {
            throw new IllegalArgumentException("El DOM y el usuario no pueden ser null");
        }

        Element elementoUsuario = dom.createElement("usuario");
        elementoUsuario.setAttribute("dni", usuario.getDni());

        elementoUsuario.appendChild(crearElementoTexto(dom, "nombre", usuario.getNombre()));
        elementoUsuario.appendChild(crearElementoTexto(dom, "email", usuario.getEmail()));

        Direccion direccion = usuario.getDireccion();
        if (direccion != null) {
            Element elementoDireccion = dom.createElement("direccion");
            elementoDireccion.appendChild(crearElementoTexto(dom, "via", direccion.getVia()));
            elementoDireccion.appendChild(crearElementoTexto(dom, "numero", direccion.getNumero()));
            elementoDireccion.appendChild(crearElementoTexto(dom, "cp", direccion.getCp()));
            elementoDireccion.appendChild(crearElementoTexto(dom, "localidad", direccion.getLocalidad()));
            elementoUsuario.appendChild(elementoDireccion);
        }

        return elementoUsuario;
    }

    // Escribe todos los usuarios de la base de datos en el XML.
    public void escribirXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        Document dom = UtilidadesXML.crearDomVacio("usuarios");
        Element raiz = dom.getDocumentElement();

        for (Usuario usuario : todos()) {
            raiz.appendChild(usuarioToElement(dom, usuario));
        }

        UtilidadesXML.domToXml(dom, GestorBackup.getRutaUsuarios());
    }

    // Borra todos los usuarios de la base de datos.
    public void borrarTodos() {
        String sqlDireccion = "DELETE FROM direccion";
        String sqlUsuario = "DELETE FROM usuario";

        try (
                PreparedStatement psDireccion = conexion.prepareStatement(sqlDireccion);
                PreparedStatement psUsuario = conexion.prepareStatement(sqlUsuario)
        ) {
            psDireccion.executeUpdate();
            psUsuario.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar usuarios.", e);
        }
    }

    private Usuario crearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getString("dni"), rs.getString("nombre"));

        usuario.setEmail(rs.getString("email"));

        Direccion direccion = new Direccion(rs.getString("via"), rs.getString("numero"), rs.getString("cp"), rs.getString("localidad"));

        usuario.setDireccion(direccion);
        return usuario;
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
