package br.gov.caixa;

import java.util.Date;

public class Cliente implements Usuario {
    private final long id;
    private final Tipo classificacao;
    private String nome;
    private final Date dataCadastro;
    private Situacao status;

    public Cliente(String cpf_cnpj, String nome) {
        cpf_cnpj = cpf_cnpj.replace(".", "");
        cpf_cnpj = cpf_cnpj.replace("-", "");
        cpf_cnpj = cpf_cnpj.replace("/", "");
        if (cpf_cnpj.length() > 11) {
            this.classificacao = Tipo.PJ;
        } else {
            this.classificacao = Tipo.PF;
        }
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
        return this.nome;
    }
}
