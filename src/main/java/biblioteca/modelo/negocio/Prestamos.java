package biblioteca.modelo.negocio;

import biblioteca.fichero.GestorBackup;
import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Prestamos {

    private static Prestamos instancia;
    private Connection conexion;

    private Prestamos() {
    }

    public static Prestamos getInstancia() {
        if (instancia == null) {
            instancia = new Prestamos();
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

    public Prestamo prestar(Libro libro, Usuario usuario, LocalDate fecha) {
        if (libro == null || usuario == null || fecha == null) {
            throw new IllegalArgumentException("Datos invalidos");
        }

        Prestamo prestamo = new Prestamo(libro, usuario, fecha);
        LocalDate fechaLimite = fecha.plusDays(14);

        String sql = """
                INSERT INTO prestamo (dni, isbn, fInicio, fLimite, devuelto, fDevolucion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getDni());
            ps.setString(2, libro.getISBN());
            ps.setDate(3, Date.valueOf(prestamo.getInicio()));
            ps.setDate(4, Date.valueOf(fechaLimite));
            ps.setBoolean(5, false);
            ps.setDate(6, null);

            ps.executeUpdate();
            return new Prestamo(prestamo);

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("El préstamo ya existe");
            }

            throw new RuntimeException("Error al insertar prestamo.", e);
        }
    }

    public boolean devolver(Libro libro, Usuario usuario, LocalDate fecha) {
        if (libro == null || usuario == null || fecha == null) {
            throw new IllegalArgumentException("Datos invalidos");
        }

        String sql = """
                UPDATE prestamo
                SET devuelto = ?, fDevolucion = ?
                WHERE dni = ? AND isbn = ? AND devuelto = false
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBoolean(1, true);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setString(3, usuario.getDni());
            ps.setString(4, libro.getISBN());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al devolver prestamo.", e);
        }
    }

    public List<Prestamo> todos() {
        List<Prestamo> prestamos = new ArrayList<>();

        String sql = """
                SELECT dni, isbn, fInicio, devuelto, fDevolucion
                FROM prestamo
                ORDER BY fInicio DESC
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(crearPrestamo(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar prestamos.", e);
        }

        Collections.sort(prestamos);
        return prestamos;
    }

    // Convierte un nodo XML en un Prestamo.
    public Prestamo elementToPrestamo(Element elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento prestamo no puede ser null");
        }

        String dni = obtenerTexto(elemento, "dni");
        String isbn = obtenerTexto(elemento, "isbn");
        LocalDate inicio = LocalDate.parse(obtenerTexto(elemento, "inicio"));

        Usuario usuario = Usuarios.getInstancia().buscar(new Usuario(dni));
        Libro libro = Libros.getInstancia().buscar(new Libro(isbn));

        Prestamo prestamo = new Prestamo(libro, usuario, inicio);

        boolean devuelto = Boolean.parseBoolean(obtenerTexto(elemento, "devuelto"));
        String fin = obtenerTexto(elemento, "fin");

        if (devuelto && !fin.isEmpty()) {
            prestamo.devolver(LocalDate.parse(fin));
        }

        return prestamo;
    }

    // Lee el XML de prestamos e inserta cada prestamo en la base de datos.
    public void leerXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        String ruta = GestorBackup.getRutaPrestamos();
        if (!new File(ruta).exists()) {
            return;
        }

        Document dom = UtilidadesXML.xmlToDom(ruta);
        NodeList nodosPrestamos = dom.getElementsByTagName("prestamo");

        for (int i = 0; i < nodosPrestamos.getLength(); i++) {
            Element elemento = (Element) nodosPrestamos.item(i);
            Prestamo prestamo = elementToPrestamo(elemento);

            if (!existe(prestamo)) {
                insertarPrestamo(prestamo);
            }
        }
    }

    // Convierte un Prestamo en un nodo XML.
    public Element prestamoToElement(Document dom, Prestamo prestamo) {
        if (dom == null || prestamo == null) {
            throw new IllegalArgumentException("El DOM y el prestamo no pueden ser null");
        }

        Element elementoPrestamo = dom.createElement("prestamo");

        elementoPrestamo.appendChild(crearElementoTexto(dom, "dni", prestamo.getUsuario().getDni()));
        elementoPrestamo.appendChild(crearElementoTexto(dom, "isbn", prestamo.getLibro().getISBN()));
        elementoPrestamo.appendChild(crearElementoTexto(dom, "inicio", prestamo.getInicio().toString()));
        elementoPrestamo.appendChild(crearElementoTexto(dom, "devuelto", String.valueOf(prestamo.isDevuelto())));
        elementoPrestamo.appendChild(crearElementoTexto(
                dom,
                "fin",
                prestamo.getFin() != null ? prestamo.getFin().toString() : ""
        ));

        return elementoPrestamo;
    }

    // Escribe todos los prestamos de la base de datos en el XML.
    public void escribirXML() {
        if (!GestorBackup.hayDirectorioActivo()) {
            return;
        }

        Document dom = UtilidadesXML.crearDomVacio("prestamos");
        Element raiz = dom.getDocumentElement();

        for (Prestamo prestamo : todos()) {
            raiz.appendChild(prestamoToElement(dom, prestamo));
        }

        UtilidadesXML.domToXml(dom, GestorBackup.getRutaPrestamos());
    }

    // Borra todos los prestamos de la base de datos.
    public void borrarTodos() {
        String sql = "DELETE FROM prestamo";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar prestamos.", e);
        }
    }

    private Prestamo crearPrestamo(ResultSet rs) throws SQLException {
        Usuario usuario = Usuarios.getInstancia().buscar(new Usuario(rs.getString("dni")));
        Libro libro = Libros.getInstancia().buscar(new Libro(rs.getString("isbn")));

        Prestamo prestamo = new Prestamo(libro, usuario, rs.getDate("fInicio").toLocalDate());

        if (rs.getBoolean("devuelto") && rs.getDate("fDevolucion") != null) {
            prestamo.devolver(rs.getDate("fDevolucion").toLocalDate());
        }

        return prestamo;
    }

    // Inserta un prestamo desde el XML respetando si esta devuelto o pendiente.
    private void insertarPrestamo(Prestamo prestamo) {
        String sql = """
                INSERT INTO prestamo (dni, isbn, fInicio, fLimite, devuelto, fDevolucion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, prestamo.getUsuario().getDni());
            ps.setString(2, prestamo.getLibro().getISBN());
            ps.setDate(3, Date.valueOf(prestamo.getInicio()));
            ps.setDate(4, Date.valueOf(prestamo.getInicio().plusDays(14)));
            ps.setBoolean(5, prestamo.isDevuelto());
            ps.setDate(6, prestamo.getFin() != null ? Date.valueOf(prestamo.getFin()) : null);
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return;
            }

            throw new RuntimeException("Error al insertar prestamo desde XML.", e);
        }
    }

    // Comprueba si el prestamo ya existe en la base de datos.
    private boolean existe(Prestamo prestamo) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM prestamo
                WHERE dni = ? AND isbn = ? AND fInicio = ?
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, prestamo.getUsuario().getDni());
            ps.setString(2, prestamo.getLibro().getISBN());
            ps.setDate(3, Date.valueOf(prestamo.getInicio()));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar prestamo.", e);
        }
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
