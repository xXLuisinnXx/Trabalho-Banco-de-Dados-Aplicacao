package br.com.entidade.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import br.com.entidade.Categoria;
import br.com.entidade.DAO;

public class ManterCategoria extends DAO {
    public void inserir(Categoria c) throws Exception {
        abrirBanco();
        try {
            String query = "INSERT INTO categoria (id_categoria,nome) values(null,?)";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, c.getNome());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public void deletarCategoria(Categoria c) throws Exception {
        abrirBanco();
        try {
            String query = "delete from categoria where id_categoria=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, c.getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Categoria c) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            String query = "select id_categoria, nome FROM categoria where id_categoria=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, c.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                c.setId(tr.getInt("id_categoria"));
                c.setNome(tr.getString("nome"));
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

    public ArrayList<Categoria> PesquisarTudo() throws Exception {
        ArrayList<Categoria> categorias = new ArrayList<Categoria>();
        try {
            abrirBanco();
            String query = "select * FROM categoria order by id_categoria";
            pst = (PreparedStatement) con.prepareStatement(query);
            ResultSet tr = pst.executeQuery();
            Categoria c;
            while (tr.next()) {
                c = new Categoria();
                c.setId(tr.getInt("id_categoria"));
                c.setNome(tr.getString("nome"));
                categorias.add(c);
            }
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            fecharBanco();
        }
        return categorias;
    }

    public void editarCategoria(Categoria c) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE categoria set nome = ? where id_categoria = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, c.getNome());
            pst.setInt(2, c.getId());
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

    public boolean existeNome(String nome, int ignorarId) throws Exception {
        boolean existe;
        abrirBanco();
        try {
            String query = "select id_categoria FROM categoria where nome=? and id_categoria<>?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, nome);
            pst.setInt(2, ignorarId);
            existe = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return existe;
    }

    public boolean possuiProdutos(int idCategoria) throws Exception {
        boolean possui;
        abrirBanco();
        try {
            String query = "select id_produto FROM produto where id_categoria=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, idCategoria);
            possui = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return possui;
    }
}
