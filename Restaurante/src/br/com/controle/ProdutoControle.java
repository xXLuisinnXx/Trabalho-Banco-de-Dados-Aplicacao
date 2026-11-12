package br.com.controle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.entidade.Categoria;
import br.com.entidade.Produto;
import br.com.entidade.repository.ManterCategoria;
import br.com.entidade.repository.ManterProduto;

public class ProdutoControle {
private final ManterProduto manterProduto = new ManterProduto();
    private final ManterCategoria manterCategoria = new ManterCategoria();

    public void cadastrar(String nome, BigDecimal preco, int idCategoria) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Produto produto = new Produto();
        preencher(produto, nome, preco, idCategoria);
        manterProduto.inserir(produto);
    }

    public void atualizar(int id, String nome, BigDecimal preco, int idCategoria) throws Exception {
        Sessao.exigir(Permissao.CADASTRAR_CATALOGO);
        Produto produto = buscarExistente(id);
        preencher(produto, nome, preco, idCategoria);
        manterProduto.editarProduto(produto);
    }

    public void excluir(int id) throws Exception {
        Sessao.exigir(Permissao.EXCLUIR_CADASTRO);
        Produto produto = buscarExistente(id);
        if (manterProduto.possuiItensPedido(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir: o produto já foi usado em pedidos.");
        }
        manterProduto.deletarProduto(produto);
    }

    public List<Produto> listar() throws Exception {
        Sessao.exigir(Permissao.CONSULTAR);
        return manterProduto.PesquisarTudo();
    }

    private void preencher(Produto produto, String nome, BigDecimal preco, int idCategoria)
            throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("Informe o nome do produto.");
        }
        if (nome.trim().length() > 55) {
            throw new RegraNegocioException("O nome deve ter no máximo 55 caracteres.");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O preço deve ser maior que zero.");
        }
        Categoria categoria = new Categoria();
        categoria.setId(idCategoria);
        if (!manterCategoria.carregar(categoria)) {
            throw new RegraNegocioException("Selecione uma categoria válida.");
        }
        produto.setNome(nome.trim()); // o tamanho validado acima e o do nome aparado
        produto.setPreco(preco.setScale(2, RoundingMode.HALF_UP));
        produto.setCategoria(categoria);
    }

    private Produto buscarExistente(int id) throws Exception {
        Produto produto = new Produto();
        produto.setId(id);
        if (!manterProduto.carregar(produto)) {
            throw new RegraNegocioException("Produto não encontrado.");
        }
        return produto;
    }
}
