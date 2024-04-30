package br.gov.caixa.contas;

import br.gov.caixa.operacoes.Operacao.Transacao;
import java.time.LocalDate;

public class ContaCorrente extends Conta {
    public ContaCorrente(long id, long idUsuario) {
        super(id, idUsuario);
    }

    public boolean investir(Transacao investimento) {
        if (investimento.valorReal() <= this.saldo) {
            this.saldo = this.saldo - investimento.valorReal();
            this.historico.add(investimento);
            this.dataAtualizacao = LocalDate.now();
            return true;
        }
        return false;
    }
}
