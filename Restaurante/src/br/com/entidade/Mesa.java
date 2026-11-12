package br.com.entidade;

public class Mesa {

    private int id;
    private int numero;
    private int capacidade;
    private SituacaoMesa situacao = SituacaoMesa.LIVRE; // toda mesa começa LIVRE

    public Mesa() {
    }

    public Mesa(int id, int numero, int capacidade) {
        this.id = id;
        this.numero = numero;
        this.capacidade = capacidade;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("O número da mesa deve ser maior que zero.");
        }
        this.numero = numero;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        }
        this.capacidade = capacidade;
    }

    public SituacaoMesa getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoMesa situacao) {
        this.situacao = situacao;
    }

    @Override
    public String toString() {
        return "Mesa " + numero;
    }
}
