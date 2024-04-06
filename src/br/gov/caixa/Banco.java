package br.gov.caixa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Banco {
    public static final Banco CAIXA = new Banco();
    public enum TipoConta {CORRENTE, POUPANCA, INVESTIMENTO}
    public enum Operacao {SAQUE, DEPOSITO, TRANSFERENCIA, INVESTIMENTO, CONSULTA_SALDO}
    private final ArrayList<Usuario> clientes = new ArrayList<>();
    private final ArrayList<ContaCorrente> contasCorrentes = new ArrayList<>();
    private final ArrayList<ContaPoupanca> contasPoupancas = new ArrayList<>();
    private final ArrayList<ContaInvestimento> contasInvestimento = new ArrayList<>();
    public record Transacao(Date data, Operacao tipo, double valorPretendido, double valorReal, Usuario usuarioOrigem, Usuario usuarioDestino, String observacao) {

    }
    public Banco() {
        new Thread(() -> {
            while (true) {
                Calendar hoje = Calendar.getInstance();
                if (hoje.get(Calendar.DAY_OF_MONTH) == 1) {
                    creditarRendimentos();
                    try {
                        synchronized (this) {
                            Banco.this.wait(28 * 24 * 3600 * 1000L); // espera 28 dias
                        }
                    } catch (InterruptedException iex) {
                        iex.printStackTrace();
                    }
                } else { // se não for o primeiro dia do mês
                    try {
                        synchronized (this) {
                            Banco.this.wait(24 * 3600 * 1000L); // espera um dia
                        }
                    } catch (InterruptedException iex) {
                        iex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public ContaCorrente cadastrarUsuario(String cpf_cnpj, String nome) {
        Usuario novo = new Usuario(cpf_cnpj, nome);
        this.clientes.add(novo);
        ContaCorrente conta = new ContaCorrente(contasCorrentes.size() + 1, novo.getId());
        contasCorrentes.add(conta);
        System.out.printf("Cliente cadastrado com sucesso! Conta corrente nº: %d\n",  conta.getId());
        return conta;
    }

    public ContaPoupanca abrirContaPoupanca(long idCliente) {
        Usuario titular = getUsuario(idCliente);
        if (titular != null) {
            if (titular.getClassificacao() == Usuario.Tipo.PF) {
                ContaPoupanca poupanca = new ContaPoupanca(contasPoupancas.size() + 1, idCliente);
                contasPoupancas.add(poupanca);
                System.out.printf("Poupança nº %d cadastrada com sucesso!\n", poupanca.getId());
                return poupanca;
            } else {
                System.out.println("Poupança não disponível para pessoa jurídica");
            }
        } else {
            System.out.println("CPF/CNPJ não cadastrado!");
        }
        return null;
    }

    public ContaInvestimento abrirContaInvestimento(long idTitular) {
        Usuario titular = getUsuario(idTitular);
            if (titular != null) {
            ContaInvestimento conta = new ContaInvestimento(contasInvestimento.size() + 1, idTitular);
            contasInvestimento.add(conta);
            System.out.printf("Conta Investimento nº %d cadastrada com sucesso!\n",  conta.getId());
            return conta;
        } else {
            System.out.println("CPF/CNPJ não cadastrado!");
        }
        return null;
    }

    public void sacar(long idConta, TipoConta tipo, double valor) {
        Conta conta = getConta(idConta, tipo);
        double valorReal = valor;
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Usuario titular = getUsuario(conta.getIdUsuario());
                if (titular.getClassificacao() == Usuario.Tipo.PJ) {
                    valorReal = valor * 1.005;
                }
                Transacao saque = new Transacao(new Date(), Banco.Operacao.SAQUE, valor, valorReal, getUsuario(conta.idUsuario),
                        null, "");
                if (conta.sacar(saque)) {
                    System.out.println("Saque realizado com sucesso!");
                } else {
                    System.out.println("Saldo insuficiente!");
                }
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void depositar(long idConta, TipoConta tipo, double valor) {
        Conta conta = getConta(idConta, tipo);
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Transacao deposito = new Transacao(new Date(), Banco.Operacao.DEPOSITO, valor, valor, null, getUsuario(conta.idUsuario), "");
                conta.depositar(deposito);
                System.out.println("Depósito realizado com sucesso!");
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void transferir(long idContaOrigem, TipoConta tipoOrigem, long idContaDestino, TipoConta tipoDestino, double valor) {
        Conta contaOrigem = getConta(idContaOrigem, tipoOrigem);
        Conta contaDestino = getConta(idContaDestino, tipoDestino);
        double valorReal = valor;
        if (contaOrigem != null && contaDestino != null) {
            if (contaOrigem.getStatus() == Conta.Situacao.ATIVA) {
                if (contaDestino.getStatus() == Conta.Situacao.ATIVA) {
                    Usuario usuarioOrigem = getUsuario(contaOrigem.getIdUsuario());
                    Usuario usuarioDestino = getUsuario(contaDestino.getIdUsuario());
                    if (usuarioOrigem.getClassificacao() == Usuario.Tipo.PJ) {
                        valorReal = valor * 1.005;
                    }
                    if (usuarioDestino != null && usuarioDestino.getStatus() == Usuario.Situacao.ATIVO) {
                        String observacao = tipoDestino == TipoConta.CORRENTE ? "Crédito em Conta Corrente" : "";
                        Transacao transferencia = new Transacao(new Date(), Banco.Operacao.TRANSFERENCIA, valor, valorReal,
                                getUsuario(contaOrigem.getIdUsuario()), usuarioDestino, observacao);
                        if (contaOrigem.transferir(transferencia)) {
                            contaDestino.depositar(transferencia);
                            System.out.println("Transferência realizada com sucesso!");
                        } else {
                            System.out.println("Saldo insuficiente!");
                        }
                    } else {
                        System.out.println("Cliente de destino inválido!");
                    }
                } else {
                    System.out.println("Conta destino inativa!");
                }
            } else {
                System.out.println("Conta origem inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void consultarSaldo(long id, TipoConta tipo) {
        Conta conta = getConta(id, tipo);
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Transacao consulta = new Transacao(new Date(), Banco.Operacao.CONSULTA_SALDO, 0.0, 0.0,
                        getUsuario(conta.getIdUsuario()), null,"");
                System.out.printf("Saldo: R$ %.2f\n", conta.consultarSaldo(consulta));
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public ContaInvestimento investir(long id, double valor) {
        ContaCorrente contaOrigem = (ContaCorrente) getConta(id, TipoConta.CORRENTE);
        if (contaOrigem != null) {
            if (contaOrigem.getStatus() == Conta.Situacao.ATIVA) {
                Usuario usuario = getUsuario(contaOrigem.getIdUsuario());
                ContaInvestimento contaDestino = (ContaInvestimento) getConta(usuario, TipoConta.INVESTIMENTO);
                if (contaDestino == null) {
                    contaDestino = this.abrirContaInvestimento(usuario.getId());
                }
                if (contaDestino.getStatus() == Conta.Situacao.ATIVA) {
                    Transacao investimento = new Transacao(new Date(), Banco.Operacao.INVESTIMENTO, valor, valor, usuario, usuario,
                            "");
                    if (contaOrigem.investir(investimento)) {
                        contaDestino.depositar(investimento);
                    } else {
                        System.out.println("Saldo insuficiente!");
                    }
                } else {
                    System.out.println("Conta investimento inativa!");
                }
                return contaDestino;
            } else {
                System.out.println("Conta corrente inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
        return null;
    }

    // Procura a conta de um usuário pelo tipo
    public Conta getConta(Usuario titular, TipoConta tipo) {
        ArrayList<? extends Conta> contas;
        if (tipo == TipoConta.CORRENTE) {
            contas = this.contasCorrentes;
        } else if (tipo == TipoConta.POUPANCA) {
            contas = this.contasPoupancas;
        } else {
            contas = this.contasInvestimento;
        }
        for (Conta conta : contas) {
            if (conta.getIdUsuario() == titular.getId()) {
                return conta;
            }
        }
        return null;
    }

    // Procura uma conta pelo número e pelo tipo
    public Conta getConta(long id, TipoConta tipo) {
        ArrayList<? extends Conta> contas;
        if (tipo == TipoConta.CORRENTE) {
            contas = this.contasCorrentes;
        } else if (tipo == TipoConta.POUPANCA) {
            contas = this.contasPoupancas;
        } else {
            contas = this.contasInvestimento;
        }
        for (Conta conta : contas) {
            if (conta.getId() == id) {
                return conta;
            }
        }
        return null;
    }

    //Procura um cliente pelo CPF/CNPJ
    public Usuario getUsuario(long id) {
        for (Usuario cliente : this.clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }

    private void creditarRendimentos() {
        for (ContaInvestimento conta : this.contasInvestimento) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Usuario titular = getUsuario(conta.getIdUsuario());
                if (titular != null) {
                    if (titular.getClassificacao() == Usuario.Tipo.PF) {
                        conta.setSaldo(conta.getSaldo() * 1.01);
                    } else { // PJ
                        conta.setSaldo(conta.getSaldo() * 1.02);
                    }
                } else {
                    System.out.printf("Titular da conta %d não cadastrado!\n", conta.getId());
                }
            }
        }
    }

    public static void main(String[] args) {
        ContaCorrente contaPF = Banco.CAIXA.cadastrarUsuario("123.456.789-00", "Fulano de Tal");
        ContaCorrente contaPJ = Banco.CAIXA.cadastrarUsuario("12.345.678/9000-00", "Sociedade Anônima S.A.");
        ContaPoupanca poupanca = Banco.CAIXA.abrirContaPoupanca(contaPF.getIdUsuario());
        Banco.CAIXA.depositar(contaPF.getId(), TipoConta.CORRENTE, 200.00);
        Banco.CAIXA.sacar(contaPF.getId(), TipoConta.CORRENTE, 100.00);
        Banco.CAIXA.depositar(poupanca.getId(), TipoConta.POUPANCA, 300.00);
        Banco.CAIXA.sacar(poupanca.getId(), TipoConta.POUPANCA, 250.00);
        Banco.CAIXA.transferir(contaPF.getId(), TipoConta.CORRENTE, poupanca.getId(), TipoConta.POUPANCA, 50.00);
        ContaInvestimento investimentoPF = Banco.CAIXA.investir(contaPF.getId(), 50.00);
        Banco.CAIXA.consultarSaldo(contaPF.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(poupanca.getId(), TipoConta.POUPANCA);
        Banco.CAIXA.consultarSaldo(investimentoPF.getId(), TipoConta.INVESTIMENTO);
        Banco.CAIXA.depositar(contaPJ.getId(), TipoConta.CORRENTE, 10000.00);
        Banco.CAIXA.sacar(contaPJ.getId(), TipoConta.CORRENTE, 1000.00);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.transferir(contaPJ.getId(), TipoConta.CORRENTE, contaPF.getId(), TipoConta.CORRENTE, 2000.00);
        Banco.CAIXA.consultarSaldo(contaPF.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        ContaInvestimento investimentoPJ = Banco.CAIXA.investir(contaPJ.getId(), 5000.00);
        Banco.CAIXA.investir(contaPJ.getId(), 1000.00);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(investimentoPJ.getId(), TipoConta.INVESTIMENTO);
        investimentoPF.setStatus(Conta.Situacao.INATIVA);
        // Método sendo chamado aqui apenas pra teste. Deve ser chamado apenas na thread do construtor
        Banco.CAIXA.creditarRendimentos();
        Banco.CAIXA.consultarSaldo(investimentoPF.getId(), TipoConta.INVESTIMENTO);
        System.out.println(investimentoPF.getSaldo());
        Banco.CAIXA.consultarSaldo(investimentoPJ.getId(), TipoConta.INVESTIMENTO);
        for (Transacao registro: contaPF.getHistorico()) {
            System.out.println(registro);
        }
    }
}
