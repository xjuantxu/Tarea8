package biblioteca.modelo.negocio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase para gestionar la conexion con MySQL.
public class MySQL {

    public static final String Host = "localhost";
    public static final String Esquema = "biblioteca";
    public static final String Usuario = "admin";
    public static final String Pass = "biblioteca-2026";

    private static MySQL instancia;
    private Connection conexion;

    private MySQL() {
        // Al crear la instancia se abre la conexion.
        establecerConexion();
    }

    public static MySQL getInstancia() {
        // Se usa una unica instancia para toda la aplicacion.
        if (instancia == null) {
            instancia = new MySQL();
        }
        return instancia;
    }

    private void establecerConexion() {
        try {
            // URL de conexion a la base de datos.
            String url = "jdbc:mysql://" + Host + "/" + Esquema +"?useSSL=false&allowPublicKeyRetrieval=true";

            conexion = DriverManager.getConnection(url, Usuario, Pass);

        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con la base de datos: " + e.getMessage(), e);
        }
    }

    public Connection getConexion() {
        // Devuelve la conexion para hacer consultas.
        return conexion;
    }

    public void cerrarConexion() {
        try {
            // Si la conexion esta abierta, se cierra.
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}
