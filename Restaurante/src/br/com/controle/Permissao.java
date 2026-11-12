package br.com.controle;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import br.com.entidade.GrupoUsuario;

import static br.com.entidade.GrupoUsuario.ADMIN;
import static br.com.entidade.GrupoUsuario.GARCOM;
import static br.com.entidade.GrupoUsuario.GERENTE;

/** Matriz de permissões: o que cada grupo pode fazer. Alterar aqui muda o sistema todo. */
public enum Permissao {
    CONSULTAR("consultar cadastros", GARCOM, GERENTE, ADMIN),
    OPERAR_CONTAS_PEDIDOS("operar contas e pedidos", GARCOM, GERENTE, ADMIN),
    CONSULTAR_RELATORIOS("consultar relatórios", GERENTE, ADMIN),
    CADASTRAR_CATALOGO("cadastrar e alterar produtos, categorias, garçons e mesas", GERENTE, ADMIN),
    EXCLUIR_CADASTRO("excluir cadastros", ADMIN),
    GERENCIAR_USUARIOS("gerenciar usuários", ADMIN);

    private final String descricao;
    private final Set<GrupoUsuario> grupos;

    Permissao(String descricao, GrupoUsuario... grupos) {
        this.descricao = descricao;
        this.grupos = EnumSet.copyOf(Arrays.asList(grupos));
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean permite(GrupoUsuario grupo) {
        return grupo != null && grupos.contains(grupo);
    }
}
