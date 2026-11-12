package br.com.entidade.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

import br.com.entidade.Conta;
import br.com.entidade.DAO;
import br.com.entidade.Garcom;
import br.com.entidade.ItemPedido;
import br.com.entidade.Mesa;
import br.com.entidade.Pedido;
import br.com.entidade.Produto;
import br.com.entidade.SituacaoConta;
import br.com.entidade.SituacaoMesa;
import br.com.entidade.SituacaoPedido;

public class ManterPedido extends DAO {

    private static final String SELECT = "select p.id_pedido, p.data_hora, p.situacao, p.total, "
            + "g.id_garcom, g.nome as nome_garcom, g.telefone, "
            + "c.id_conta, c.data_abertura, c.data_fechamento, c.situacao as situacao_conta, "
            + "c.total as total_conta, "
            + "m.id_mesa, m.numero, m.capacidade, m.situacao as situacao_mesa "
            + "FROM pedido p "
            + "inner join garcom g on g.id_garcom = p.id_garcom "
            + "inner join conta c on c.id_conta = p.id_conta "
            + "inner join mesa m on m.id_mesa = c.id_mesa ";

    public void inserir(Pedido p) throws Exception {
        abrirBanco();
        try {
            con.setAutoCommit(false);
            gravarPedido(p);
            gravarItens(p);
            con.commit();
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            fecharBanco();
        }
    }

    public void inserirAbrindoConta(Pedido p) throws Exception {
        abrirBanco();
        try {
            con.setAutoCommit(false);
            Conta conta = p.getConta();

            // A trigger trg_conta_ai já marca a mesa como OCUPADA, e o UNIQUE
            // uq_conta_aberta_por_mesa impede duas contas abertas na mesma mesa,
            // mesmo com duas telas abrindo ao mesmo tempo. Por isso não há mais
            // UPDATE mesa aqui (ele nunca achava a mesa LIVRE e derrubava o pedido).
            String queryConta = "INSERT INTO conta (id_conta,id_mesa,data_abertura,situacao,total) "
                    + "values(null,?,?,'ABERTA',0)";
            pst = con.prepareStatement(queryConta, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, conta.getMesa().getId());
            pst.setTimestamp(2, Timestamp.valueOf(conta.getDataAbertura()));
            try {
                pst.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // chave duplicada: a mesa já tem conta aberta
                    throw new Exception("A mesa já está ocupada.");
                }
                throw e;
            }
            ResultSet chave = pst.getGeneratedKeys();
            if (chave.next()) {
                conta.setId(chave.getInt(1));
            }

            gravarPedido(p);
            gravarItens(p);
            con.commit();
            conta.getMesa().setSituacao(SituacaoMesa.OCUPADA);
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            fecharBanco();
        }
    }

    // Pedido em conta que já existe: procedure registrar_pedido (FOR UPDATE na conta,
    // confere se está ABERTA e grava pedido e itens numa transação só)
    public void registrarPorProcedure(Pedido p) throws Exception {
        abrirBanco();
        try {
            CallableStatement cs = con.prepareCall("{call registrar_pedido(?, ?, ?, ?)}");
            pst = cs; // fecharBanco() fecha
            cs.setInt(1, p.getConta().getId());
            cs.setInt(2, p.getGarcom().getId());
            cs.setString(3, itensEmJson(p));
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            p.setId(cs.getInt(4));
        } finally {
            fecharBanco();
        }
    }

    private String itensEmJson(Pedido p) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < p.getItens().size(); i++) {
            ItemPedido item = p.getItens().get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"id_produto\":").append(item.getProduto().getId())
                    .append(",\"quantidade\":").append(item.getQuantidade()).append('}');
        }
        return json.append(']').toString();
    }

    public void editarPedido(Pedido p) throws Exception {
        abrirBanco();
        try {
            con.setAutoCommit(false);

            // pedido.total é recalculado pelas triggers trg_item_pedido_* a cada item
            // removido/gravado: o UPDATE do total que existia aqui ficou redundante
            String queryApagar = "delete from item_pedido where id_pedido = ?";
            pst = con.prepareStatement(queryApagar);
            pst.setInt(1, p.getId());
            pst.executeUpdate();

            gravarItens(p);
            con.commit();
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            fecharBanco();
        }
    }

    public void atualizarSituacao(int idPedido, SituacaoPedido situacao) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE pedido set situacao = ? where id_pedido = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, situacao.name());
            pst.setInt(2, idPedido);
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

    public void deletarPedido(Pedido p) throws Exception {
        abrirBanco();
        try {
            con.setAutoCommit(false);

            String queryItens = "delete from item_pedido where id_pedido=?";
            pst = (PreparedStatement) con.prepareStatement(queryItens);
            pst.setInt(1, p.getId());
            pst.execute();

            String queryPedido = "delete from pedido where id_pedido=?";
            pst = (PreparedStatement) con.prepareStatement(queryPedido);
            pst.setInt(1, p.getId());
            pst.execute();

            con.commit();
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Pedido p) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            pst = (PreparedStatement) con.prepareStatement(SELECT + "where p.id_pedido=?");
            pst.setInt(1, p.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                Pedido lido = lerPedido(tr);
                p.setId(lido.getId());
                p.setConta(lido.getConta());
                p.setGarcom(lido.getGarcom());
                p.setDataHora(lido.getDataHora());
                p.setSituacao(lido.getSituacao());
                p.setTotal(lido.getTotal());
                carregarItens(p);
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

    public ArrayList<Pedido> PesquisarTudo() throws Exception {
        ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
        try {
            abrirBanco();
            pst = (PreparedStatement) con.prepareStatement(SELECT + "order by p.id_pedido desc");
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                pedidos.add(lerPedido(tr));
            }
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            fecharBanco();
        }
        return pedidos;
    }

    // Não carrega os itens: nenhum chamador usa (a tela busca os itens do pedido
    // selecionado) e o total vem de pedido.total, mantido pelas triggers.
    // Antes era 1 consulta + 1 por pedido (N+1).
    public ArrayList<Pedido> PesquisarPorConta(int idConta) throws Exception {
        ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
        abrirBanco();
        try {
            pst = con.prepareStatement(
                    SELECT + "where p.id_conta=? order by p.id_pedido");
            pst.setInt(1, idConta);
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                pedidos.add(lerPedido(tr));
            }
        } finally {
            fecharBanco();
        }
        return pedidos;
    }

    public ArrayList<Pedido> PesquisarPorPeriodo(LocalDateTime inicio, LocalDateTime fimExclusivo,
            Integer idGarcom, Integer idMesa, SituacaoPedido situacao) throws Exception {
        ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
        abrirBanco();
        try {
            StringBuilder query = new StringBuilder(SELECT);
            query.append("where p.data_hora >= ? and p.data_hora < ? ");
            if (idGarcom != null) {
                query.append("and p.id_garcom = ? ");
            }
            if (idMesa != null) {
                query.append("and m.id_mesa = ? ");
            }
            if (situacao != null) {
                query.append("and p.situacao = ? ");
            }
            query.append("order by p.data_hora");

            pst = (PreparedStatement) con.prepareStatement(query.toString());
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(inicio));
            pst.setTimestamp(i++, Timestamp.valueOf(fimExclusivo));
            if (idGarcom != null) {
                pst.setInt(i++, idGarcom);
            }
            if (idMesa != null) {
                pst.setInt(i++, idMesa);
            }
            if (situacao != null) {
                pst.setString(i++, situacao.name());
            }
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                pedidos.add(lerPedido(tr));
            }
        } finally {
            fecharBanco();
        }
        return pedidos;
    }

    private void gravarPedido(Pedido p) throws Exception {
        String query = "INSERT INTO pedido (id_pedido,id_conta,id_garcom,data_hora,situacao,total) "
                + "values(null,?,?,?,?,?)";
        pst = (PreparedStatement) con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, p.getConta().getId());
        pst.setInt(2, p.getGarcom().getId());
        pst.setTimestamp(3, Timestamp.valueOf(p.getDataHora()));
        pst.setString(4, p.getSituacao().name());
        pst.setBigDecimal(5, p.getTotal());
        pst.executeUpdate();
        ResultSet chave = pst.getGeneratedKeys();
        if (chave.next()) {
            p.setId(chave.getInt(1));
        }
    }

    private void gravarItens(Pedido p) throws Exception {
        String query = "INSERT INTO item_pedido (id_item,id_pedido,id_produto,quantidade,preco_unitario) "
                + "values(null,?,?,?,?)";
        pst = con.prepareStatement(query); // preparado uma vez só, itens em lote
        for (ItemPedido item : p.getItens()) {
            pst.setInt(1, p.getId());
            pst.setInt(2, item.getProduto().getId());
            pst.setInt(3, item.getQuantidade());
            pst.setBigDecimal(4, item.getPrecoUnitario());
            pst.addBatch();
        }
        pst.executeBatch();
    }

    private void carregarItens(Pedido p) throws Exception {
        String query = "select i.id_item, i.quantidade, i.preco_unitario, "
                + "pr.id_produto, pr.nome, pr.preco "
                + "FROM item_pedido i inner join produto pr on pr.id_produto = i.id_produto "
                + "where i.id_pedido=? order by i.id_item";
        PreparedStatement ps = (PreparedStatement) con.prepareStatement(query);
        ps.setInt(1, p.getId());
        ResultSet tr = ps.executeQuery();
        ArrayList<ItemPedido> itens = new ArrayList<ItemPedido>();
        while (tr.next()) {
            Produto produto = new Produto();
            produto.setId(tr.getInt("id_produto"));
            produto.setNome(tr.getString("nome"));
            produto.setPreco(tr.getBigDecimal("preco"));

            ItemPedido item = new ItemPedido();
            item.setId(tr.getInt("id_item"));
            item.setProduto(produto);
            item.setQuantidade(tr.getInt("quantidade"));
            item.setPrecoUnitario(tr.getBigDecimal("preco_unitario"));
            itens.add(item);
        }
        BigDecimal totalGravado = p.getTotal();
        p.setItens(itens); 
        if (p.getTotal().compareTo(totalGravado) != 0) {
            throw new Exception("Inconsistência: total do pedido diverge dos itens.");
}
        p.setTotal(totalGravado); 
    }

    private Pedido lerPedido(ResultSet tr) throws Exception {
        Mesa mesa = new Mesa();
        mesa.setId(tr.getInt("id_mesa"));
        mesa.setNumero(tr.getInt("numero"));
        mesa.setCapacidade(tr.getInt("capacidade"));
        mesa.setSituacao(SituacaoMesa.valueOf(tr.getString("situacao_mesa")));

        Conta conta = new Conta();
        conta.setId(tr.getInt("id_conta"));
        conta.setMesa(mesa);
        conta.setDataAbertura(tr.getTimestamp("data_abertura").toLocalDateTime());
        Timestamp fechamento = tr.getTimestamp("data_fechamento");
        if (fechamento != null) {
            conta.setDataFechamento(fechamento.toLocalDateTime());
        }
        conta.setSituacao(SituacaoConta.valueOf(tr.getString("situacao_conta")));
        conta.setTotal(tr.getBigDecimal("total_conta"));

        Garcom garcom = new Garcom();
        garcom.setId(tr.getInt("id_garcom"));
        garcom.setNome(tr.getString("nome_garcom"));
        garcom.setTelefone(tr.getString("telefone"));

        Pedido pedido = new Pedido();
        pedido.setId(tr.getInt("id_pedido"));
        pedido.setConta(conta);
        pedido.setGarcom(garcom);
        pedido.setDataHora(tr.getTimestamp("data_hora").toLocalDateTime());
        pedido.setSituacao(SituacaoPedido.valueOf(tr.getString("situacao")));
        pedido.setTotal(tr.getBigDecimal("total"));
        return pedido;
    }
}
