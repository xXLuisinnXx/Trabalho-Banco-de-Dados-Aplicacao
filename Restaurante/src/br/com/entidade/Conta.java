package br.com.entidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Conta {

    private int id;
    private Mesa mesa;
    private LocalDateTime dataAbertura = LocalDateTime.now();
    private LocalDateTime dataFechamento; // nulo enquanto estiver ABERTA
    private SituacaoConta situacao = SituacaoConta.ABERTA;
    private BigDecimal total = BigDecimal.ZERO;

    public Conta() {
    }

    public Conta(int id, Mesa mesa) {
        this.id = id;
        this.mesa = mesa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public SituacaoConta getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoConta situacao) {
        this.situacao = situacao;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O total da conta não pode ser negativo.");
        }
        this.total = total;
    }
}
