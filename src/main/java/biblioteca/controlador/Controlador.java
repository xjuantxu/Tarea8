package biblioteca.controlador;

import java.time.LocalDate;
import java.util.List;

import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.*;

/**
 * Clase Controlador.
 * Hace de intermediario entre la Vista y el Modelo.
 * Encapsula toda la lógica de la aplicación.
 */
public class Controlador {

    private Modelo modelo;

    // Constructor
    public Controlador(Modelo modelo) {
        if (modelo == null)
            throw new IllegalArgumentException("Modelo  no puede ser null");

        this.modelo = modelo;
    }

    // Comienza la aplicación
    public void comenzar() {
        modelo.comenzar();
    }
    // Termina la aplicación
    public void terminar() {
        modelo.terminar();
    }

    // =========================
    // Métodos de usuarios
    // =========================

    public boolean alta(Usuario usuario) {
        if (modelo.buscarUsuario(usuario) != null) return false;
        modelo.altaUsuario(usuario);
        return true;
    }
    public boolean baja(Usuario usuario) {
        return modelo.bajaUsuario(usuario);
    }
    public boolean modificar(Usuario usuario) {
        return modelo.modificarUsuario(usuario);
    }
    public Usuario buscar(Usuario usuario) {
        return modelo.buscarUsuario(usuario);
    }
    public List<Usuario> listadoUsuarios() {
        return modelo.listadoUsuarios();
    }

    // =========================
    // Métodos de libros
    // =========================

    public boolean alta(Libro libro) {
        if (modelo.buscarLibro(libro) != null) return false;
        modelo.altaLibro(libro);
        return true;
    }
    public boolean baja(Libro libro) {
        return modelo.bajaLibro(libro);
    }
    public boolean modificar(Libro libro) {
        return modelo.modificarLibro(libro);
    }
    public Libro buscar(Libro libro) {
        return modelo.buscarLibro(libro);
    }
    public List<Libro> listadoLibros() {
        return modelo.listadoLibros();
    }

    // =========================
    // Métodos de préstamos
    // =========================

    public boolean prestar(Libro libro, Usuario usuario, LocalDate fecha) {
        return modelo.prestar(libro, usuario, fecha);
    }
    public boolean devolver(Libro libro, Usuario usuario, LocalDate fecha) {
        return modelo.devolver(libro, usuario, fecha);
    }
    public List<Prestamo> listadoPrestamos() {
        return modelo.listadoPrestamos();
    }
    public List<Prestamo> listadoPrestamos(Usuario usuario) {
        return modelo.listadoPrestamos(usuario);
    }
}
