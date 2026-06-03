package biblioteca.modelo.dominio;

import java.util.Arrays;
import java.util.Objects;

/**
 * Clase Libro.
 * Representa un libro de la biblioteca.
 * Contiene ISBN, título, año, categoría y autores.
 */
public class Libro implements Comparable<Libro>{

    //Patrones ISBN y máximo de autores
    public static final String PATRON_ISBN = "\\d{10}|\\d{13}";
    public static final int MAX_AUTORES = 3;

    // Atributos
    private String isbn;
    private String titulo;
    private int anio;
    private Categoria categoria;
    private Autor[] autores;
    private int numAutores;

    // Constructor principal
    public Libro(String isbn, String titulo, int anio, Categoria categoria) {

        setIsbn(isbn);
        setTitulo(titulo);
        setAnio(anio);
        setCategoria(categoria);
        this.autores = new Autor[MAX_AUTORES];
        this.numAutores = 0;
    }

    // Constructor Copia
    public Libro(Libro libro) {
        setIsbn(libro.getISBN());
        setTitulo(libro.getTitulo());
        setAnio(libro.getAnio());
        setCategoria(libro.getCategoria());
        this.autores = new Autor[MAX_AUTORES];
        this.numAutores = 0;
        for (Autor a : libro.getAutores()) {
            if (a != null) {
            addAutor(new Autor(a));
        }
        }
    }

    //Constructor de búsqueda por ISBN
    public Libro(String isbn) {
        setIsbn(isbn);
        this.titulo = "Temporal";
        this.anio = 1;
        this.categoria = Categoria.OTROS;
        this.autores = new Autor[MAX_AUTORES];
        this.numAutores = 0;
    }

    // Getters y Setters
    public String getISBN() {
        return isbn;
    }
    public String getTitulo() {
        return titulo;
    }
    public int getAnio() {
        return anio;
    }
    public Categoria getCategoria() {
        return categoria;
    }
    public Autor[] getAutores() {
        Autor[] copia = new Autor[MAX_AUTORES];
        for (int i = 0; i < MAX_AUTORES; i++) {
            if (autores[i] != null) {
                copia[i] = new Autor(autores[i]);
            }
        }
        return copia;
    }

    public void setIsbn(String isbn) throws IllegalArgumentException {
        if (isbn == null) throw new IllegalArgumentException("ISBN no puede ser nulo");
        if (isbn.trim().isEmpty()) throw new IllegalArgumentException("ISBN no puede estar vacío");
        //Si el ISBN no sigue el patrón lanza una excepción
        if (!isbn.matches(PATRON_ISBN)) throw new IllegalArgumentException("ISBN no válido");
        this.isbn = isbn;
    }
    public void setTitulo(String titulo) throws IllegalArgumentException {
        if (titulo == null) throw new IllegalArgumentException("Título no puede ser nulo");
        if (titulo.trim().isEmpty()) throw new IllegalArgumentException("Título no puede estar vacío");
        this.titulo = titulo;
    }
    public void setAnio(int anio) throws IllegalArgumentException {

        int anioActual = java.time.LocalDate.now().getYear();

        if (anio <= 0) {
            throw new IllegalArgumentException("Año inválido");
        }

        if (anio > anioActual) {
            throw new IllegalArgumentException("El año no puede ser futuro");
        }

        this.anio = anio;
    }
    public void setCategoria(Categoria categoria) throws IllegalArgumentException {
        if (categoria == null) throw new IllegalArgumentException("Categoría no puede ser nulo");
        this.categoria = categoria;
    }

    //Métodos de gestión de autores

    public void addAutor(Autor autor) throws IllegalArgumentException {
        if (autor == null) {
            throw new IllegalArgumentException("Autor no puede ser nulo");
        }

        for (int i = 0; i < MAX_AUTORES; i++) {
            if (autores[i] == null) {
                autores[i] = new Autor(autor); // copia profunda
                return;
            }
        }

        throw new IllegalStateException("Máximo número de autores alcanzado");
    } //Añadir autor

    private String formatearAutores() {
        StringBuilder sb = new StringBuilder();

        for (Autor autor : autores) {
            if (autor != null) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(autor);
            }
        }

        return sb.toString();
    }

    //Overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return Objects.equals(isbn, libro.isbn);
    }
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    //ToString
    @Override
    public String toString() {
        return String.format(
                "Título: %s\nAutor(es): %s\nAño: %d\nCategoría: %s\nISBN: %s",
                titulo,
                formatearAutores(),
                anio,
                categoria,
                isbn
        );
    }

    @Override
    public int compareTo(Libro libro) {
        if (libro == null) {
            throw new IllegalArgumentException("No se puede comparar con null");
        }
        return this.getTitulo().compareToIgnoreCase(libro.getTitulo());
    }
}

