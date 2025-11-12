package br.com.petcare;

public class Pet {
    private int id;
    private String nome;
    private String especie;
    private int idade;
    private String tutor;

    //usado quando o pet ja ta cadastrado
    public Pet(int id, String nome, String especie, int idade, String tutor) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.idade = idade;
        this.tutor = tutor;
    }

    //usado quando vai cadastrar o pet
    public Pet(String nome, String especie, int idade, String tutor) {
        this(0, nome, especie, idade, tutor);
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecie() { return especie; }
    public int getIdade() { return idade; }
    public String getTutor() { return tutor; }

    public void setId(int id) { this.id = id; }
}
