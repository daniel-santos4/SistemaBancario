package br.gov.caixa;

import java.util.Date;
import br.gov.caixa.Banco.Operacao;

public final class Registro {
    private final Date data;
    private final Operacao tipo;
    private final double valorPretendido;
    private final double valorReal;
    private final Usuario usuarioOrigem;
    private final Usuario usuarioDestino;
    private final String observacao;

    public Registro(Operacao tipo, double valorPretendido, double valorReal, Usuario usuarioOrigem, Usuario usuarioDestino, String observacao) {
        this.data = new Date();
        this.tipo = tipo;
        this.valorPretendido = valorPretendido;
        this.valorReal = valorReal;
        this.usuarioOrigem = usuarioOrigem;
        this.usuarioDestino = usuarioDestino;
        this.observacao = observacao;
    }

    public Date getData() {
        return data;
    }

    public double getValorPretendido() {
        return valorPretendido;
    }

    public double getValorReal() {
        return valorReal;
    }

    public Usuario getUsuarioOrigem() {
        return usuarioOrigem;
    }

    public Usuario getUsuarioDestino() {
        return usuarioDestino;
    }

    public String getObservacao() {
        return observacao;
    }

    @Override
    public String toString() {
        String registro = "";
        String dataRegistro = String.format("%02d/%02d/%4d", this.data.getDate(), this.data.getMonth() + 1,
                this.data.getYear() + 1900);
        switch (this.tipo) {
            case SAQUE:
                registro = String.format("Saque em %s no valor de R$ %.2f", dataRegistro, this.valorPretendido);
                break;
            case DEPOSITO:
                registro = String.format("Depósito em %s no valor de R$ %.2f", dataRegistro, this.valorPretendido);
                break;
            case INVESTIMENTO: registro = String.format("Investimento em %s no valor de R$ %.2f", dataRegistro,
                    this.valorPretendido);
                break;
            case TRANSFERENCIA:
                registro = String.format("Transferência em %s no valor de R$ %.2f de %s para %s", dataRegistro,
                        this.valorPretendido, this.usuarioOrigem.getNome(), this.usuarioDestino.getNome());
                break;
            case CONSULTA_SALDO:
                registro = String.format("Consulta de saldo em %s", dataRegistro);
                break;
        }
        if (!this.observacao.equals("")) {
            registro = registro + " OBS: " + this.observacao;
        }
        return registro;
    }
}
