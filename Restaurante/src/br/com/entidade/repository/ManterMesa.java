package br.com.entidade.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import br.com.entidade.DAO;
import br.com.entidade.Mesa;
import br.com.entidade.SituacaoMesa;

public class ManterMesa extends DAO {
    public void inserir(Mesa m) throws Exception {
        abrirBanco();
        try {
            String query = "INSERT INTO mesa (id_mesa,numero,capacidade,situacao) values(null,?,?,'LIVRE')";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, m.getNumero());
            pst.setInt(2, m.getCapacidade());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public void deletarMesa(Mesa m) throws Exception {
        abrirBanco();
        try {
            String query = "delete from mesa where id_mesa=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, m.getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Mesa m) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            String query = "select id_mesa, numero, capacidade, situacao FROM mesa where id_mesa=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, m.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                copiar(tr, m);
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

     public ArrayList<Mesa> PesquisarTudo() throws Exception {
        return listar("select * FROM mesa order by numero");
    }

    public ArrayList<Mesa> PesquisarLivres() throws Exception {
        return listar("select * FROM mesa where situacao = 'LIVRE' order by numero");
    }

    public void editarMesa(Mesa m) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE mesa set numero = ?, capacidade = ? where id_mesa = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, m.getNumero());
            pst.setInt(2, m.getCapacidade());
            pst.setInt(3, m.getId());
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

    public boolean existeNumero(int numero, int ignorarId) throws Exception {
        boolean existe;
        abrirBanco();
        try {
            String query = "select id_mesa FROM mesa where numero=? and id_mesa<>?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, numero);
            pst.setInt(2, ignorarId);
            existe = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return existe;
    }

     public boolean possuiContas(int idMesa) throws Exception {
        boolean possui;
        abrirBanco();
        try {
            String query = "select id_conta FROM conta where id_mesa=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, idMesa);
            possui = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return possui;
    }

    private ArrayList<Mesa> listar(String query) throws Exception {
        ArrayList<Mesa> mesas = new ArrayList<Mesa>();
        try {
            abrirBanco();
            pst = (PreparedStatement) con.prepareStatement(query);
            ResultSet tr = pst.executeQuery();
            Mesa m;
            while (tr.next()) {
                m = new Mesa();
                copiar(tr, m);
                mesas.add(m);
            }
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            fecharBanco();
        }
        return mesas;
    }

    private void copiar(ResultSet tr, Mesa m) throws Exception {
        m.setId(tr.getInt("id_mesa"));
        m.setNumero(tr.getInt("numero"));
        m.setCapacidade(tr.getInt("capacidade"));
        m.setSituacao(SituacaoMesa.valueOf(tr.getString("situacao")));
    }
}
