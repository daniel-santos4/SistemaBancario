package br.gov.caixa;

import java.util.Date;
import br.gov.caixa.Banco.Transacao;

public class ContaCorrente extends Conta {
    public ContaCorrente(long id, long idUsuario) {
        super(id, idUsuario);
    }

    public boolean investir(Transacao investimento) {
        if (investimento.valorReal() <= this.saldo) {
            this.saldo = this.saldo - investimento.valorReal();
            this.historico.add(investimento);
            this.dataAtualizacao = new Date();
            return true;
        }
        return false;
    }
}
