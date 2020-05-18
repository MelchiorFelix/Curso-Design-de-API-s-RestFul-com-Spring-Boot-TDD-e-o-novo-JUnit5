package com.cursos;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CadastroPessoasTest {

    @Test
    @DisplayName("Deve criar o Cadastro de pessoas.")
    public void deveCriarOCadastroDePessoas(){
        //canário e execução
        CadastroPessoas cadastro = new CadastroPessoas();

        //verificacao
        Assertions.assertThat(cadastro.getPessoas()).isEmpty();
    }

    @Test
    @DisplayName("Deve adicionar uma pessoa.")
    public void deveAdidcionarUmaPessoa(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("John");

        //execucao
        cadastroPessoas.adicionar(pessoa);

        //verificacao
        Assertions.assertThat(cadastroPessoas.getPessoas())
                .isNotEmpty()
                .hasSize(1)
                .contains(pessoa);

    }

    @Test
    @DisplayName("Não deve adicionar uma pessoa com nome vazio.")
    public void naoDeveAdicionarPessoaComNomeVazio(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();

        //execucao
        org.junit.jupiter.api.Assertions
                .assertThrows(PessoaSemNotException.class, () -> cadastroPessoas.adicionar(pessoa) );

    }

    @Test
    @DisplayName("Deve remover uma pessoa.")
    public void deveRemoverUmaPessoa(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("John");
        cadastroPessoas.adicionar(pessoa);

        //execucao
        cadastroPessoas.remover(pessoa);

        //verificacao
        Assertions.assertThat(cadastroPessoas.getPessoas()).isEmpty();


    }

    @Test
    @DisplayName("Deve lançar um erro ao tentar remover uma pessoa inexistente.")
    public void deveLancarErrorAoTentarRemoverPessoaInexistente(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();

        //execucacao
        org.junit.jupiter.api.Assertions.assertThrows(CadastroVazioException.class , () -> cadastroPessoas.remover(pessoa));

    }


}
