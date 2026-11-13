package br.com.visao;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import br.com.controle.ProdutoControle;
import br.com.entidade.Produto;

/**
 * Cardápio exibido direto da view vw_cardapio_completo: a tela não conhece o
 * relacionamento produto x categoria, ela só mostra as linhas que a view devolve.
 */
public class TelaCardapio extends javax.swing.JFrame {

    private final ProdutoControle controle = new ProdutoControle();
    private final DefaultTableModel modelo;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public TelaCardapio() {
        initComponents();
        setSize(520, 400);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel(new Object[]{"Categoria", "Produto", "Preço"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblCardapio.setModel(modelo);
        listar();
    }

    private void listar() {
        modelo.setRowCount(0);
        try {
            for (Produto p : controle.listarCardapio()) {
                modelo.addRow(new Object[]{p.getCategoria().getNome(), p.getNome(),
                    moeda.format(p.getPreco())});
            }
            lblRodape.setText(modelo.getRowCount() + " itens no cardápio");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblOrigem = new javax.swing.JLabel();
        scpCardapio = new javax.swing.JScrollPane();
        tblCardapio = new javax.swing.JTable();
        pnlRodape = new javax.swing.JPanel();
        lblRodape = new javax.swing.JLabel();
        btnAtualizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cardápio");

        lblOrigem.setText("Dados carregados da view vw_cardapio_completo");
        lblOrigem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOrigem.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().add(lblOrigem, java.awt.BorderLayout.NORTH);

        scpCardapio.setViewportView(tblCardapio);
        getContentPane().add(scpCardapio, java.awt.BorderLayout.CENTER);

        pnlRodape.add(lblRodape);

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        pnlRodape.add(btnAtualizar);

        getContentPane().add(pnlRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {
        listar();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JLabel lblOrigem;
    private javax.swing.JLabel lblRodape;
    private javax.swing.JPanel pnlRodape;
    private javax.swing.JScrollPane scpCardapio;
    private javax.swing.JTable tblCardapio;
    // End of variables declaration//GEN-END:variables
}
