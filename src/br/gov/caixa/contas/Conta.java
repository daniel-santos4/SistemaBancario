package br.gov.caixa.contas;

import br.gov.caixa.operacoes.Operacao.Transacao;
import java.time.LocalDate;
import java.util.ArrayList;


public class Conta {
    public enum Situacao {ATIVA, INATIVA}
    protected final long id;
    protected double saldo;
    protected final ArrayList<Transacao> historico;
    protected LocalDate dataAtualizacao;
    protected Situacao status;
    protected final long idUsuario;

    public Conta(long id, long idUsuario) {
        this.id = id;
        this.saldo = 0.00;
        this.historico = new ArrayList<Transacao>();
        this.dataAtualizacao = LocalDate.now();
        this.status = Situacao.ATIVA;
        this.idUsuario = idUsuario;
    }

    public long getId() {
        return id;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public ArrayList<Transacao> getHistorico() {
        return historico;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public boolean debitar(Transacao debito) {
        if (debito.valorReal() <= this.saldo) {
            this.saldo = this.saldo - debito.valorReal();
            this.historico.add(debito);
            this.dataAtualizacao = LocalDate.now();
            return true;
        }
        return false;
    }

    public void creditar(Transacao credito) {
        // É creditado o valor pretendido, para que a transferência de PJ seja creditada sem a taxa
        this.saldo = this.saldo + credito.valorPretendido();
        this.historico.add(credito);
        this.dataAtualizacao = LocalDate.now();
    }

    public double consultarSaldo(Transacao consulta) {
        this.historico.add(consulta);
        this.dataAtualizacao = LocalDate.now();
        return this.saldo;
    }
}
