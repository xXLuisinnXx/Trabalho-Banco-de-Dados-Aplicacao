package br.com.visao;

import br.com.controle.GarcomControle;
import br.com.controle.MesaControle;
import br.com.controle.RelatorioControle;
import br.com.controle.RelatorioPedidos;
import br.com.entidade.Garcom;
import br.com.entidade.Mesa;
import br.com.entidade.Pedido;
import br.com.entidade.SituacaoPedido;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaRelatorio extends javax.swing.JFrame {

    private final RelatorioControle controle = new RelatorioControle();
    private final GarcomControle garcomControle = new GarcomControle();
    private final MesaControle mesaControle = new MesaControle();
    private final DefaultTableModel modelo;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TelaRelatorio() {
        initComponents();
        setSize(1100, 550);
        setLocationRelativeTo(null);
        txtInicial.setPreferredSize(new java.awt.Dimension(90, 25));
        txtFinal.setPreferredSize(new java.awt.Dimension(90, 25));
        cmbGarcom.setPreferredSize(new java.awt.Dimension(150, 25));
        cmbMesa.setPreferredSize(new java.awt.Dimension(90, 25));
        cmbSituacao.setPreferredSize(new java.awt.Dimension(120, 25));

        // tabela somente leitura
        modelo = new DefaultTableModel(
                new Object[]{"ID", "Data/hora", "Mesa", "Garçom", "Situação", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tblPedidos.setModel(modelo);

        // período padrão: ano atual
        txtInicial.setText(LocalDate.now().withDayOfYear(1).format(formatoData));
        txtFinal.setText(LocalDate.now().format(formatoData));
        carregarCombos();
        pesquisar();
    }

    // "Todos" + opções do banco
    private void carregarCombos() {
        cmbGarcom.addItem("Todos");
        cmbMesa.addItem("Todas");
        cmbSituacao.addItem("Todas");
        try {
            for (Garcom g : garcomControle.listar()) {
                cmbGarcom.addItem(g);
            }
            for (Mesa m : mesaControle.listar()) {
                cmbMesa.addItem(m);
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
        for (SituacaoPedido s : SituacaoPedido.values()) {
            cmbSituacao.addItem(s);
        }
        cmbGarcom.setSelectedIndex(0);
        cmbMesa.setSelectedIndex(0);
        cmbSituacao.setSelectedIndex(0);
    }

    // consulta com os filtros
    private void pesquisar() {
        try {
            LocalDate inicial = LocalDate.parse(txtInicial.getText().trim(), formatoData);
            LocalDate fim = LocalDate.parse(txtFinal.getText().trim(), formatoData);
            Object g = cmbGarcom.getSelectedItem();
            Object m = cmbMesa.getSelectedItem();
            Object s = cmbSituacao.getSelectedItem();
            Integer idGarcom = g instanceof Garcom ? ((Garcom) g).getId() : null;
            Integer idMesa = m instanceof Mesa ? ((Mesa) m).getId() : null;
            SituacaoPedido situacao = s instanceof SituacaoPedido ? (SituacaoPedido) s : null;

            RelatorioPedidos relatorio = controle.gerar(inicial, fim, idGarcom, idMesa, situacao);

            modelo.setRowCount(0);
            for (Pedido p : relatorio.getPedidos()) {
                modelo.addRow(new Object[]{p.getId(), p.getDataHora().format(formatoHora),
                    p.getConta().getMesa(), p.getGarcom().getNome(), p.getSituacao(),
                    moeda.format(p.getTotal())});
            }
            lblQuantidade.setText("Quantidade de pedidos: " + relatorio.getQuantidade());
            lblFaturamento.setText("Faturamento (finalizados): "
                    + moeda.format(relatorio.getTotalFinalizados()));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Data inválida. Use o formato dd/mm/aaaa.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void mostrarErro(Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFiltros = new javax.swing.JPanel();
        lblInicial = new javax.swing.JLabel();
        txtInicial = new javax.swing.JTextField();
        lblFinal = new javax.swing.JLabel();
        txtFinal = new javax.swing.JTextField();
        lblGarcom = new javax.swing.JLabel();
        cmbGarcom = new javax.swing.JComboBox<>();
        lblMesa = new javax.swing.JLabel();
        cmbMesa = new javax.swing.JComboBox<>();
        lblSituacao = new javax.swing.JLabel();
        cmbSituacao = new javax.swing.JComboBox<>();
        btnPesquisar = new javax.swing.JButton();
        scpPedidos = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        pnlRodape = new javax.swing.JPanel();
        lblQuantidade = new javax.swing.JLabel();
        lblFaturamento = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Relatório de Pedidos");

        lblInicial.setText("Data inicial:");
        pnlFiltros.add(lblInicial);

        pnlFiltros.add(txtInicial);

        lblFinal.setText("Data final:");
        pnlFiltros.add(lblFinal);

        pnlFiltros.add(txtFinal);

        lblGarcom.setText("Garçom:");
        pnlFiltros.add(lblGarcom);

        pnlFiltros.add(cmbGarcom);

        lblMesa.setText("Mesa:");
        pnlFiltros.add(lblMesa);

        pnlFiltros.add(cmbMesa);

        lblSituacao.setText("Situação:");
        pnlFiltros.add(lblSituacao);

        pnlFiltros.add(cmbSituacao);

        btnPesquisar.setText("Pesquisar");
        btnPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarActionPerformed(evt);
            }
        });
        pnlFiltros.add(btnPesquisar);

        getContentPane().add(pnlFiltros, java.awt.BorderLayout.NORTH);

        scpPedidos.setViewportView(tblPedidos);

        getContentPane().add(scpPedidos, java.awt.BorderLayout.CENTER);

        lblQuantidade.setText("Quantidade de pedidos: 0");
        pnlRodape.add(lblQuantidade);

        lblFaturamento.setText("Faturamento (finalizados): R$ 0,00");
        pnlRodape.add(lblFaturamento);

        getContentPane().add(pnlRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {
        pesquisar();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaRelatorio().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JComboBox<Object> cmbGarcom;
    private javax.swing.JComboBox<Object> cmbMesa;
    private javax.swing.JComboBox<Object> cmbSituacao;
    private javax.swing.JLabel lblFaturamento;
    private javax.swing.JLabel lblFinal;
    private javax.swing.JLabel lblGarcom;
    private javax.swing.JLabel lblInicial;
    private javax.swing.JLabel lblMesa;
    private javax.swing.JLabel lblQuantidade;
    private javax.swing.JLabel lblSituacao;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlRodape;
    private javax.swing.JScrollPane scpPedidos;
    private javax.swing.JTable tblPedidos;
    private javax.swing.JTextField txtFinal;
    private javax.swing.JTextField txtInicial;
    // End of variables declaration//GEN-END:variables
}
