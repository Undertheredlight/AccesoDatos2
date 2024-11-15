package Biblioteca;

import java.sql.*;

/**
 *
 * @author Liz
 */
public class InserccionLibro {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/biblioteca";
        String user = "user-biblioteca";
        String password = "b1bl10t3ca";

        try (Connection con = DriverManager.getConnection(url, user, password); Statement sentencia = con.createStatement(); CallableStatement procedimiento = con.prepareCall("{CALL new_procedure()}")) {

            System.out.println("Conexión correcta");

            // Datos de ejemplo para prueba
            String isbn = "9999999999";
            String titulo = "Ejemplo de Libro";
            int numeroEjemplares = 5;
            String nombreAutor = "Autor Ejemplo";
            String nombreEditorial = "Editorial Ejemplo";
            String nombreTema = "Tema Ejemplo";

            // Comprobación si el libro ya existe
            String selectLibro = "SELECT * FROM Libro WHERE ISBN = ?;";
            try (PreparedStatement stmtLibro = con.prepareStatement(selectLibro)) {
                stmtLibro.setString(1, isbn);
                try (ResultSet rs = stmtLibro.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("El libro ya existe");
                        visualizarLibros();
                    } else {
                        // Comprobar existencia del autor, tema, y editorial
                        if (!existeAutor(nombreAutor, con)) {
                            altaAutor(nombreAutor, con);
                        }
                        int idAutor = buscarAutor(nombreAutor, con);

                        int idTema = buscarTema(nombreTema, con);
                        if (idTema == -1) {
                            altaTema(nombreTema, con);
                            idTema = buscarTema(nombreTema, con);
                        }

                        if (!existeEditorial(nombreEditorial, con)) {
                            procedimiento.execute();
                        }
                        int idEditorial = buscarEditorial(nombreEditorial, con);

                        // Dar de alta el libro
                        altaLibro(isbn, titulo, numeroEjemplares, idAutor, idEditorial, idTema, con);
                        visualizarLibros();
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Conexión incorrecta...");
            System.out.println(e.getMessage());
        }
    }

    // Método para visualizar todos los libros
    public static void visualizarLibros() {
        String url = "jdbc:mysql://localhost:3306/biblioteca";
        String user = "user-biblioteca";
        String password = "b1bl10t3ca";

        try (Connection con = DriverManager.getConnection(url, user, password); Statement sentencia = con.createStatement()) {

            String selectLibro = "SELECT * FROM libro NATURAL JOIN (autor, editorial, tema);";
            try (ResultSet rs = sentencia.executeQuery(selectLibro)) {
                while (rs.next()) {
                    System.out.println(rs.getString("ISBN") + " ");
                    System.out.println(rs.getString("titulo") + " ");
                    System.out.println(rs.getInt("numeroEjemplares") + " ");
                    System.out.println(rs.getString("nombreAutor") + " ");
                    System.out.println(rs.getString("nombreEditorial") + " ");
                    System.out.println(rs.getString("direccion") + " ");
                    System.out.println(rs.getString("telefono") + " ");
                    System.out.println(rs.getString("nombreTema") + " ");
                    System.out.println("-------------------------------");
                    
                }
            }
        } catch (SQLException e) {
            System.out.println("Conexión incorrecta");
            System.out.println(e.getMessage());
        }
    }

    // Método que verifica si existe el autor
    private static boolean existeAutor(String nombreAutor, Connection con) throws SQLException {
        String sentenciaAutor = "SELECT * FROM autor WHERE nombreAutor = ?";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaAutor)) {
            stmt.setString(1, nombreAutor);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Método que da de alta a un autor (si no existe)
    private static void altaAutor(String nombreAutor, Connection con) throws SQLException {
        String sentenciaInsert = "INSERT INTO autor(nombreAutor) VALUES (?)";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaInsert)) {
            stmt.setString(1, nombreAutor);
            stmt.executeUpdate();
        }
    }

    // Método para buscar a un autor
    private static int buscarAutor(String nombreAutor, Connection con) throws SQLException {
        String sentenciaAutor = "SELECT idAutor FROM autor WHERE nombreAutor = ?";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaAutor)) {
            stmt.setString(1, nombreAutor);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idAutor");
                }
            }
        }
        return -1;
    }

    // Método para buscar un tema 
    private static int buscarTema(String nombreTema, Connection con) throws SQLException {
        String selectTema = "SELECT idTema FROM tema WHERE nombreTema = ?";
        try (PreparedStatement stmt = con.prepareStatement(selectTema)) {
            stmt.setString(1, nombreTema);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idTema");
                }
            }
        }
        return -1;
    }

    // Método para dar de alta un tema (si no existe)
    private static void altaTema(String nombreTema, Connection con) throws SQLException {
        String sentenciaInsert = "INSERT INTO tema(nombreTema) VALUES (?)";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaInsert)) {
            stmt.setString(1, nombreTema);
            stmt.executeUpdate();
        }
    }

    // Método para saber si existe la editorial
    private static boolean existeEditorial(String nombreEditorial, Connection con) throws SQLException {
        String sentenciaEditorial = "SELECT * FROM editorial WHERE nombreEditorial = ?";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaEditorial)) {
            stmt.setString(1, nombreEditorial);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Método para buscar una editorial
    private static int buscarEditorial(String nombreEditorial, Connection con) throws SQLException {
        String selectEditorial = "SELECT idEditorial FROM editorial WHERE nombreEditorial = ?";
        try (PreparedStatement stmt = con.prepareStatement(selectEditorial)) {
            stmt.setString(1, nombreEditorial);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idEditorial");
                }
            }
        }
        return -1;
    }

    // Método para dar de alta un libro
    private static void altaLibro(String isbn, String titulo, int numeroEjemplares, int idAutor, int idEditorial, int idTema, Connection con) throws SQLException {
        String sentenciaInsert = "INSERT INTO libro (isbn, titulo, numeroEjemplares, idAutor, idEditorial, idTema) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sentenciaInsert)) {
            stmt.setString(1, isbn);
            stmt.setString(2, titulo);
            stmt.setInt(3, numeroEjemplares);
            stmt.setInt(4, idAutor);
            stmt.setInt(5, idEditorial);
            stmt.setInt(6, idTema);
            stmt.executeUpdate();
        }
    }
}
