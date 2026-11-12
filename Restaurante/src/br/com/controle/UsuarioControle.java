package br.com.controle;

import java.util.List;
import java.util.regex.Pattern;

import br.com.entidade.GrupoUsuario;
import br.com.entidade.Usuario;
import br.com.entidade.repository.ManterUsuario;

/** Cadastro, alteração e exclusão de usuários. Só o grupo ADMIN pode usar. */
public class UsuarioControle {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ManterUsuario manterUsuario = new ManterUsuario();

    public void cadastrar(String nome, String email, String senha, GrupoUsuario grupo) throws Exception {
        Sessao.exigir(Permissao.GERENCIAR_USUARIOS);
        criar(nome, email, senha, grupo);
    }

    // só funciona com a tabela usuarios vazia (primeira execução; ver CriarAdminInicial)
    public void cadastrarPrimeiroAdmin(String nome, String email, String senha) throws Exception {
        if (manterUsuario.contar() > 0) {
            throw new RegraNegocioException("Já existem usuários cadastrados.");
        }
        criar(nome, email, senha, GrupoUsuario.ADMIN);
    }

    // novaSenha vazia ou nula mantém a senha atual
    public void atualizar(String id, String nome, String email, GrupoUsuario grupo, String novaSenha)
            throws Exception {
        Sessao.exigir(Permissao.GERENCIAR_USUARIOS);
        Usuario usuario = buscarExistente(id);
        if (ehUsuarioLogado(id) && grupo != usuario.getGrupo()) {
            throw new RegraNegocioException("Você não pode alterar o seu próprio perfil de acesso.");
        }
        boolean trocarSenha = novaSenha != null && !novaSenha.isEmpty();
        if (trocarSenha) {
            validarSenha(novaSenha);
        }
        preencher(usuario, nome, email, grupo);
        if (trocarSenha) {
            usuario.setSenhaHash(SenhaUtil.gerarHash(novaSenha));
        }
        manterUsuario.editarUsuario(usuario, trocarSenha);
    }

    public void excluir(String id) throws Exception {
        Sessao.exigir(Permissao.GERENCIAR_USUARIOS);
        Usuario usuario = buscarExistente(id);
        // só ADMIN gerencia usuários; como ninguém exclui a si mesmo, sempre sobra um administrador
        if (ehUsuarioLogado(id)) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário.");
        }
        manterUsuario.deletarUsuario(usuario);
    }

    public List<Usuario> listar() throws Exception {
        Sessao.exigir(Permissao.GERENCIAR_USUARIOS);
        return manterUsuario.PesquisarTudo();
    }

    public Usuario buscar(String id) throws Exception {
        Sessao.exigir(Permissao.GERENCIAR_USUARIOS);
        return buscarExistente(id);
    }

    private void criar(String nome, String email, String senha, GrupoUsuario grupo) throws Exception {
        Usuario usuario = new Usuario();
        validarSenha(senha);
        preencher(usuario, nome, email, grupo);
        usuario.setSenhaHash(SenhaUtil.gerarHash(senha));
        manterUsuario.inserir(usuario);
    }

    private void preencher(Usuario usuario, String nome, String email, GrupoUsuario grupo)
            throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("Informe o nome do usuário.");
        }
        if (nome.trim().length() > 100) {
            throw new RegraNegocioException("O nome deve ter no máximo 100 caracteres.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RegraNegocioException("Informe o e-mail.");
        }
        if (email.trim().length() > 100) {
            throw new RegraNegocioException("O e-mail deve ter no máximo 100 caracteres.");
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new RegraNegocioException("E-mail inválido.");
        }
        if (grupo == null) {
            throw new RegraNegocioException("Selecione o perfil de acesso do usuário.");
        }
        usuario.setNome(nome);   // a entidade apara e padroniza (e-mail em minúsculas)
        usuario.setEmail(email);
        usuario.setGrupo(grupo);
        String ignorar = usuario.getId() == null ? "" : usuario.getId();
        if (manterUsuario.existeEmail(usuario.getEmail(), ignorar)) {
            throw new RegraNegocioException("Já existe um usuário com esse e-mail.");
        }
        if (!manterUsuario.existeGrupo(grupo.name())) {
            throw new RegraNegocioException("O grupo " + grupo
                    + " não existe em grupos_usuarios. Execute o AjustesBanco.sql.");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new RegraNegocioException("A senha deve ter pelo menos 8 caracteres.");
        }
        if (senha.length() > 100) {
            throw new RegraNegocioException("A senha deve ter no máximo 100 caracteres.");
        }
        boolean letra = false;
        boolean numero = false;
        for (char c : senha.toCharArray()) {
            letra = letra || Character.isLetter(c);
            numero = numero || Character.isDigit(c);
        }
        if (!letra || !numero) {
            throw new RegraNegocioException("A senha deve ter letras e números.");
        }
    }

    private boolean ehUsuarioLogado(String id) {
        Usuario logado = Sessao.getUsuario();
        return logado != null && logado.getId() != null && logado.getId().equals(id);
    }

    private Usuario buscarExistente(String id) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        if (id == null || !manterUsuario.carregar(usuario)) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }
        return usuario;
    }
}
