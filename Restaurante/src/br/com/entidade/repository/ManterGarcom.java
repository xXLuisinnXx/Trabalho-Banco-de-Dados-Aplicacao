package br.com.entidade.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import br.com.entidade.DAO;
import br.com.entidade.Garcom;

public class ManterGarcom extends DAO {
     public void inserir(Garcom g) throws Exception {
        abrirBanco();
        try {
            String query = "INSERT INTO garcom (id_garcom,nome,telefone) values(null,?,?)";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, g.getNome());
            pst.setString(2, g.getTelefone());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public void deletarGarcom(Garcom g) throws Exception {
        abrirBanco();
        try {
            String query = "delete from garcom where id_garcom=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, g.getId());
            pst.execute();
        } finally {
            fecharBanco();
        }
    }

    public boolean carregar(Garcom g) throws Exception {
        boolean achou = false;
        abrirBanco();
        try {
            String query = "select id_garcom, nome, telefone FROM garcom where id_garcom=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, g.getId());
            ResultSet tr = pst.executeQuery();
            if (tr.next()) {
                g.setId(tr.getInt("id_garcom"));
                g.setNome(tr.getString("nome"));
                g.setTelefone(tr.getString("telefone"));
                achou = true;
            }
        } finally {
            fecharBanco();
        }
        return achou;
    }

    public ArrayList<Garcom> PesquisarTudo() throws Exception {
        ArrayList<Garcom> garcons = new ArrayList<Garcom>();
        try {
            abrirBanco();
            String query = "select * FROM garcom order by id_garcom";
            pst = (PreparedStatement) con.prepareStatement(query);
            ResultSet tr = pst.executeQuery();
            Garcom g;
            while (tr.next()) {
                g = new Garcom();
                g.setId(tr.getInt("id_garcom"));
                g.setNome(tr.getString("nome"));
                g.setTelefone(tr.getString("telefone"));
                garcons.add(g);
            }
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            fecharBanco();
        }
        return garcons;
    }

    public void editarGarcom(Garcom g) throws Exception {
        abrirBanco();
        try {
            String query = "UPDATE garcom set nome = ?, telefone = ? where id_garcom = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, g.getNome());
            pst.setString(2, g.getTelefone());
            pst.setInt(3, g.getId());
            pst.executeUpdate();
        } finally {
            fecharBanco();
        }
    }

     public boolean possuiPedidos(int idGarcom) throws Exception {
        boolean possui;
        abrirBanco();
        try {
            String query = "select id_pedido FROM pedido where id_garcom=?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, idGarcom);
            possui = pst.executeQuery().next();
        } finally {
            fecharBanco();
        }
        return possui;
    }
}
