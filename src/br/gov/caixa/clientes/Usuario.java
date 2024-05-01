package br.gov.caixa.clientes;

import java.time.LocalDate;

public interface Usuario {
    public enum Tipo {PF, PJ}
    public enum Situacao {ATIVO, INATIVO}
    public Tipo getClassificacao();
    public LocalDate getDataCadastro();
    public long getId();
    public String getNome();
    public void setNome(String nome);
    public Situacao getStatus();
    public void setStatus(Situacao status);
}
