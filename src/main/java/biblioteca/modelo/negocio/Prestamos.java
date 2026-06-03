package biblioteca.modelo.negocio;

import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.dominio.Usuario;

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
    }

    public void terminar() {
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

    private Prestamo crearPrestamo(ResultSet rs) throws SQLException {
        Usuario usuario = Usuarios.getInstancia().buscar(new Usuario(rs.getString("dni")));
        Libro libro = Libros.getInstancia().buscar(new Libro(rs.getString("isbn")));

        Prestamo prestamo = new Prestamo(libro, usuario, rs.getDate("fInicio").toLocalDate());

        if (rs.getBoolean("devuelto") && rs.getDate("fDevolucion") != null) {
            prestamo.devolver(rs.getDate("fDevolucion").toLocalDate());
        }

        return prestamo;
    }
}