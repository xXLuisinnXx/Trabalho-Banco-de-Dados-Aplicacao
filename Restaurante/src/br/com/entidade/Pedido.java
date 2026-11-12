package br.com.entidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int id;
    private Conta conta;
    private Garcom garcom;
    private LocalDateTime dataHora = LocalDateTime.now();
    private SituacaoPedido situacao = SituacaoPedido.EM_PREPARO; 
    private BigDecimal total = BigDecimal.ZERO;
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(int id, Conta conta, Garcom garcom) {
        this.id = id;
        this.conta = conta;
        this.garcom = garcom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public Garcom getGarcom() {
        return garcom;
    }

    public void setGarcom(Garcom garcom) {
        this.garcom = garcom;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public SituacaoPedido getSituacao() {
        return situacao;
    }

    // nunca nula: as regras de negocio sao consultadas atraves dela
    public void setSituacao(SituacaoPedido situacao) {
        if (situacao == null) {
            throw new IllegalArgumentException("A situação do pedido é obrigatória.");
        }
        this.situacao = situacao;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        calcularTotal();
    }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        calcularTotal();
    }

    public void adicionarOuSomarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto do item é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        ItemPedido itemExistente = buscarItemPorProduto(produto.getId());
        if (itemExistente != null) {
            itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade);
            calcularTotal();
            return;
        }

        adicionarItem(new ItemPedido(produto, quantidade));
    }

    public void removerItem(ItemPedido item) {
        itens.remove(item);
        calcularTotal();
    }

    public ItemPedido buscarItemPorProduto(int idProduto) {
        for (ItemPedido item : itens) {
            if (item.getProduto().getId() == idProduto) {
                return item;
            }
        }
        return null;
    }

    public boolean permiteAlteracao() {
        return situacao.permiteAlteracao();
    }

    public boolean bloqueiaFechamentoConta() {
        return situacao.bloqueiaFechamentoConta();
    }

    public boolean entraNoTotal() {
        return situacao.entraNoTotal();
    }

    public void calcularTotal() {
        total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
    }
}
