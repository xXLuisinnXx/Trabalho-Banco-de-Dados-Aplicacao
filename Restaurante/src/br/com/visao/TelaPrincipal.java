package br.com.visao;

import br.com.controle.AutenticacaoControle;
import br.com.controle.Permissao;
import br.com.controle.Sessao;
import br.com.entidade.Usuario;

/**
 * Menu do sistema. Só abre com alguém logado: os botões aparecem conforme a
 * matriz de Permissao e, no banco, a conexão já é a conta MySQL do grupo do usuário.
 */
public class TelaPrincipal extends javax.swing.JFrame {

    private final AutenticacaoControle autenticacao = new AutenticacaoControle();

    public TelaPrincipal() {
        initComponents();
        mostrarUsuarioLogado();
        aplicarPermissoes();
        pack();
        setSize(Math.max(getWidth(), 460), Math.max(getHeight(), 320));
        setLocationRelativeTo(null); // centraliza
    }

    private void mostrarUsuarioLogado() {
        Usuario u = Sessao.getUsuario();
        lblUsuario.setText(u == null
                ? "Nenhum usuário conectado"
                : "Usuário: " + u.getNome() + "  |  Perfil: " + u.getGrupo()
                        + "  |  Conta MySQL: " + u.getGrupo().getPerfilBanco().getUsuario());
    }

    // esconde o que o perfil não pode usar; o controle e o banco barram de novo
    private void aplicarPermissoes() {
        esconderSemPermissao(btnCardapio, Permissao.CONSULTAR);
        esconderSemPermissao(btnCategorias, Permissao.CONSULTAR);
        esconderSemPermissao(btnGarcons, Permissao.CONSULTAR);
        esconderSemPermissao(btnMesas, Permissao.CONSULTAR);
        esconderSemPermissao(btnProdutos, Permissao.CONSULTAR);
        esconderSemPermissao(btnContasPedidos, Permissao.OPERAR_CONTAS_PEDIDOS);
        esconderSemPermissao(btnRelatorios, Permissao.CONSULTAR_RELATORIOS);
        esconderSemPermissao(btnUsuarios, Permissao.GERENCIAR_USUARIOS);
    }

    private void esconderSemPermissao(javax.swing.JButton botao, Permissao permissao) {
        if (!Sessao.temPermissao(permissao)) {
            pnlMenu.remove(botao); // sai do GridLayout: não deixa buraco no menu
        }
    }

    private void sair() {
        autenticacao.sair(); // volta a conexão para o perfil LOGIN (app_login)
        new TelaLogin().setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblUsuario = new javax.swing.JLabel();
        pnlMenu = new javax.swing.JPanel();
        btnCardapio = new javax.swing.JButton();
        btnCategorias = new javax.swing.JButton();
        btnGarcons = new javax.swing.JButton();
        btnMesas = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnContasPedidos = new javax.swing.JButton();
        btnRelatorios = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        pnlRodape = new javax.swing.JPanel();
        btnSair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Restaurante");

        lblUsuario.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUsuario.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().add(lblUsuario, java.awt.BorderLayout.NORTH);

        pnlMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        pnlMenu.setLayout(new java.awt.GridLayout(0, 2, 10, 10));

        btnCardapio.setText("Cardápio");
        btnCardapio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCardapioActionPerformed(evt);
            }
        });
        pnlMenu.add(btnCardapio);

        btnCategorias.setText("Categorias");
        btnCategorias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategoriasActionPerformed(evt);
            }
        });
        pnlMenu.add(btnCategorias);

        btnGarcons.setText("Garçons");
        btnGarcons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGarconsActionPerformed(evt);
            }
        });
        pnlMenu.add(btnGarcons);

        btnMesas.setText("Mesas");
        btnMesas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMesasActionPerformed(evt);
            }
        });
        pnlMenu.add(btnMesas);

        btnProdutos.setText("Produtos");
        btnProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProdutosActionPerformed(evt);
            }
        });
        pnlMenu.add(btnProdutos);

        btnContasPedidos.setText("Contas / Pedidos");
        btnContasPedidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContasPedidosActionPerformed(evt);
            }
        });
        pnlMenu.add(btnContasPedidos);

        btnRelatorios.setText("Relatórios");
        btnRelatorios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelatoriosActionPerformed(evt);
            }
        });
        pnlMenu.add(btnRelatorios);

        btnUsuarios.setText("Usuários");
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });
        pnlMenu.add(btnUsuarios);

        getContentPane().add(pnlMenu, java.awt.BorderLayout.CENTER);

        btnSair.setText("Sair (trocar de usuário)");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });
        pnlRodape.add(btnSair);

        getContentPane().add(pnlRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCardapioActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaCardapio().setVisible(true);
    }

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

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {
        new TelaUsuario().setVisible(true);
    }

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {
        sair();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // sem login não há menu: o sistema sempre começa pela TelaLogin
                if (Sessao.estaLogado()) {
                    new TelaPrincipal().setVisible(true);
                } else {
                    new TelaLogin().setVisible(true);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCardapio;
    private javax.swing.JButton btnCategorias;
    private javax.swing.JButton btnContasPedidos;
    private javax.swing.JButton btnGarcons;
    private javax.swing.JButton btnMesas;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnRelatorios;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JPanel pnlRodape;
    // End of variables declaration//GEN-END:variables
}
