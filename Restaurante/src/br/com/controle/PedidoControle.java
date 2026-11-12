package br.com.controle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.entidade.Conta;
import br.com.entidade.Garcom;
import br.com.entidade.ItemPedido;
import br.com.entidade.Mesa;
import br.com.entidade.Pedido;
import br.com.entidade.Produto;
import br.com.entidade.SituacaoMesa;
import br.com.entidade.SituacaoPedido;
import br.com.entidade.repository.ManterConta;
import br.com.entidade.repository.ManterGarcom;
import br.com.entidade.repository.ManterMesa;
import br.com.entidade.repository.ManterPedido;
import br.com.entidade.repository.ManterProduto;

public class PedidoControle {
    private final ManterPedido manterPedido = new ManterPedido();
    private final ManterConta manterConta = new ManterConta();
    private final ManterMesa manterMesa = new ManterMesa();
    private final ManterGarcom manterGarcom = new ManterGarcom();
    private final ManterProduto manterProduto = new ManterProduto();

    public Pedido abrirPedido(int idMesa, int idGarcom, List<ItemPedido> itensInformados)
            throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Mesa mesa = new Mesa();
        mesa.setId(idMesa);
        if (!manterMesa.carregar(mesa)) {
            throw new RegraNegocioException("Mesa não encontrada.");
        }
        Garcom garcom = new Garcom();
        garcom.setId(idGarcom);
        if (!manterGarcom.carregar(garcom)) {
            throw new RegraNegocioException("Selecione um garçom válido.");
        }
        if (itensInformados == null || itensInformados.isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter pelo menos um item.");
        }

        Pedido pedido = new Pedido();
        pedido.setGarcom(garcom);
        pedido.setSituacao(SituacaoPedido.EM_PREPARO);
        pedido.setDataHora(LocalDateTime.now());
        for (ItemPedido informado : itensInformados) {
            if (informado.getProduto() == null) {
                throw new RegraNegocioException("Selecione um produto.");
            }
            adicionarOuSomar(pedido, informado.getProduto().getId(), informado.getQuantidade());
        }

        Conta contaAberta = manterConta.buscarAbertaPorMesa(mesa);

        if (mesa.getSituacao() == SituacaoMesa.LIVRE) {
            if (contaAberta != null) {
                throw new RegraNegocioException(
                        "Inconsistência: mesa livre com conta aberta. Feche a conta antes.");
            }
            Conta novaConta = new Conta();
            novaConta.setMesa(mesa);
            novaConta.setDataAbertura(LocalDateTime.now());
            pedido.setConta(novaConta);
            manterPedido.inserirAbrindoConta(pedido); 
        } else {
            if (contaAberta == null) {
                throw new RegraNegocioException("Inconsistência: mesa ocupada sem conta aberta.");
            }
            pedido.setConta(contaAberta);
            manterPedido.registrarPorProcedure(pedido); // trava a conta e confere se ainda está ABERTA
        }
        return pedido;
    }

    public void adicionarItem(int idPedido, int idProduto, int quantidade) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Pedido pedido = buscarEmPreparo(idPedido);
        adicionarOuSomar(pedido, idProduto, quantidade);
        manterPedido.editarPedido(pedido);
    }

    public void alterarQuantidade(int idPedido, int idProduto, int novaQuantidade) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Pedido pedido = buscarEmPreparo(idPedido);
        ItemPedido item = localizarItem(pedido, idProduto);
        validarQuantidade(novaQuantidade);
        item.setQuantidade(novaQuantidade);
        pedido.calcularTotal();
        manterPedido.editarPedido(pedido);
    }

    public void removerItem(int idPedido, int idProduto) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Pedido pedido = buscarEmPreparo(idPedido);
        ItemPedido item = localizarItem(pedido, idProduto);
        if (pedido.getItens().size() == 1) {
            throw new RegraNegocioException(
                    "O pedido precisa ter pelo menos um item. Cancele o pedido se for o caso.");
        }
        pedido.removerItem(item);
        manterPedido.editarPedido(pedido);
    }

    public void finalizar(int idPedido) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Pedido pedido = buscarEmPreparo(idPedido);
        if (pedido.getItens().isEmpty()) {
            throw new RegraNegocioException("Não é possível finalizar um pedido sem itens.");
        }
        manterPedido.atualizarSituacao(idPedido, SituacaoPedido.FINALIZADO);
    }


    public void cancelar(int idPedido) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        buscarEmPreparo(idPedido);
        manterPedido.atualizarSituacao(idPedido, SituacaoPedido.CANCELADO);
    }

    
    public void excluir(int idPedido) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        Pedido pedido = buscarEmPreparo(idPedido);
        manterPedido.deletarPedido(pedido);
    }

    public Pedido buscar(int idPedido) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        return buscarExistente(idPedido);
    }

    public List<Pedido> listarPorConta(int idConta) throws Exception {
        Sessao.exigir(Permissao.OPERAR_CONTAS_PEDIDOS);
        return manterPedido.PesquisarPorConta(idConta);
    }

    public BigDecimal calcularTotal(List<ItemPedido> itens) {
        return TotalizadorPedidos.calcularItens(itens);
    }

    private void adicionarOuSomar(Pedido pedido, int idProduto, int quantidade) throws Exception {
        validarQuantidade(quantidade);
        Produto produto = new Produto();
        produto.setId(idProduto);
        if (!manterProduto.carregar(produto)) {
            throw new RegraNegocioException("Produto não encontrado.");
        }
        pedido.adicionarOuSomarItem(produto, quantidade); // copia o preço vigente
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser um número inteiro maior que zero.");
        }
    }

    private ItemPedido localizarItem(Pedido pedido, int idProduto) {
        ItemPedido item = pedido.buscarItemPorProduto(idProduto);
        if (item != null) {
            return item;
        }
        throw new RegraNegocioException("O produto não faz parte deste pedido.");
    }

    private Pedido buscarExistente(int idPedido) throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(idPedido);
        if (!manterPedido.carregar(pedido)) {
            throw new RegraNegocioException("Pedido não encontrado.");
        }
        return pedido;
    }

    private Pedido buscarEmPreparo(int idPedido) throws Exception {
        Pedido pedido = buscarExistente(idPedido);
        if (!pedido.permiteAlteracao()) {
            throw new RegraNegocioException(
                    "Pedido " + pedido.getSituacao() + " não pode ser alterado, cancelado ou excluído.");
        }
        return pedido;
    }
}
