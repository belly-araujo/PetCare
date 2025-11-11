package br.com.petcare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/petcare";
    private static final String USER = "root";
    private static final String PASSWORD = "isamanu0608@"; // <-- troca se tua senha for diferente

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
