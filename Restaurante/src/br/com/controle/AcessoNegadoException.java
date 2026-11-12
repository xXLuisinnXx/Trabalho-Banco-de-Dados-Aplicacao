package br.com.controle;

/** Herda de RegraNegocioException: as telas já mostram a mensagem sem mudar nada. */
public class AcessoNegadoException extends RegraNegocioException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
