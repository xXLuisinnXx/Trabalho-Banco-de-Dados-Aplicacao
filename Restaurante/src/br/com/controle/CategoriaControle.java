package br.com.controle;

import java.util.List;

import br.com.entidade.Categoria;
import br.com.entidade.repository.ManterCategoria;

public class CategoriaControle {
    private final ManterCategoria manterCategoria = new ManterCategoria();

    public void cadastrar(String nome) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Categoria categoria = new Categoria();
        preencher(categoria, nome);
        manterCategoria.inserir(categoria);
    }

    public void atualizar(int id, String nome) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Categoria categoria = buscarExistente(id);
        preencher(categoria, nome);
        manterCategoria.editarCategoria(categoria);
    }

    public void excluir(int id) throws Exception {
        Sessao.exigir(Permissao.EXCLUIR_CADASTRO);
        Categoria categoria = buscarExistente(id);
        if (manterCategoria.possuiProdutos(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir: a categoria possui produtos vinculados.");
        }
        manterCategoria.deletarCategoria(categoria);
    }

    public List<Categoria> listar() throws Exception {
        Sessao.exigir(Permissao.CONSULTAR);
        return manterCategoria.PesquisarTudo();
    }

    private void preencher(Categoria categoria, String nome) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        if (nome.trim().length() > 50) {
            throw new RegraNegocioException("O nome deve ter no máximo 50 caracteres.");
        }
        if (manterCategoria.existeNome(nome.trim(), categoria.getId())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }
        // grava o mesmo valor que foi validado, senao " Bebidas " passa pela
        // checagem de duplicidade e entra no banco com os espacos
        categoria.setNome(nome.trim());
    }

    private Categoria buscarExistente(int id) throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        if (!manterCategoria.carregar(categoria)) {
            throw new RegraNegocioException("Categoria não encontrada.");
        }
        return categoria;
    }
}
