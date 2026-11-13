package br.com.visao;

import javax.swing.JOptionPane;

import br.com.controle.AutenticacaoControle;

/**
 * Porta de entrada do sistema. Enquanto ninguém entra, o DAO usa o perfil LOGIN
 * (conta MySQL app_login, que só lê usuarios e grupos_usuarios). Depois do login,
 * a Sessao troca a conexão para a conta MySQL do grupo do usuário.
 */
public class TelaLogin extends javax.swing.JFrame {

    private final AutenticacaoControle autenticacao = new AutenticacaoControle();

    public TelaLogin() {
        initComponents();
        setSize(380, 210);
        setLocationRelativeTo(null); // centraliza
        getRootPane().setDefaultButton(btnEntrar); // Enter já tenta entrar
        txtEmail.requestFocus();
    }

    private void entrar() {
        try {
            autenticacao.entrar(txtEmail.getText(), new String(txtSenha.getPassword()));
            new TelaPrincipal().setVisible(true);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            txtSenha.setText("");
            txtSenha.requestFocus();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        pnlCampos = new javax.swing.JPanel();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblSenha = new javax.swing.JLabel();
        txtSenha = new javax.swing.JPasswordField();
        pnlBotoes = new javax.swing.JPanel();
        btnEntrar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Restaurante - Acesso");
        setResizable(false);

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(lblTitulo.getFont().getSize() + 4f));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Identifique-se para continuar");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(lblTitulo, java.awt.BorderLayout.NORTH);

        pnlCampos.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        pnlCampos.setLayout(new java.awt.GridLayout(2, 2, 5, 8));

        lblEmail.setText("E-mail:");
        pnlCampos.add(lblEmail);
        pnlCampos.add(txtEmail);

        lblSenha.setText("Senha:");
        pnlCampos.add(lblSenha);
        pnlCampos.add(txtSenha);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.CENTER);

        btnEntrar.setText("Entrar");
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });
        pnlBotoes.add(btnEntrar);

        btnCancelar.setText("Sair");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        pnlBotoes.add(btnCancelar);

        getContentPane().add(pnlBotoes, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEntrarActionPerformed(java.awt.event.ActionEvent evt) {
        entrar();
    }

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
        System.exit(0);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaLogin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEntrar;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JPasswordField txtSenha;
    // End of variables declaration//GEN-END:variables
}
