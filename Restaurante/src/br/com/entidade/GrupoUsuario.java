package br.com.entidade;

/** Grupos da tabela grupos_usuarios (nome_grupo). Cada grupo usa uma conta MySQL. */
public enum GrupoUsuario {
    GARCOM(PerfilBanco.GARCOM),
    GERENTE(PerfilBanco.GERENTE),
    ADMIN(PerfilBanco.ADMIN);

    private final PerfilBanco perfilBanco;

    GrupoUsuario(PerfilBanco perfilBanco) {
        this.perfilBanco = perfilBanco;
    }

    public PerfilBanco getPerfilBanco() {
        return perfilBanco;
    }

    public static GrupoUsuario deNome(String nome) {
        if (nome != null) {
            for (GrupoUsuario g : values()) {
                if (g.name().equalsIgnoreCase(nome.trim())) {
                    return g;
                }
            }
        }
        throw new IllegalArgumentException("Grupo de usuário desconhecido: " + nome);
    }
}
