package biblioteca.modelo.negocio;

import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;

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
    }

    public void terminar() {
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

    public boolean modificar(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        String sqlUsuario = """
                UPDATE usuario
                SET nombre = ?, email = ?
                WHERE dni = ?
                """;

        String sqlDireccion = """
                UPDATE direccion
                SET via = ?, numero = ?, cp = ?, localidad = ?
                WHERE dni = ?
                """;

        boolean autoCommitOriginal = true;

        try {
            //desactivamos que cada operación en la base de datos se confirme automáticamente
            autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            try (PreparedStatement psUsuario = conexion.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, usuario.getNombre());
                psUsuario.setString(2, usuario.getEmail());
                psUsuario.setString(3, usuario.getDni());

                //Si no afecta a ningún registro no hace nada
                if (psUsuario.executeUpdate() == 0) {
                    conexion.rollback();
                    return false;
                }
            }

            Direccion direccion = usuario.getDireccion();

            try (PreparedStatement psDireccion = conexion.prepareStatement(sqlDireccion)) {
                psDireccion.setString(1, direccion.getVia());
                psDireccion.setString(2, direccion.getNumero());
                psDireccion.setString(3, direccion.getCp());
                psDireccion.setString(4, direccion.getLocalidad());
                psDireccion.setString(5, usuario.getDni());
                psDireccion.executeUpdate();
            }

            //Si tiene éxito realiza el cambio
            conexion.commit();
            return true;

        } catch (SQLException e) {
            try {
                //En caso de excepción deshace la operación, y en caso de mostrar error por el rollback lo captura
                conexion.rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(rollbackException);
            }

            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("El email ya está en uso");
            }

            throw new RuntimeException("Error al modificar usuario.", e);
        } finally {
            try {
                //Vuelve a realizar automaticamente las operaciones en la base de datos
                conexion.setAutoCommit(autoCommitOriginal);
            } catch (SQLException e) {
                throw new RuntimeException("Error al restaurar la conexion.", e);
            }
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

    private Usuario crearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getString("dni"), rs.getString("nombre"));

        usuario.setEmail(rs.getString("email"));

        Direccion direccion = new Direccion(rs.getString("via"), rs.getString("numero"), rs.getString("cp"), rs.getString("localidad"));

        usuario.setDireccion(direccion);
        return usuario;
    }
}
