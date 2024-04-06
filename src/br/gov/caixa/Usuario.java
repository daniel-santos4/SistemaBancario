package br.gov.caixa;

import java.util.Date;

public class Usuario {
    public enum Tipo {PF, PJ}
    public enum Situacao {ATIVO, INATIVO}
    private long id;
    private final Tipo classificacao;
    private String nome;
    private final Date dataCadastro;
    private Situacao status;

    public Tipo getClassificacao() {
        return classificacao;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public Usuario(String cpf_cnpj, String nome) {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
