package br.com.visao;

import br.com.controle.Permissao;
import br.com.controle.Sessao;
import br.com.controle.GarcomControle;
import br.com.entidade.Garcom;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class TelaGarcom extends javax.swing.JFrame {

    private final GarcomControle controle = new GarcomControle();
    private final DefaultTableModel modelo;

    public TelaGarcom() {
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(null);

        // tabela somente leitura
        modelo = new DefaultTableModel(new Object[]{"ID", "Nome", "Telefone"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblGarcons.setModel(modelo);
        aplicarMascaraTelefone();
        // sem permissao os botoes ficam desabilitados; o controle e o banco barram de novo
        btnSalvar.setEnabled(Sessao.temPermissao(Permissao.CADASTRAR_CATALOGO));
        btnExcluir.setEnabled(Sessao.temPermissao(Permissao.EXCLUIR_CADASTRO));
        listar();
    }

    // preenche a tabela
    private void listar() {
        modelo.setRowCount(0);
        try {
            for (Garcom g : controle.listar()) {
                modelo.addRow(new Object[]{g.getId(), g.getNome(), g.getTelefone()});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void limpar() {
        txtId.setText("");
        txtNome.setText("");
        txtTelefone.setText("");
        tblGarcons.clearSelection();
        txtNome.requestFocus();
    }

    private void aviso(String mensagem) {

        JOptionPane.showMessageDialog(this, mensagem);

    }


    private void mostrarErro(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }


    // telefone: o usuario digita so os numeros, os parenteses e o traco entram sozinhos
    private void aplicarMascaraTelefone() {
        ((AbstractDocument) txtTelefone.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int desloc, int tamanho, String texto,
                    AttributeSet atributos) throws BadLocationException {
                String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String digitado = atual.substring(0, desloc)
                        + (texto == null ? "" : texto)
                        + atual.substring(desloc + tamanho);
                fb.replace(0, fb.getDocument().getLength(), formatarTelefone(digitado), atributos);
            }

            @Override
            public void remove(FilterBypass fb, int desloc, int tamanho) throws BadLocationException {
                replace(fb, desloc, tamanho, "", null);
            }
        });
    }

    // (61) 99876-5432 para celular, (61) 3321-1234 para fixo
    private static String formatarTelefone(String texto) {
        String numeros = texto.replaceAll("\\D", "");
        if (numeros.length() > 11) {
            numeros = numeros.substring(0, 11);
        }
        if (numeros.isEmpty()) {
            return "";
        }
        StringBuilder saida = new StringBuilder("(");
        saida.append(numeros, 0, Math.min(2, numeros.length()));
        if (numeros.length() > 2) {
            saida.append(") ");
            int corte = numeros.length() > 10 ? 7 : 6;
            if (numeros.length() > corte) {
                saida.append(numeros, 2, corte).append('-').append(numeros.substring(corte));
            } else {
                saida.append(numeros.substring(2));
            }
        }
        return saida.toString();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCampos = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblTelefone = new javax.swing.JLabel();
        txtTelefone = new javax.swing.JTextField();
        scpGarcons = new javax.swing.JScrollPane();
        tblGarcons = new javax.swing.JTable();
        pnlBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Garçons");

        pnlCampos.setLayout(new java.awt.GridLayout(3, 2, 5, 5));

        lblId.setText("ID:");
        pnlCampos.add(lblId);

        txtId.setEnabled(false);
        pnlCampos.add(txtId);

        lblNome.setText("Nome:");
        pnlCampos.add(lblNome);
        pnlCampos.add(txtNome);

        lblTelefone.setText("Telefone:");
        pnlCampos.add(lblTelefone);
        pnlCampos.add(txtTelefone);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.NORTH);

        tblGarcons.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblGarconsMouseClicked(evt);
            }
        });
        scpGarcons.setViewportView(tblGarcons);

        getContentPane().add(scpGarcons, java.awt.BorderLayout.CENTER);

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
            String nome = txtNome.getText();
            String telefone = txtTelefone.getText(); // o controle converte vazio em null
            boolean novo = txtId.getText().isEmpty();
            if (novo) {
                controle.cadastrar(nome, telefone);
            } else {
                controle.atualizar(Integer.parseInt(txtId.getText()), nome, telefone);
            }
            listar();
            limpar();
            aviso(novo ? "Garçom inserido com sucesso!" : "Garçom alterado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um garçom na tabela.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "Excluir o garçom selecionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controle.excluir(Integer.parseInt(txtId.getText()));
            listar();
            limpar();
            aviso("Garçom deletado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // clique na linha preenche campos
    private void tblGarconsMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblGarcons.getSelectedRow();
        if (linha >= 0) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNome.setText(modelo.getValueAt(linha, 1).toString());
            Object tel = modelo.getValueAt(linha, 2);
            txtTelefone.setText(tel == null ? "" : tel.toString());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaGarcom().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblTelefone;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JScrollPane scpGarcons;
    private javax.swing.JTable tblGarcons;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtTelefone;
    // End of variables declaration//GEN-END:variables
}
