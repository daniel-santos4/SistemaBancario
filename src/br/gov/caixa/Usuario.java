package br.gov.caixa;

import java.util.Date;

public interface Usuario {
    public enum Tipo {PF, PJ}
    public enum Situacao {ATIVO, INATIVO}
    public Tipo getClassificacao();
    public Date getDataCadastro();
    public long getId();
    public String getNome();
    public void setNome(String nome);
    public Situacao getStatus();
    public void setStatus(Situacao status);
}
