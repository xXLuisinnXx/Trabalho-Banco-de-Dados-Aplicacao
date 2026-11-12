package br.com.controle;

import java.math.BigDecimal;
import java.util.List;

import br.com.entidade.Pedido;

public class RelatorioPedidos {
private final List<Pedido> pedidos;
    private final int quantidade;
    private final BigDecimal totalFinalizados;

    public RelatorioPedidos(List<Pedido> pedidos, int quantidade, BigDecimal totalFinalizados) {
        this.pedidos = pedidos;
        this.quantidade = quantidade;
        this.totalFinalizados = totalFinalizados;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getTotalFinalizados() {
        return totalFinalizados;
    }
}
