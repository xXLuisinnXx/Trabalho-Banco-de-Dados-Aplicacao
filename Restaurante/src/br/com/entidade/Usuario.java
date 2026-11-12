package br.com.entidade;

import java.util.Locale;

public class Usuario {

    private String id; // USR-000001, gerado pela função gerar_id_usuario()
    private String nome;
    private String email;
    private String senhaHash; // nunca a senha em texto; nulo na sessão depois do login
    private GrupoUsuario grupo;

    public Usuario() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail do usuário é obrigatório.");
        }
        this.email = email.trim().toLowerCase(Locale.ROOT);
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public GrupoUsuario getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoUsuario grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("O grupo do usuário é obrigatório.");
        }
        this.grupo = grupo;
    }

    @Override
    public String toString() {
        return nome;
    }
}
