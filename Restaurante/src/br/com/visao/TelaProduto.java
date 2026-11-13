package br.com.visao;

import br.com.controle.Permissao;
import br.com.controle.Sessao;
import br.com.controle.CategoriaControle;
import br.com.controle.ProdutoControle;
import br.com.entidade.Categoria;
import br.com.entidade.Produto;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaProduto extends javax.swing.JFrame {

    private final ProdutoControle controle = new ProdutoControle();
    private final CategoriaControle categoriaControle = new CategoriaControle();
    private final DefaultTableModel modelo;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private List<Produto> produtos = new ArrayList<>(); // mesma ordem da tabela

    public TelaProduto() {
        initComponents();
        setSize(600, 450);
        setLocationRelativeTo(null);

        // tabela somente leitura
        modelo = new DefaultTableModel(new Object[]{"ID", "Nome", "Preço", "Categoria"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblProdutos.setModel(modelo);
        carregarCategorias();
        // sem permissao os botoes ficam desabilitados; o controle e o banco barram de novo
        btnSalvar.setEnabled(Sessao.temPermissao(Permissao.CADASTRAR_CATALOGO));
        btnExcluir.setEnabled(Sessao.temPermissao(Permissao.EXCLUIR_CADASTRO));
        listar();
    }

    // preenche o ComboBox
    private void carregarCategorias() {
        cmbCategoria.removeAllItems();
        try {
            for (Categoria c : categoriaControle.listar()) {
                cmbCategoria.addItem(c);
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
        cmbCategoria.setSelectedIndex(-1);
    }

    // preenche a tabela
    private void listar() {
        modelo.setRowCount(0);
        try {
            produtos = controle.listar();
            for (Produto p : produtos) {
                modelo.addRow(new Object[]{p.getId(), p.getNome(),
                    moeda.format(p.getPreco()), p.getCategoria().getNome()});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void limpar() {
        txtId.setText("");
        txtNome.setText("");
        txtPreco.setText("");
        cmbCategoria.setSelectedIndex(-1);
        tblProdutos.clearSelection();
        txtNome.requestFocus();
    }

    private void aviso(String mensagem) {

        JOptionPane.showMessageDialog(this, mensagem);

    }


    private void mostrarErro(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // seleciona categoria pelo id
    private void selecionarCategoria(int id) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == id) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCampos = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblPreco = new javax.swing.JLabel();
        txtPreco = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        scpProdutos = new javax.swing.JScrollPane();
        tblProdutos = new javax.swing.JTable();
        pnlBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Produtos");

        pnlCampos.setLayout(new java.awt.GridLayout(4, 2, 5, 5));

        lblId.setText("ID:");
        pnlCampos.add(lblId);

        txtId.setEnabled(false);
        pnlCampos.add(txtId);

        lblNome.setText("Nome:");
        pnlCampos.add(lblNome);
        pnlCampos.add(txtNome);

        lblPreco.setText("Preço:");
        pnlCampos.add(lblPreco);
        pnlCampos.add(txtPreco);

        lblCategoria.setText("Categoria:");
        pnlCampos.add(lblCategoria);
        pnlCampos.add(cmbCategoria);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.NORTH);

        tblProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProdutosMouseClicked(evt);
            }
        });
        scpProdutos.setViewportView(tblProdutos);

        getContentPane().add(scpProdutos, java.awt.BorderLayout.CENTER);

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
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
            if (categoria == null) {
                throw new IllegalArgumentException("Selecione uma categoria.");
            }
            String nome = txtNome.getText();
            BigDecimal preco = new BigDecimal(txtPreco.getText().trim().replace(",", "."));
            boolean novo = txtId.getText().isEmpty();
            if (novo) {
                controle.cadastrar(nome, preco, categoria.getId());
            } else {
                controle.atualizar(Integer.parseInt(txtId.getText()), nome, preco, categoria.getId());
            }
            listar();
            limpar();
            aviso(novo ? "Produto inserido com sucesso!" : "Produto alterado com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Exemplo: 12,50.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "Excluir o produto selecionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controle.excluir(Integer.parseInt(txtId.getText()));
            listar();
            limpar();
            aviso("Produto deletado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // clique na linha preenche campos
    private void tblProdutosMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblProdutos.getSelectedRow();
        if (linha >= 0 && linha < produtos.size()) {
            Produto p = produtos.get(linha);
            txtId.setText(String.valueOf(p.getId()));
            txtNome.setText(p.getNome());
            txtPreco.setText(p.getPreco().toPlainString().replace('.', ','));
            selecionarCategoria(p.getCategoria().getId());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaProduto().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<Categoria> cmbCategoria;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblPreco;
    private javax.swing.JPanel pnlBotoes;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JScrollPane scpProdutos;
    private javax.swing.JTable tblProdutos;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtPreco;
    // End of variables declaration//GEN-END:variables
}
