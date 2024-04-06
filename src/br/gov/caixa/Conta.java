package br.gov.caixa;

import java.util.ArrayList;
import java.util.Date;
import br.gov.caixa.Banco.Transacao;

public class Conta {
    public enum Situacao {ATIVA, INATIVA}
    protected final long id;
    protected double saldo;
    protected final ArrayList<Transacao> historico;
    protected Date dataAtualizacao;
    protected Situacao status;
    protected final long idUsuario;

    public Conta(long id, long idUsuario) {
        this.id = id;
        this.saldo = 0.00;
        this.historico = new ArrayList<Transacao>();
        this.dataAtualizacao = new Date();
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

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
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

    public boolean sacar(Transacao saque) {
        if (saque.valorPretendido() <= this.saldo) {
            this.saldo = this.saldo - saque.valorReal();
            this.historico.add(saque);
            this.dataAtualizacao = new Date();
            return true;
        }
        return false;
    }

    public void depositar(Transacao deposito) {
        // É creditado o valor pretendido, para que a transferência de PJ seja creditada sem a taxa
        this.saldo = this.saldo + deposito.valorPretendido();
        this.historico.add(deposito);
        this.dataAtualizacao = new Date();
    }

    public boolean transferir(Transacao transferencia) {
        if (transferencia.valorReal() <= this.saldo) {
            this.saldo = this.saldo - transferencia.valorReal();
            this.historico.add(transferencia);
            this.dataAtualizacao = new Date();
            return true;
        }
        return false;
    }

    public double consultarSaldo(Transacao consulta) {
        this.historico.add(consulta);
        this.dataAtualizacao = new Date();
        return this.saldo;
    }
}
