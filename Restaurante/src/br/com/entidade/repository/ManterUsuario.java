package br.com.entidade.repository;

import java.sql.ResultSet;
import java.util.ArrayList;

import br.com.entidade.DAO;
import br.com.entidade.GrupoUsuario;
import br.com.entidade.Usuario;

public class ManterUsuario extends DAO {

    private static final String SELECT = "select u.id_usuario, u.nome, u.email, u.senha_hash, g.nome_grupo "
            + "FROM usuarios u inner join grupos_usuarios g on g.id_grupo = u.id_grupo ";
    private static final String GRUPO_POR_NOME = "(select id_grupo FROM grupos_usuarios where nome_grupo = ?)";

    public void inserir(Usuario u) throws Exception {
        abrirBanco();
        try {
            con.setAutoCommit(false);
            // o id é gerado dentro da transação: se o INSERT falhar, o contador de
            // sequencia_id volta atrás e não sobra buraco na numeração USR-xxxxxx
            pst = con.prepareStatement("select gerar_id_usuario()");
            ResultSet tr = pst.executeQuery();
            tr.next();
            String novoId = tr.getString(1);

            pst = con.prepareStatement("INSERT INTO usuarios (id_usuario,nome,email,senha_hash,id_grupo) "
                    + "values(?,?,?,?," + GRUPO_POR_NOME + ")");
            pst.setString(1, novoId);
            pst.setString(2, u.getNome());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getSenhaHash());
            pst.setString(5, u.getGrupo().name());
            pst.executeUpdate();
            con.commit();
            u.setId(novoId);
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            fecharBanco();
        }
    }

    // atualizarSenha = false mantém o hash que já está no banco
    public void editarUsuario(Usuario u, boolean atualizarSenha) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE usuarios set nome = ?, email = ?, id_grupo = " + GRUPO_POR_NOME
                    + (atualizarSenha ? ", senha_hash = ?" : "") + " where id_usuario = ?";
            pst = con.prepareStatement(query);
            int i = 1;
            pst.setString(i++, u.getNome());
            pst.setString(i++, u.getEmail());
            pst.setString(i++, u.getGrupo().name());
            if (atualizarSenha) {
                pst.setString(i++, u.getSenhaHash());
            }
            pst.setString(i, u.getId());
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

    public void deletarUsuario(Usuario u) throws Exception {
        abrirBanco();
        try {
            pst = con.prepareStatement("delete from usuarios where id_usuario=?");
            pst.setString(1, u.getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Usuario u) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            pst = con.prepareStatement(SELECT + "where u.id_usuario=?");
            pst.setString(1, u.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                copiar(tr, u);
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

    // usado no login: devolve o usuário com o hash, ou null se o e-mail não existe
    public Usuario buscarPorEmail(String email) throws Exception {
        Usuario u = null;
        abrirBanco();
        try {
            pst = con.prepareStatement(SELECT + "where u.email=?");
            pst.setString(1, email);
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                u = new Usuario();
                copiar(tr, u);
            }
        } finally {
            fecharBanco();
        }
        return u;
    }

    // não engole a exceção como os outros PesquisarTudo: erro de permissão tem que aparecer
    public ArrayList<Usuario> PesquisarTudo() throws Exception {
        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
        abrirBanco();
        try {
            pst = con.prepareStatement(SELECT + "order by u.nome");
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                Usuario u = new Usuario();
                copiar(tr, u);
                usuarios.add(u);
            }
        } finally {
            fecharBanco();
        }
        return usuarios;
    }

    public boolean existeEmail(String email, String ignorarId) throws Exception {
        boolean existe;
        abrirBanco();
        try {
            pst = con.prepareStatement("select id_usuario FROM usuarios where email=? and id_usuario<>?");
            pst.setString(1, email);
            pst.setString(2, ignorarId);
            existe = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return existe;
    }

    public boolean existeGrupo(String nomeGrupo) throws Exception {
        boolean existe;
        abrirBanco();
        try {
            pst = con.prepareStatement("select id_grupo FROM grupos_usuarios where nome_grupo=?");
            pst.setString(1, nomeGrupo);
            existe = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return existe;
    }

    public int contar() throws Exception {
        int total = 0;
        abrirBanco();
        try {
            pst = con.prepareStatement("select count(*) FROM usuarios");
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                total = tr.getInt(1);
            }
        } finally {
            fecharBanco();
        }
        return total;
    }

    private void copiar(ResultSet tr, Usuario u) throws Exception {
        u.setId(tr.getString("id_usuario"));
        u.setNome(tr.getString("nome"));
        u.setEmail(tr.getString("email"));
        u.setSenhaHash(tr.getString("senha_hash"));
        u.setGrupo(GrupoUsuario.deNome(tr.getString("nome_grupo")));
    }
}
