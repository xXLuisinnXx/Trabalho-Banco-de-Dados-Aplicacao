package br.com.entidade.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;

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
