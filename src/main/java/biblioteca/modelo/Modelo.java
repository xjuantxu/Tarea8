package biblioteca.modelo;

import biblioteca.modelo.negocio.Libros;
import biblioteca.modelo.negocio.MySQL;
import biblioteca.modelo.negocio.Usuarios;
import biblioteca.modelo.negocio.Prestamos;


import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.modelo.dominio.Prestamo;

import java.time.LocalDate;
import java.util.List;

/**
 * Clase Modelo.
 * Gestiona el acceso a las colecciones de negocio (Usuarios, Libros, Préstamos)
 * y ofrece métodos para que el Controlador pueda realizar operaciones de alta,
 * baja, búsqueda y listado.
 */
public class Modelo {

    private Libros libros;
    private Usuarios usuarios;
    private Prestamos prestamos;

    public Modelo() {
    }
    public void comenzar() {
        MySQL.getInstancia();
        libros = Libros.getInstancia();
        usuarios = Usuarios.getInstancia();
        prestamos = Prestamos.getInstancia();

        libros.comenzar();
        usuarios.comenzar();
        prestamos.comenzar();
    }
    public void terminar() {
        prestamos.terminar();
        libros.terminar();
        usuarios.terminar();
        MySQL.getInstancia().cerrarConexion();
    }

    // Usuarios
    public void altaUsuario(Usuario u) {
        usuarios.alta(u);
    }
    public boolean bajaUsuario(Usuario u) {
        return usuarios.baja(u);
    }
    public Usuario buscarUsuario(Usuario u) {
        return usuarios.buscar(u);
    }
    public List<Usuario> listadoUsuarios() {
        return usuarios.todos();
    }

    // Libros
    public void altaLibro(Libro l) {
        libros.alta(l);
    }
    public boolean bajaLibro(Libro l) {
        return libros.baja(l);
    }
    public Libro buscarLibro(Libro l) {
        return libros.buscar(l);
    }
    public List<Libro> listadoLibros() {
        return libros.todos();
    }

    // Préstamos
    public boolean prestar(Libro l, Usuario u, LocalDate f) {
        return prestamos.prestar(l, u, f) != null;
    }
    public boolean devolver(Libro l, Usuario u, LocalDate f) {
        return prestamos.devolver(l, u, f);
    }
    public List<Prestamo> listadoPrestamos() {
        return prestamos.todos();
    }
}
