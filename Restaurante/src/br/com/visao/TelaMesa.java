package br.com.visao;

import br.com.controle.Permissao;
import br.com.controle.Sessao;
import br.com.controle.MesaControle;
import br.com.entidade.Mesa;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaMesa extends javax.swing.JFrame {

    private final MesaControle controle = new MesaControle();
    private final DefaultTableModel modelo;

    public TelaMesa() {
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(null);

        // tabela somente leitura
        modelo = new DefaultTableModel(new Object[]{"ID", "Número", "Capacidade", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblMesas.setModel(modelo);
        // sem permissao os botoes ficam desabilitados; o controle e o banco barram de novo
        btnSalvar.setEnabled(Sessao.temPermissao(Permissao.CADASTRAR_CATALOGO));
        btnExcluir.setEnabled(Sessao.temPermissao(Permissao.EXCLUIR_CADASTRO));
        listar();
    }

    // preenche a tabela
    private void listar() {
        modelo.setRowCount(0);
        try {
            for (Mesa m : controle.listar()) {
                modelo.addRow(new Object[]{m.getId(), m.getNumero(), m.getCapacidade(), m.getSituacao()});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void limpar() {
        txtId.setText("");
        txtNumero.setText("");
        txtCapacidade.setText("");
        tblMesas.clearSelection();
        txtNumero.requestFocus();
    }

    private void aviso(String mensagem) {

        JOptionPane.showMessageDialog(this, mensagem);

    }


    private void mostrarErro(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCampos = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lblNumero = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        lblCapacidade = new javax.swing.JLabel();
        txtCapacidade = new javax.swing.JTextField();
        scpMesas = new javax.swing.JScrollPane();
        tblMesas = new javax.swing.JTable();
        pnlBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Mesas");

        pnlCampos.setLayout(new java.awt.GridLayout(3, 2, 5, 5));

        lblId.setText("ID:");
        pnlCampos.add(lblId);

        txtId.setEnabled(false);
        pnlCampos.add(txtId);

        lblNumero.setText("Número:");
        pnlCampos.add(lblNumero);
        pnlCampos.add(txtNumero);

        lblCapacidade.setText("Capacidade:");
        pnlCampos.add(lblCapacidade);
        pnlCampos.add(txtCapacidade);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.NORTH);

        tblMesas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMesasMouseClicked(evt);
            }
        });
        scpMesas.setViewportView(tblMesas);

        getContentPane().add(scpMesas, java.awt.BorderLayout.CENTER);

        btnNovo.setText("Novo");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });
        pnlBotoes.add(btnNovo);

        btnSalvar.setText("Salvar");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });
        pnlBotoes.add(btnSalvar);

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });
        pnlBotoes.add(btnExcluir);

        getContentPane().add(pnlBotoes, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {
        limpar();
    }

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacidade = Integer.parseInt(txtCapacidade.getText().trim());
            boolean novo = txtId.getText().isEmpty();
            if (novo) {
                controle.cadastrar(numero, capacidade);
            } else {
                controle.atualizar(Integer.parseInt(txtId.getText()), numero, capacidade);
            }
            listar();
            limpar();
            aviso(novo ? "Mesa inserida com sucesso!" : "Mesa alterada com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Número e capacidade devem ser números inteiros.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma mesa na tabela.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "Excluir a mesa selecionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controle.excluir(Integer.parseInt(txtId.getText()));
            listar();
            limpar();
            aviso("Mesa deletada com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // clique na linha preenche campos
    private void tblMesasMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblMesas.getSelectedRow();
        if (linha >= 0) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNumero.setText(modelo.getValueAt(linha, 1).toString());
            txtCapacidade.setText(modelo.getValueAt(linha, 2).toString());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaMesa().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNumero;
    private javax.swing.JLabel lblCapacidade;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JScrollPane scpMesas;
    private javax.swing.JTable tblMesas;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtCapacidade;
    // End of variables declaration//GEN-END:variables
}
