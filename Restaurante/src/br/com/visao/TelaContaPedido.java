package br.com.visao;

import br.com.controle.ContaControle;
import br.com.controle.GarcomControle;
import br.com.controle.MesaControle;
import br.com.controle.PedidoControle;
import br.com.controle.ProdutoControle;
import br.com.controle.RegraNegocioException;
import br.com.entidade.Conta;
import br.com.entidade.Garcom;
import br.com.entidade.ItemPedido;
import br.com.entidade.Mesa;
import br.com.entidade.Pedido;
import br.com.entidade.Produto;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaContaPedido extends javax.swing.JFrame {

    private final PedidoControle pedidoControle = new PedidoControle();
    private final ContaControle contaControle = new ContaControle();
    private final MesaControle mesaControle = new MesaControle();
    private final GarcomControle garcomControle = new GarcomControle();
    private final ProdutoControle produtoControle = new ProdutoControle();

    private final DefaultTableModel modeloNovo = criarModelo("Produto", "Qtd", "Preço", "Subtotal");
    private final DefaultTableModel modeloPedidos = criarModelo("ID", "Garçom", "Hora", "Situação", "Total");
    private final DefaultTableModel modeloItens = criarModelo("Produto", "Qtd", "Preço", "Subtotal");
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final Pedido novoPedido = new Pedido(); // pedido em montagem: agrupa item repetido
    private List<Pedido> pedidos = new ArrayList<>(); // mesma ordem da tabela
    private List<ItemPedido> itensPedido = new ArrayList<>(); // itens do pedido selecionado
    private boolean carregando = false; // evita evento ao carregar combos

    public TelaContaPedido() {
        initComponents();
        setSize(1150, 650);
        setLocationRelativeTo(null);
        cmbMesa.setPreferredSize(new java.awt.Dimension(100, 25));
        cmbGarcom.setPreferredSize(new java.awt.Dimension(150, 25));
        cmbProduto.setPreferredSize(new java.awt.Dimension(200, 25));
        txtQtd.setPreferredSize(new java.awt.Dimension(50, 25));
        txtQtd.setText("1");
        tblNovo.setModel(modeloNovo);
        tblPedidos.setModel(modeloPedidos);
        tblItens.setModel(modeloItens);
        carregarCombos();
    }

    // tabela somente leitura
    private static DefaultTableModel criarModelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    // preenche os ComboBox
    private void carregarCombos() {
        carregando = true;
        try {
            for (Mesa m : mesaControle.listar()) {
                cmbMesa.addItem(m);
            }
            for (Garcom g : garcomControle.listar()) {
                cmbGarcom.addItem(g);
            }
            for (Produto p : produtoControle.listar()) {
                cmbProduto.addItem(p);
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
        cmbMesa.setSelectedIndex(-1);
        cmbGarcom.setSelectedIndex(-1);
        cmbProduto.setSelectedIndex(-1);
        carregando = false;
    }

    // recarrega pedidos da mesa
    private void atualizarMesa() {
        modeloPedidos.setRowCount(0);
        modeloItens.setRowCount(0);
        pedidos = new ArrayList<>();
        itensPedido = new ArrayList<>();
        lblTotalParcial.setText("Total parcial da conta: " + moeda.format(BigDecimal.ZERO));
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        if (mesa == null) {
            return;
        }
        try {
            Conta conta = contaControle.consultarContaAberta(mesa.getId());
            pedidos = pedidoControle.listarPorConta(conta.getId());
            for (Pedido p : pedidos) {
                modeloPedidos.addRow(new Object[]{p.getId(), p.getGarcom().getNome(),
                    p.getDataHora().format(formato), p.getSituacao(), moeda.format(p.getTotal())});
            }
            // os pedidos da conta ja estao carregados: nao vale uma nova ida ao banco
            lblTotalParcial.setText("Total parcial da conta: "
                    + moeda.format(contaControle.calcularTotalConta(pedidos)));
        } catch (RegraNegocioException e) {
            // mesa livre ou sem conta: mostra o motivo real em vez de uma mensagem fixa
            lblTotalParcial.setText(e.getMessage());
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // itens do pedido escolhido
    private void mostrarItens(int idPedido) {
        modeloItens.setRowCount(0);
        itensPedido = new ArrayList<>(); // evita sobrar a lista do pedido anterior se der erro
        try {
            itensPedido = pedidoControle.buscar(idPedido).getItens();
            for (ItemPedido i : itensPedido) {
                modeloItens.addRow(new Object[]{i.getProduto().getNome(), i.getQuantidade(),
                    moeda.format(i.getPrecoUnitario()), moeda.format(i.getSubtotal())});
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    // mantém pedido selecionado
    private void reselecionar(int idPedido) {
        for (int i = 0; i < pedidos.size(); i++) {
            if (pedidos.get(i).getId() == idPedido) {
                tblPedidos.setRowSelectionInterval(i, i);
                mostrarItens(idPedido);
                return;
            }
        }
    }

    // itens do pedido em montagem
    private void atualizarNovos() {
        modeloNovo.setRowCount(0);
        for (ItemPedido i : novoPedido.getItens()) {
            modeloNovo.addRow(new Object[]{i.getProduto().getNome(), i.getQuantidade(),
                moeda.format(i.getPrecoUnitario()), moeda.format(i.getSubtotal())});
        }
        lblTotalNovo.setText("Total: "
                + moeda.format(pedidoControle.calcularTotal(novoPedido.getItens())));
    }

    private int lerQuantidade() {
        try {
            return Integer.parseInt(txtQtd.getText().trim());
        } catch (NumberFormatException e) {
            return 0; // controle recusa
        }
    }

    private Pedido pedidoSelecionado() {
        int linha = tblPedidos.getSelectedRow();
        if (linha < 0 || linha >= pedidos.size()) {
            aviso("Selecione um pedido na tabela.");
            return null;
        }
        return pedidos.get(linha);
    }

    private ItemPedido itemSelecionado() {
        int linha = tblItens.getSelectedRow();
        if (linha < 0 || linha >= itensPedido.size()) {
            aviso("Selecione um item na tabela de itens.");
            return null;
        }
        return itensPedido.get(linha);
    }

    private boolean confirmar(String mensagem) {
        return JOptionPane.showConfirmDialog(this, mensagem, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem);
    }

    private void mostrarErro(Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTopo = new javax.swing.JPanel();
        lblMesa = new javax.swing.JLabel();
        cmbMesa = new javax.swing.JComboBox<>();
        lblGarcom = new javax.swing.JLabel();
        cmbGarcom = new javax.swing.JComboBox<>();
        lblProduto = new javax.swing.JLabel();
        cmbProduto = new javax.swing.JComboBox<>();
        lblQtd = new javax.swing.JLabel();
        txtQtd = new javax.swing.JTextField();
        pnlCentro = new javax.swing.JPanel();
        pnlNovo = new javax.swing.JPanel();
        lblNovo = new javax.swing.JLabel();
        scpNovo = new javax.swing.JScrollPane();
        tblNovo = new javax.swing.JTable();
        pnlBtnNovo = new javax.swing.JPanel();
        btnAddNovo = new javax.swing.JButton();
        btnRemNovo = new javax.swing.JButton();
        lblTotalNovo = new javax.swing.JLabel();
        btnRegistrar = new javax.swing.JButton();
        pnlDireita = new javax.swing.JPanel();
        pnlPedidos = new javax.swing.JPanel();
        lblPedidos = new javax.swing.JLabel();
        scpPedidos = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        pnlBtnPedidos = new javax.swing.JPanel();
        btnFinalizar = new javax.swing.JButton();
        btnCancelarPedido = new javax.swing.JButton();
        btnExcluirPedido = new javax.swing.JButton();
        pnlItens = new javax.swing.JPanel();
        lblItens = new javax.swing.JLabel();
        scpItens = new javax.swing.JScrollPane();
        tblItens = new javax.swing.JTable();
        pnlBtnItens = new javax.swing.JPanel();
        btnAddItem = new javax.swing.JButton();
        btnAltQtd = new javax.swing.JButton();
        btnRemItem = new javax.swing.JButton();
        pnlRodape = new javax.swing.JPanel();
        lblTotalParcial = new javax.swing.JLabel();
        btnFecharConta = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Contas / Pedidos");

        lblMesa.setText("Mesa:");
        pnlTopo.add(lblMesa);

        cmbMesa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMesaActionPerformed(evt);
            }
        });
        pnlTopo.add(cmbMesa);

        lblGarcom.setText("Garçom:");
        pnlTopo.add(lblGarcom);

        pnlTopo.add(cmbGarcom);

        lblProduto.setText("Produto:");
        pnlTopo.add(lblProduto);

        pnlTopo.add(cmbProduto);

        lblQtd.setText("Qtd:");
        pnlTopo.add(lblQtd);

        pnlTopo.add(txtQtd);

        getContentPane().add(pnlTopo, java.awt.BorderLayout.NORTH);

        pnlCentro.setLayout(new java.awt.GridLayout(1, 2, 10, 10));
        pnlNovo.setLayout(new java.awt.BorderLayout());
        lblNovo.setText("Novo pedido (itens)");
        pnlNovo.add(lblNovo, java.awt.BorderLayout.NORTH);

        scpNovo.setViewportView(tblNovo);

        pnlNovo.add(scpNovo, java.awt.BorderLayout.CENTER);

        btnAddNovo.setText("Adicionar item");
        btnAddNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNovoActionPerformed(evt);
            }
        });
        pnlBtnNovo.add(btnAddNovo);

        btnRemNovo.setText("Remover item");
        btnRemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemNovoActionPerformed(evt);
            }
        });
        pnlBtnNovo.add(btnRemNovo);

        lblTotalNovo.setText("Total: R$ 0,00");
        pnlBtnNovo.add(lblTotalNovo);

        btnRegistrar.setText("Registrar pedido");
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });
        pnlBtnNovo.add(btnRegistrar);

        pnlNovo.add(pnlBtnNovo, java.awt.BorderLayout.SOUTH);

        pnlCentro.add(pnlNovo);

        pnlDireita.setLayout(new java.awt.GridLayout(2, 1, 10, 10));
        pnlPedidos.setLayout(new java.awt.BorderLayout());
        lblPedidos.setText("Pedidos da conta aberta");
        pnlPedidos.add(lblPedidos, java.awt.BorderLayout.NORTH);

        tblPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPedidosMouseClicked(evt);
            }
        });
        scpPedidos.setViewportView(tblPedidos);

        pnlPedidos.add(scpPedidos, java.awt.BorderLayout.CENTER);

        btnFinalizar.setText("Finalizar");
        btnFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarActionPerformed(evt);
            }
        });
        pnlBtnPedidos.add(btnFinalizar);

        btnCancelarPedido.setText("Cancelar pedido");
        btnCancelarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarPedidoActionPerformed(evt);
            }
        });
        pnlBtnPedidos.add(btnCancelarPedido);

        btnExcluirPedido.setText("Excluir pedido");
        btnExcluirPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirPedidoActionPerformed(evt);
            }
        });
        pnlBtnPedidos.add(btnExcluirPedido);

        pnlPedidos.add(pnlBtnPedidos, java.awt.BorderLayout.SOUTH);

        pnlDireita.add(pnlPedidos);

        pnlItens.setLayout(new java.awt.BorderLayout());
        lblItens.setText("Itens do pedido selecionado");
        pnlItens.add(lblItens, java.awt.BorderLayout.NORTH);

        scpItens.setViewportView(tblItens);

        pnlItens.add(scpItens, java.awt.BorderLayout.CENTER);

        btnAddItem.setText("Adicionar item");
        btnAddItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddItemActionPerformed(evt);
            }
        });
        pnlBtnItens.add(btnAddItem);

        btnAltQtd.setText("Alterar quantidade");
        btnAltQtd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAltQtdActionPerformed(evt);
            }
        });
        pnlBtnItens.add(btnAltQtd);

        btnRemItem.setText("Remover item");
        btnRemItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemItemActionPerformed(evt);
            }
        });
        pnlBtnItens.add(btnRemItem);

        pnlItens.add(pnlBtnItens, java.awt.BorderLayout.SOUTH);

        pnlDireita.add(pnlItens);

        pnlCentro.add(pnlDireita);

        getContentPane().add(pnlCentro, java.awt.BorderLayout.CENTER);

        lblTotalParcial.setText("Total parcial da conta: R$ 0,00");
        pnlRodape.add(lblTotalParcial);

        btnFecharConta.setText("Fechar conta");
        btnFecharConta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharContaActionPerformed(evt);
            }
        });
        pnlRodape.add(btnFecharConta);

        getContentPane().add(pnlRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbMesaActionPerformed(java.awt.event.ActionEvent evt) {
        if (!carregando) {
            atualizarMesa();
        }
    }

    private void btnAddNovoActionPerformed(java.awt.event.ActionEvent evt) {
        Produto produto = (Produto) cmbProduto.getSelectedItem();
        if (produto == null) {
            aviso("Selecione um produto.");
            return;
        }
        try {
            // mesma regra do pedido ja gravado: produto repetido soma a quantidade
            novoPedido.adicionarOuSomarItem(produto, lerQuantidade());
            atualizarNovos();
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnRemNovoActionPerformed(java.awt.event.ActionEvent evt) {
        int linha = tblNovo.getSelectedRow();
        if (linha < 0 || linha >= novoPedido.getItens().size()) {
            aviso("Selecione um item do novo pedido.");
            return;
        }
        novoPedido.removerItem(novoPedido.getItens().get(linha));
        atualizarNovos();
    }

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        Garcom garcom = (Garcom) cmbGarcom.getSelectedItem();
        try {
            // abre a conta se a mesa estiver livre
            pedidoControle.abrirPedido(mesa == null ? 0 : mesa.getId(),
                    garcom == null ? 0 : garcom.getId(), novoPedido.getItens());
            novoPedido.setItens(new ArrayList<>());
            atualizarNovos();
            atualizarMesa();
            aviso("Pedido registrado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void tblPedidosMouseClicked(java.awt.event.MouseEvent evt) {
        int linha = tblPedidos.getSelectedRow();
        if (linha >= 0 && linha < pedidos.size()) {
            mostrarItens(pedidos.get(linha).getId());
        }
    }

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null) {
            return;
        }
        try {
            pedidoControle.finalizar(p.getId());
            atualizarMesa();
            reselecionar(p.getId());
            aviso("Pedido finalizado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null || !confirmar("Cancelar o pedido " + p.getId() + "?")) {
            return;
        }
        try {
            pedidoControle.cancelar(p.getId());
            atualizarMesa();
            reselecionar(p.getId());
            aviso("Pedido cancelado com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnExcluirPedidoActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null || !confirmar("Excluir o pedido " + p.getId() + "?")) {
            return;
        }
        try {
            pedidoControle.excluir(p.getId());
            atualizarMesa();
            aviso("Pedido excluído com sucesso!");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnAddItemActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null) {
            return;
        }
        Produto produto = (Produto) cmbProduto.getSelectedItem();
        if (produto == null) {
            aviso("Selecione um produto.");
            return;
        }
        try {
            pedidoControle.adicionarItem(p.getId(), produto.getId(), lerQuantidade());
            atualizarMesa();
            reselecionar(p.getId());
            aviso("Item adicionado ao pedido.");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnAltQtdActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null) {
            return;
        }
        ItemPedido item = itemSelecionado();
        if (item == null) {
            return;
        }
        try {
            pedidoControle.alterarQuantidade(p.getId(), item.getProduto().getId(), lerQuantidade());
            atualizarMesa();
            reselecionar(p.getId());
            aviso("Quantidade do item alterada.");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnRemItemActionPerformed(java.awt.event.ActionEvent evt) {
        Pedido p = pedidoSelecionado();
        if (p == null) {
            return;
        }
        ItemPedido item = itemSelecionado();
        if (item == null) {
            return;
        }
        try {
            pedidoControle.removerItem(p.getId(), item.getProduto().getId());
            atualizarMesa();
            reselecionar(p.getId());
            aviso("Item removido do pedido.");
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void btnFecharContaActionPerformed(java.awt.event.ActionEvent evt) {
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        if (mesa == null) {
            aviso("Selecione uma mesa.");
            return;
        }
        if (!confirmar("Fechar a conta da " + mesa + "?")) {
            return;
        }
        try {
            Conta conta = contaControle.fecharConta(mesa.getId());
            atualizarMesa();
            aviso("Conta " + conta.getSituacao() + ". Total: " + moeda.format(conta.getTotal()));
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaContaPedido().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddItem;
    private javax.swing.JButton btnAddNovo;
    private javax.swing.JButton btnAltQtd;
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnExcluirPedido;
    private javax.swing.JButton btnFecharConta;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnRemItem;
    private javax.swing.JButton btnRemNovo;
    private javax.swing.JComboBox<Garcom> cmbGarcom;
    private javax.swing.JComboBox<Mesa> cmbMesa;
    private javax.swing.JComboBox<Produto> cmbProduto;
    private javax.swing.JLabel lblGarcom;
    private javax.swing.JLabel lblItens;
    private javax.swing.JLabel lblMesa;
    private javax.swing.JLabel lblNovo;
    private javax.swing.JLabel lblPedidos;
    private javax.swing.JLabel lblProduto;
    private javax.swing.JLabel lblQtd;
    private javax.swing.JLabel lblTotalNovo;
    private javax.swing.JLabel lblTotalParcial;
    private javax.swing.JPanel pnlBtnItens;
    private javax.swing.JPanel pnlBtnNovo;
    private javax.swing.JPanel pnlBtnPedidos;
    private javax.swing.JPanel pnlCentro;
    private javax.swing.JPanel pnlDireita;
    private javax.swing.JPanel pnlItens;
    private javax.swing.JPanel pnlNovo;
    private javax.swing.JPanel pnlPedidos;
    private javax.swing.JPanel pnlRodape;
    private javax.swing.JPanel pnlTopo;
    private javax.swing.JScrollPane scpItens;
    private javax.swing.JScrollPane scpNovo;
    private javax.swing.JScrollPane scpPedidos;
    private javax.swing.JTable tblItens;
    private javax.swing.JTable tblNovo;
    private javax.swing.JTable tblPedidos;
    private javax.swing.JTextField txtQtd;
    // End of variables declaration//GEN-END:variables
}
