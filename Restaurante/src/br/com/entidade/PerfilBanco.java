package br.com.entidade;

/**
 * Contas MySQL criadas em banco/01_estrutura.sql.
 * LOGIN só lê usuarios e grupos_usuarios: é a conexão usada antes do login.
 * Obs.: em produção, leia estas credenciais de um arquivo de configuração.
 */
public enum PerfilBanco {
    LOGIN("app_login", "LoginSenha123!"),
    GARCOM("garcom_user", "GarcomSenha123!"),
    GERENTE("gerente_user", "GerenteSenha123!"),
    ADMIN("admin_user", "AdminSenha123!");

    private final String usuario;
    private final String senha;

    PerfilBanco(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }
}
