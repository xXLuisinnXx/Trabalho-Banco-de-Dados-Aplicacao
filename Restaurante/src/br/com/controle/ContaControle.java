package br.com.controle;

import java.math.BigDecimal;
import java.util.List;

import br.com.entidade.Conta;
import br.com.entidade.Mesa;
import br.com.entidade.Pedido;
import br.com.entidade.SituacaoMesa;
import br.com.entidade.repository.ManterConta;
import br.com.entidade.repository.ManterMesa;
import br.com.entidade.repository.ManterPedido;

public class ContaControle {
    private final ManterConta manterConta = new ManterConta();
    private final ManterMesa manterMesa = new ManterMesa();
    private final ManterPedido manterPedido = new ManterPedido();

    // 1 consulta na view vw_contas_abertas (antes: mesa + conta + pedidos + itens de cada pedido)
    public BigDecimal consultarTotalParcial(int idMesa) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        BigDecimal total = manterConta.totalParcialPorMesa(idMesa);
        if (total == null) {
            throw new RegraNegocioException("Não há conta aberta para esta mesa.");
        }
        return total;
    }

    // conta aberta da mesa
    public Conta consultarContaAberta(int idMesa) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        return buscarContaAberta(buscarMesa(idMesa));
    }

    public Conta fecharConta(int idMesa) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Mesa mesa = buscarMesa(idMesa);
        Conta conta = buscarContaAberta(mesa);
        List<Pedido> pedidos = manterPedido.PesquisarPorConta(conta.getId());

        // checagem rápida só para dar a mensagem antes de ir ao banco; a regra definitiva
        // está na procedure fechar_conta, que trava a conta e confere de novo
        if (existePedidoBloqueandoFechamento(pedidos)) {
            throw new RegraNegocioException(
                    "Existem pedidos em preparo. Finalize ou cancele antes de fechar a conta.");
        }

        manterConta.fecharConta(conta); // total, situação e liberação da mesa ficam no banco
        return conta;
    }

    public BigDecimal calcularTotalConta(List<Pedido> pedidos) {
        return TotalizadorPedidos.calcularPedidosCobraveis(pedidos);
    }

    private boolean existePedidoBloqueandoFechamento(List<Pedido> pedidos) {
        for (Pedido pedido : pedidos) {
            if (pedido.bloqueiaFechamentoConta()) {
                return true;
            }
        }
        return false;
    }

    private Mesa buscarMesa(int idMesa) throws Exception {
        Mesa mesa = new Mesa();
        mesa.setId(idMesa);
        if (!manterMesa.carregar(mesa)) {
            throw new RegraNegocioException("Mesa não encontrada.");
        }
        return mesa;
    }

    private Conta buscarContaAberta(Mesa mesa) throws Exception {
        if (mesa.getSituacao() != SituacaoMesa.OCUPADA) {
            throw new RegraNegocioException("A mesa está livre: não há conta aberta.");
        }
        Conta conta = manterConta.buscarAbertaPorMesa(mesa);
        if (conta == null) {
            throw new RegraNegocioException("Não foi encontrada conta aberta para esta mesa.");
        }
        return conta;
    }
}
