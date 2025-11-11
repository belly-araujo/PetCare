package br.com.petcare;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {
    public static void inserir(Pet pet) throws SQLException {
        String sql = "INSERT INTO pet (nome, especie, idade, tutor) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getNome());
            stmt.setString(2, pet.getEspecie());
            stmt.setInt(3, pet.getIdade());
            stmt.setString(4, pet.getTutor());
            stmt.executeUpdate();
        }
    }

    public static List<Pet> listar() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pet";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pets.add(new Pet(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("especie"),
                        rs.getInt("idade"),
                        rs.getString("tutor")
                ));
            }
        }
        return pets;
    }

    public static void excluir(int id) throws SQLException {
        String sql = "DELETE FROM pet WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
