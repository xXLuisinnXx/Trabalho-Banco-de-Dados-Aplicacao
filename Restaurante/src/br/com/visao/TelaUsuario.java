package br.com.visao;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import br.com.controle.Permissao;
import br.com.controle.Sessao;
import br.com.controle.UsuarioControle;
import br.com.entidade.GrupoUsuario;
import br.com.entidade.Usuario;

/**
 * Cadastro de usuários do sistema (tabelas usuarios e grupos_usuarios).
 * Só o grupo ADMIN chega aqui: o UsuarioControle exige GERENCIAR_USUARIOS em toda
 * operação e, no banco, a conexão é a conta admin_user.
 */
public class TelaUsuario extends javax.swing.JFrame {

    private final UsuarioControle controle = new UsuarioControle();
    private final DefaultTableModel modelo;

    public TelaUsuario() {
        initComponents();
        setSize(620, 420);
        setLocationRelativeTo(null);

        // tabela somente leitura (a senha nunca é exibida: o banco guarda só o hash)
        modelo = new DefaultTableModel(new Object[]{"ID", "Nome", "E-mail", "Perfil"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblUsuarios.setModel(modelo);

        for (GrupoUsuario grupo : GrupoUsuario.values()) {
            cmbGrupo.addItem(grupo);
        }
        cmbGrupo.setSelectedItem(GrupoUsuario.GARCOM);

        // segunda barreira: mesmo que a tela seja aberta direto, sem permissão não salva
        boolean podeGerenciar = Sessao.temPermissao(Permissao.GERENCIAR_USUARIOS);
        btnSalvar.setEnabled(podeGerenciar);
        btnExcluir.setEnabled(podeGerenciar);

        listar();
    }

    // preenche a tabela
    private void listar() {
        modelo.setRowCount(0);
        try {
            for (Usuario u : controle.listar()) {
                modelo.addRow(new Object[]{u.getId(), u.getNome(), u.getEmail(), u.getGrupo()});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void limpar() {
        txtId.setText("");
        txtNome.setText("");
        txtEmail.setText("");
        txtSenha.setText("");
        cmbGrupo.setSelectedItem(GrupoUsuario.GARCOM);
        tblUsuarios.clearSelection();
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

        pnlTopo = new javax.swing.JPanel();
        pnlCampos = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblGrupo = new javax.swing.JLabel();
        cmbGrupo = new javax.swing.JComboBox<>();
        lblSenha = new javax.swing.JLabel();
        txtSenha = new javax.swing.JPasswordField();
        lblDica = new javax.swing.JLabel();
        scpUsuarios = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        pnlBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Usuários");

        pnlTopo.setLayout(new java.awt.BorderLayout());

        pnlCampos.setLayout(new java.awt.GridLayout(5, 2, 5, 5));

        lblId.setText("ID:");
        pnlCampos.add(lblId);

        txtId.setEnabled(false); // vem da função gerar_id_usuario() do banco
        pnlCampos.add(txtId);

        lblNome.setText("Nome:");
        pnlCampos.add(lblNome);
        pnlCampos.add(txtNome);

        lblEmail.setText("E-mail:");
        pnlCampos.add(lblEmail);
        pnlCampos.add(txtEmail);

        lblGrupo.setText("Perfil de acesso:");
        pnlCampos.add(lblGrupo);
        pnlCampos.add(cmbGrupo);

        lblSenha.setText("Senha:");
        pnlCampos.add(lblSenha);
        pnlCampos.add(txtSenha);

        pnlTopo.add(pnlCampos, java.awt.BorderLayout.CENTER);

        lblDica.setText("Mínimo de 8 caracteres, com letras e números. "
                + "Ao alterar, deixe em branco para manter a senha atual.");
        lblDica.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pnlTopo.add(lblDica, java.awt.BorderLayout.SOUTH);

        getContentPane().add(pnlTopo, java.awt.BorderLayout.NORTH);

        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        scpUsuarios.setViewportView(tblUsuarios);

        getContentPane().add(scpUsuarios, java.awt.BorderLayout.CENTER);

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
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());
            GrupoUsuario grupo = (GrupoUsuario) cmbGrupo.getSelectedItem();
            boolean novo = txtId.getText().isEmpty();
            if (novo) {
                controle.cadastrar(nome, email, senha, grupo);
            } else {
                // senha em branco mantém o hash que já está no banco
                controle.atualizar(txtId.getText(), nome, email, grupo, senha);
            }
            listar();
            limpar();
            aviso(novo ? "Usuário inserido com sucesso!" : "Usuário alterado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "Excluir o usuário selecionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controle.excluir(txtId.getText());
            listar();
            limpar();
            aviso("Usuário deletado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // clique na linha preenche campos (a senha fica em branco: o hash não volta para a tela)
    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblUsuarios.getSelectedRow();
        if (linha >= 0) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNome.setText(modelo.getValueAt(linha, 1).toString());
            txtEmail.setText(modelo.getValueAt(linha, 2).toString());
            cmbGrupo.setSelectedItem(modelo.getValueAt(linha, 3));
            txtSenha.setText("");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<GrupoUsuario> cmbGrupo;
    private javax.swing.JLabel lblDica;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblGrupo;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JPanel pnlTopo;
    private javax.swing.JScrollPane scpUsuarios;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNome;
    private javax.swing.JPasswordField txtSenha;
    // End of variables declaration//GEN-END:variables
}
