package br.com.controle;

import java.util.List;

import br.com.entidade.Garcom;
import br.com.entidade.repository.ManterGarcom;

public class GarcomControle {
private final ManterGarcom manterGarcom = new ManterGarcom();

    public void cadastrar(String nome, String telefone) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Garcom garcom = new Garcom();
        preencher(garcom, nome, telefone);
        manterGarcom.inserir(garcom);
    }

    public void atualizar(int id, String nome, String telefone) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Garcom garcom = buscarExistente(id);
        preencher(garcom, nome, telefone);
        manterGarcom.editarGarcom(garcom);
    }

    public void excluir(int id) throws Exception {
        Sessao.exigir(Permissao.EXCLUIR_CADASTRO);
        Garcom garcom = buscarExistente(id);
        if (manterGarcom.possuiPedidos(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir: o garçom possui pedidos registrados.");
        }
        manterGarcom.deletarGarcom(garcom);
    }

    public List<Garcom> listar() throws Exception {
        Sessao.exigir(Permissao.CONSULTAR);
        return manterGarcom.PesquisarTudo();
    }

    private void preencher(Garcom garcom, String nome, String telefone) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("Informe o nome do garçom.");
        }
        if (nome.trim().length() > 100) {
            throw new RegraNegocioException("O nome deve ter no máximo 100 caracteres.");
        }
        if (telefone != null && telefone.trim().length() > 20) {
            throw new RegraNegocioException("O telefone deve ter no máximo 20 caracteres.");
        }
        garcom.setNome(nome.trim()); // o tamanho validado acima e o do nome aparado
        garcom.setTelefone(telefone == null || telefone.trim().isEmpty() ? null : telefone.trim());
    }

    private Garcom buscarExistente(int id) throws Exception {
        Garcom garcom = new Garcom();
        garcom.setId(id);
        if (!manterGarcom.carregar(garcom)) {
            throw new RegraNegocioException("Garçom não encontrado.");
        }
        return garcom;
    }
}
