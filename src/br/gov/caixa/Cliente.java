package br.gov.caixa;

import java.util.Date;

public class Cliente implements Usuario {
    private final long id;
    private final Tipo classificacao;
    private String nome;
    private final Date dataCadastro;
    private Situacao status;

    public Cliente(String cpf_cnpj, String nome, Tipo classificacao) {
        cpf_cnpj = cpf_cnpj.replace(".", "");
        cpf_cnpj = cpf_cnpj.replace("-", "");
        cpf_cnpj = cpf_cnpj.replace("/", "");
        this.classificacao = classificacao;
        this.id = Long.parseLong(cpf_cnpj);
        this.nome = nome;
        this.dataCadastro = new Date();
        this.status = Situacao.ATIVO;
    }

    @Override
    public Tipo getClassificacao() {
        return classificacao;
    }

    @Override
    public Date getDataCadastro() {
        return dataCadastro;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public Situacao getStatus() {
        return status;
    }

    @Override
    public void setStatus(Situacao status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.nome + ";" + this.id + ";" + this.classificacao;
    }
}
