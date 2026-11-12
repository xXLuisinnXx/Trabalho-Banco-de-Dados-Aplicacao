package br.com.visao;

public class TelaPrincipal extends javax.swing.JFrame {

    public TelaPrincipal() {
        initComponents();
        setSize(420, 280);
        setLocationRelativeTo(null); // centraliza
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCategorias = new javax.swing.JButton();
        btnGarcons = new javax.swing.JButton();
        btnMesas = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnContasPedidos = new javax.swing.JButton();
        btnRelatorios = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Restaurante");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridLayout(3, 2, 10, 10));

        btnCategorias.setText("Categorias");
        btnCategorias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategoriasActionPerformed(evt);
            }
        });
        getContentPane().add(btnCategorias);

        btnGarcons.setText("Garçons");
        btnGarcons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGarconsActionPerformed(evt);
            }
        });
        getContentPane().add(btnGarcons);

        btnMesas.setText("Mesas");
        btnMesas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMesasActionPerformed(evt);
            }
        });
        getContentPane().add(btnMesas);

        btnProdutos.setText("Produtos");
        btnProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProdutosActionPerformed(evt);
            }
        });
        getContentPane().add(btnProdutos);

        btnContasPedidos.setText("Contas / Pedidos");
        btnContasPedidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContasPedidosActionPerformed(evt);
            }
        });
        getContentPane().add(btnContasPedidos);

        btnRelatorios.setText("Relatórios");
        btnRelatorios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelatoriosActionPerformed(evt);
            }
        });
        getContentPane().add(btnRelatorios);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCategoriasActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaCategoria().setVisible(true);
    }

    private void btnGarconsActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaGarcom().setVisible(true);
    }

    private void btnMesasActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaMesa().setVisible(true);
    }

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaProduto().setVisible(true);
    }

    private void btnContasPedidosActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaContaPedido().setVisible(true);
    }

    private void btnRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaRelatorio().setVisible(true);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCategorias;
    private javax.swing.JButton btnContasPedidos;
    private javax.swing.JButton btnGarcons;
    private javax.swing.JButton btnMesas;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnRelatorios;
    // End of variables declaration//GEN-END:variables
}
