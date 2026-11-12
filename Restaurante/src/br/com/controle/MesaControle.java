package br.com.controle;

import java.util.List;

import br.com.entidade.Mesa;
import br.com.entidade.SituacaoMesa;
import br.com.entidade.repository.ManterMesa;

public class MesaControle {
private final ManterMesa manterMesa = new ManterMesa();

   
    public void cadastrar(int numero, int capacidade) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Mesa mesa = new Mesa();
        preencher(mesa, numero, capacidade);
        manterMesa.inserir(mesa);
    }

    
    public void atualizar(int id, int numero, int capacidade) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Mesa mesa = buscarExistente(id);
        preencher(mesa, numero, capacidade);
        manterMesa.editarMesa(mesa);
    }

    public void excluir(int id) throws Exception {
        Sessao.exigir(Permissao.EXCLUIR_CADASTRO);
        Mesa mesa = buscarExistente(id);
        if (mesa.getSituacao() == SituacaoMesa.OCUPADA) {
            throw new RegraNegocioException("Não é possível excluir uma mesa ocupada.");
        }
        if (manterMesa.possuiContas(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir: a mesa possui histórico de contas.");
        }
        manterMesa.deletarMesa(mesa);
    }

    public List<Mesa> listar() throws Exception {
        Sessao.exigir(Permissao.CONSULTAR);
        return manterMesa.PesquisarTudo();
    }

    public List<Mesa> listarLivres() throws Exception {
        Sessao.exigir(Permissao.CONSULTAR);
        return manterMesa.PesquisarLivres();
    }

    private void preencher(Mesa mesa, int numero, int capacidade) throws Exception {
        if (numero <= 0) {
            throw new RegraNegocioException("O número da mesa deve ser maior que zero.");
        }
        if (capacidade <= 0) {
            throw new RegraNegocioException("A capacidade deve ser maior que zero.");
        }
        if (manterMesa.existeNumero(numero, mesa.getId())) {
            throw new RegraNegocioException("Já existe uma mesa com esse número.");
        }
        mesa.setNumero(numero);
        mesa.setCapacidade(capacidade);
    }

    private Mesa buscarExistente(int id) throws Exception {
        Mesa mesa = new Mesa();
        mesa.setId(id);
        if (!manterMesa.carregar(mesa)) {
            throw new RegraNegocioException("Mesa não encontrada.");
        }
        return mesa;
    }
}
