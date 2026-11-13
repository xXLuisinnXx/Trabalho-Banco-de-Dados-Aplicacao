package br.com.entidade.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

import br.com.entidade.Conta;
import br.com.entidade.DAO;
import br.com.entidade.Mesa;
import br.com.entidade.SituacaoConta;
import br.com.entidade.SituacaoMesa;

public class ManterConta extends DAO {
    public Conta buscarAbertaPorMesa(Mesa mesa) throws Exception {
        Conta conta = null;
        abrirBanco();
        try {
            String query = "select id_conta, data_abertura, situacao, total "
                    + "FROM conta where id_mesa=? and situacao='ABERTA'";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, mesa.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                conta = new Conta();
                conta.setId(tr.getInt("id_conta"));
                conta.setMesa(mesa);
                conta.setDataAbertura(tr.getTimestamp("data_abertura").toLocalDateTime());
                conta.setSituacao(SituacaoConta.valueOf(tr.getString("situacao")));
                conta.setTotal(tr.getBigDecimal("total"));
            }
        } finally {
            fecharBanco();
        }
        return conta;
    }

    // Total parcial pela view vw_contas_abertas (usa calcular_total_conta):
    // uma consulta só, sem carregar os pedidos. Devolve null se a mesa não tem conta aberta.
    public BigDecimal totalParcialPorMesa(int idMesa) throws Exception {
        BigDecimal total = null;
        abrirBanco();
        try {
            pst = con.prepareStatement("select total_parcial FROM vw_contas_abertas where id_mesa=?");
            pst.setInt(1, idMesa);
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                total = tr.getBigDecimal("total_parcial");
            }
        } finally {
            fecharBanco();
        }
        return total;
    }

    // Todas as contas abertas, lidas da view vw_contas_abertas. O total parcial de cada
    // uma já vem calculado pela function calcular_total_conta, que a view usa.
    public ArrayList<Conta> listarAbertas() throws Exception {
        ArrayList<Conta> contas = new ArrayList<Conta>();
        abrirBanco();
        try {
            pst = con.prepareStatement("select id_conta, id_mesa, numero_mesa, data_abertura, "
                    + "total_parcial FROM vw_contas_abertas order by numero_mesa");
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
                Mesa mesa = new Mesa();
                mesa.setId(tr.getInt("id_mesa"));
                mesa.setNumero(tr.getInt("numero_mesa"));
                mesa.setSituacao(SituacaoMesa.OCUPADA); // conta aberta implica mesa ocupada

                Conta conta = new Conta();
                conta.setId(tr.getInt("id_conta"));
                conta.setMesa(mesa);
                conta.setDataAbertura(tr.getTimestamp("data_abertura").toLocalDateTime());
                conta.setSituacao(SituacaoConta.ABERTA);
                conta.setTotal(tr.getBigDecimal("total_parcial"));
                contas.add(conta);
            }
        } finally {
            fecharBanco();
        }
        return contas;
    }

    // Histórico de contas de um período. Filtra e ordena por conta.data_abertura, que é
    // exatamente o que o índice idx_conta_data_abertura cobre, e pede o valor de cada
    // conta à function calcular_total_conta em vez de somar os pedidos na aplicação.
    public ArrayList<Conta> pesquisarPorPeriodo(LocalDateTime inicio, LocalDateTime fimExclusivo)
            throws Exception {
        ArrayList<Conta> contas = new ArrayList<Conta>();
        abrirBanco();
        try {
            String query = "select c.id_conta, c.data_abertura, c.data_fechamento, c.situacao, "
                    + "calcular_total_conta(c.id_conta) as total_calculado, "
                    + "m.id_mesa, m.numero, m.capacidade, m.situacao as situacao_mesa "
                    + "FROM conta c inner join mesa m on m.id_mesa = c.id_mesa "
                    + "where c.data_abertura >= ? and c.data_abertura < ? "
                    + "order by c.data_abertura";
            pst = con.prepareStatement(query);
            pst.setTimestamp(1, Timestamp.valueOf(inicio));
            pst.setTimestamp(2, Timestamp.valueOf(fimExclusivo));
            ResultSet tr = pst.executeQuery();
            while (tr.next()) {
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
                conta.setSituacao(SituacaoConta.valueOf(tr.getString("situacao")));
                conta.setTotal(tr.getBigDecimal("total_calculado"));
                contas.add(conta);
            }
        } finally {
            fecharBanco();
        }
        return contas;
    }

    // Fecha pela procedure fechar_conta: ela trava a conta, confere se há pedido
    // EM_PREPARO, calcula o total, decide FECHADA/CANCELADA e a trigger trg_conta_au
    // libera a mesa. Tudo numa transação do banco (antes eram 2 UPDATEs feitos aqui).
    public void fecharConta(Conta c) throws Exception {
        abrirBanco();
        try {
            CallableStatement cs = con.prepareCall("{call fechar_conta(?, ?, ?)}");
            pst = cs; // fecharBanco() fecha
            cs.setInt(1, c.getId());
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            c.setTotal(cs.getBigDecimal(2));
            c.setSituacao(SituacaoConta.valueOf(cs.getString(3)));
            c.setDataFechamento(LocalDateTime.now()); // aproximação: o banco gravou NOW()
            c.getMesa().setSituacao(SituacaoMesa.LIVRE);
        } finally {
            fecharBanco();
        }
    }
}
