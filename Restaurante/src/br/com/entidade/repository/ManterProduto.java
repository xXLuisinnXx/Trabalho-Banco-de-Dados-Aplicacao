package br.com.entidade.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import br.com.entidade.Categoria;
import br.com.entidade.DAO;
import br.com.entidade.Produto;

public class ManterProduto extends DAO {
    private static final String SELECT = "select p.id_produto, p.nome, p.preco, "
            + "c.id_categoria, c.nome as nome_categoria "
            + "FROM produto p inner join categoria c on c.id_categoria = p.id_categoria ";

    public void inserir(Produto p) throws Exception {
        abrirBanco();
        try {
            String query = "INSERT INTO produto (id_produto,nome,preco,id_categoria) values(null,?,?,?)";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, p.getNome());
            pst.setBigDecimal(2, p.getPreco());
            pst.setInt(3, p.getCategoria().getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public void deletarProduto(Produto p) throws Exception {
        abrirBanco();
        try {
            String query = "delete from produto where id_produto=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, p.getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Produto p) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            pst = (PreparedStatement) con.prepareStatement(SELECT + "where p.id_produto=?");
            pst.setInt(1, p.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                copiar(tr, p);
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

    public ArrayList<Produto> PesquisarTudo() throws Exception {
        ArrayList<Produto> produtos = new ArrayList<Produto>();
        try {
            abrirBanco();
            pst = (PreparedStatement) con.prepareStatement(SELECT + "order by p.id_produto");
            ResultSet tr = pst.executeQuery();
            Produto p;
            while (tr.next()) {
                p = new Produto();
                copiar(tr, p);
                produtos.add(p);
            }
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            fecharBanco();
        }
        return produtos;
    }

    public void editarProduto(Produto p) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE produto set nome = ?, preco = ?, id_categoria = ? where id_produto = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, p.getNome());
            pst.setBigDecimal(2, p.getPreco());
            pst.setInt(3, p.getCategoria().getId());
            pst.setInt(4, p.getId());
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

    public boolean possuiItensPedido(int idProduto) throws Exception {
        boolean possui;
        abrirBanco();
        try {
            String query = "select id_item FROM item_pedido where id_produto=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, idProduto);
            possui = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return possui;
    }

    // Cardápio lido direto da view vw_cardapio_completo: a junção produto x categoria
    // fica no banco e a aplicação só exibe o que a view entrega.
    public ArrayList<Produto> listarCardapio() throws Exception {
        ArrayList<Produto> cardapio = new ArrayList<Produto>();
        abrirBanco();
        try {
            pst = con.prepareStatement("select id_produto, produto, preco, categoria "
                    + "FROM vw_cardapio_completo order by categoria, produto");
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                Categoria c = new Categoria();
                c.setNome(tr.getString("categoria")); // a view entrega só o nome da categoria

                Produto p = new Produto();
                p.setId(tr.getInt("id_produto"));
                p.setNome(tr.getString("produto"));
                p.setPreco(tr.getBigDecimal("preco"));
                p.setCategoria(c);
                cardapio.add(p);
            }
        } finally {
            fecharBanco();
        }
        return cardapio;
    }

    private void copiar(ResultSet tr, Produto p) throws Exception {
        Categoria c = new Categoria();
        c.setId(tr.getInt("id_categoria"));
        c.setNome(tr.getString("nome_categoria"));
        p.setId(tr.getInt("id_produto"));
        p.setNome(tr.getString("nome"));
        p.setPreco(tr.getBigDecimal("preco"));
        p.setCategoria(c);
    } 
}
