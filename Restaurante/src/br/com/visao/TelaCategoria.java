package br.com.visao;

import br.com.controle.CategoriaControle;
import br.com.entidade.Categoria;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaCategoria extends javax.swing.JFrame {

    private final CategoriaControle controle = new CategoriaControle();
    private final DefaultTableModel modelo;

    public TelaCategoria() {
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(null);

        // tabela somente leitura
        modelo = new DefaultTableModel(new Object[]{"ID", "Nome"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblCategorias.setModel(modelo);
        listar();
    }

    // preenche a tabela
    private void listar() {
        modelo.setRowCount(0);
        try {
            for (Categoria c : controle.listar()) {
                modelo.addRow(new Object[]{c.getId(), c.getNome()});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void limpar() {
        txtId.setText("");
        txtNome.setText("");
        tblCategorias.clearSelection();
        txtNome.requestFocus();
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
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        scpCategorias = new javax.swing.JScrollPane();
        tblCategorias = new javax.swing.JTable();
        pnlBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Categorias");

        pnlCampos.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        lblId.setText("ID:");
        pnlCampos.add(lblId);

        txtId.setEnabled(false);
        pnlCampos.add(txtId);

        lblNome.setText("Nome:");
        pnlCampos.add(lblNome);
        pnlCampos.add(txtNome);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.NORTH);

        tblCategorias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCategoriasMouseClicked(evt);
            }
        });
        scpCategorias.setViewportView(tblCategorias);

        getContentPane().add(scpCategorias, java.awt.BorderLayout.CENTER);

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
            boolean novo = txtId.getText().isEmpty();
            if (novo) {
                controle.cadastrar(txtNome.getText());
            } else {
                controle.atualizar(Integer.parseInt(txtId.getText()), txtNome.getText());
            }
            listar();
            limpar();
            aviso(novo ? "Categoria inserida com sucesso!" : "Categoria alterada com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria na tabela.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "Excluir a categoria selecionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controle.excluir(Integer.parseInt(txtId.getText()));
            listar();
            limpar();
            aviso("Categoria deletada com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // clique na linha preenche campos
    private void tblCategoriasMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblCategorias.getSelectedRow();
        if (linha >= 0) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNome.setText(modelo.getValueAt(linha, 1).toString());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaCategoria().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNome;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JScrollPane scpCategorias;
    private javax.swing.JTable tblCategorias;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
