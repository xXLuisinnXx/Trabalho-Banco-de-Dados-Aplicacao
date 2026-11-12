package br.com.controle;

import br.com.entidade.DAO;
import br.com.entidade.PerfilBanco;
import br.com.entidade.Usuario;

/**
 * Usuário logado. Ao entrar, a conexão com o banco passa a usar a conta MySQL do grupo
 * dele (garcom_user, gerente_user, admin_user): o banco também barra o que não é permitido.
 */
public final class Sessao {

    private static Usuario usuario;

    private Sessao() {
    }

    public static synchronized void iniciar(Usuario u) {
        if (u == null || u.getGrupo() == null) {
            throw new IllegalArgumentException("Usuário inválido para iniciar a sessão.");
        }
        usuario = u;
        DAO.definirPerfil(u.getGrupo().getPerfilBanco());
    }

    public static synchronized void encerrar() {
        usuario = null;
        DAO.definirPerfil(PerfilBanco.LOGIN);
    }

    public static synchronized Usuario getUsuario() {
        return usuario;
    }

    public static boolean estaLogado() {
        return getUsuario() != null;
    }

    // para as telas esconderem botões
    public static boolean temPermissao(Permissao permissao) {
        Usuario u = getUsuario();
        return u != null && permissao.permite(u.getGrupo());
    }

    // para os controles: primeira linha de cada operação protegida
    public static void exigir(Permissao permissao) {
        Usuario u = getUsuario();
        if (u == null) {
            throw new AcessoNegadoException("Faça login para continuar.");
        }
        if (!permissao.permite(u.getGrupo())) {
            throw new AcessoNegadoException("O perfil " + u.getGrupo()
                    + " não tem permissão para " + permissao.getDescricao() + ".");
        }
    }
}
