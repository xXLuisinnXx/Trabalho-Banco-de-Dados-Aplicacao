package br.com.controle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.entidade.ItemPedido;
import br.com.entidade.Pedido;

public final class TotalizadorPedidos {

    private TotalizadorPedidos() {
    }

    public static BigDecimal calcularItens(List<ItemPedido> itens) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return arredondar(total);
    }

    public static BigDecimal calcularPedidosCobraveis(List<Pedido> pedidos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido pedido : pedidos) {
            if (pedido.entraNoTotal()) {
                total = total.add(pedido.getTotal());
            }
        }
        return arredondar(total);
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
