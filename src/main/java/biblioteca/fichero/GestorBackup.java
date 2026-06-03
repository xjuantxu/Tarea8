package biblioteca.fichero;

import biblioteca.modelo.negocio.Autores;
import biblioteca.modelo.negocio.Libros;
import biblioteca.modelo.negocio.Prestamos;
import biblioteca.modelo.negocio.Usuarios;

import java.io.File;

// Gestiona la ruta donde se guardan y cargan las copias de seguridad en XML.
// Es una clase de utilidad, por eso todos sus metodos son estaticos.
public class GestorBackup {

    // Nombre del fichero XML de usuarios.
    public static final String FICHERO_USUARIOS = "usuarios.xml";

    // Nombre del fichero XML de libros.
    public static final String FICHERO_LIBROS = "libros.xml";

    // Nombre del fichero XML de autores.
    public static final String FICHERO_AUTORES = "autores.xml";

    // Nombre del fichero XML de prestamos.
    public static final String FICHERO_PRESTAMOS = "prestamos.xml";

    // Directorio seleccionado para trabajar con las copias de seguridad.
    private static String directorio = null;

    // Constructor privado para evitar que se creen objetos de esta clase.
    private GestorBackup() {
    }

    // Devuelve el directorio de backup configurado actualmente.
    public static String getDirectorio() {
        return directorio;
    }

    // Configura el directorio donde se guardaran o leeran los XML.
    // Si la ruta esta vacia o es null, lanza una excepcion.
    public static void setDirectorio(String nuevoDirectorio) {
        if (nuevoDirectorio == null || nuevoDirectorio.isEmpty()) {
            throw new IllegalArgumentException("El directorio no puede estar vacÃo");
        }
        directorio = nuevoDirectorio;
    }

    // Comprueba si hay un directorio de backup configurado.
    public static boolean hayDirectorioActivo() {
        return directorio != null && !directorio.isEmpty();
    }

    // Genera la ruta completa del fichero de usuarios.
    // Si no hay directorio activo, devuelve null.
    public static String getRutaUsuarios() {
        return hayDirectorioActivo() ? directorio + File.separator + FICHERO_USUARIOS : null;
    }

    // Genera la ruta completa del fichero de libros.
    // Si no hay directorio activo, devuelve null.
    public static String getRutaLibros() {
        return hayDirectorioActivo() ? directorio + File.separator + FICHERO_LIBROS : null;
    }

    // Genera la ruta completa del fichero de autores.
    // Si no hay directorio activo, devuelve null.
    public static String getRutaAutores() {
        return hayDirectorioActivo() ? directorio + File.separator + FICHERO_AUTORES : null;
    }

    // Genera la ruta completa del fichero de prestamos.
    // Si no hay directorio activo, devuelve null.
    public static String getRutaPrestamos() {
        return hayDirectorioActivo() ? directorio + File.separator + FICHERO_PRESTAMOS : null;
    }

    // Crea una copia de seguridad escribiendo los datos actuales en sus XML.
    // Si el directorio no existe, lo crea antes de guardar los ficheros.
    public static void hacerCopiaSeguridad() {
        if (!hayDirectorioActivo()) {
            throw new IllegalStateException("No hay directorio de backup configurado");
        }

        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Autores.getInstancia().escribirXML();
        Libros.getInstancia().escribirXML();
        Usuarios.getInstancia().escribirXML();
        Prestamos.getInstancia().escribirXML();
    }

    // Carga una copia de seguridad desde los XML del directorio activo.
    // Primero borra los datos actuales para evitar duplicados y problemas con claves foraneas.
    public static void cargarCopiaSeguridad() {
        // Borrado en orden inverso a las dependencias FK
        Prestamos.getInstancia().borrarTodos();
        Libros.getInstancia().borrarTodos();
        Usuarios.getInstancia().borrarTodos();
        Autores.getInstancia().borrarTodos();

        Autores.getInstancia().leerXML();
        Libros.getInstancia().leerXML();
        Usuarios.getInstancia().leerXML();
        Prestamos.getInstancia().leerXML();
    }

    // Comprueba si existen todos los ficheros necesarios para restaurar un backup.
    public static boolean existenFicherosBackup() {
        if (!hayDirectorioActivo()) {
            return false;
        }
        return new File(getRutaAutores()).exists()
                && new File(getRutaLibros()).exists()
                && new File(getRutaUsuarios()).exists()
                && new File(getRutaPrestamos()).exists();
    }
}
