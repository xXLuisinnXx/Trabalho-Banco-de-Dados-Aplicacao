package br.com.controle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.entidade.Pedido;
import br.com.entidade.SituacaoPedido;
import br.com.entidade.repository.ManterPedido;

public class RelatorioControle {

    private final ManterPedido manterPedido = new ManterPedido();

    public RelatorioPedidos gerar(LocalDate dataInicial, LocalDate dataFinal,
            Integer idGarcom, Integer idMesa, SituacaoPedido situacao) throws Exception {
        Sessao.exigir(Permissao.CONSULTAR_RELATORIOS);
        if (dataInicial == null || dataFinal == null) {
            throw new RegraNegocioException("Informe a data inicial e a data final.");
        }
        if (dataFinal.isBefore(dataInicial)) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial.");
        }

        LocalDateTime inicio = dataInicial.atStartOfDay();
        LocalDateTime fimExclusivo = dataFinal.plusDays(1).atStartOfDay();

        List<Pedido> pedidos = manterPedido.PesquisarPorPeriodo(
                inicio, fimExclusivo, idGarcom, idMesa, situacao);

        return new RelatorioPedidos(pedidos, pedidos.size(), somarFinalizados(pedidos));
    }

    public BigDecimal somarFinalizados(List<Pedido> pedidos) {
        return TotalizadorPedidos.calcularPedidosCobraveis(pedidos);
    }
}
