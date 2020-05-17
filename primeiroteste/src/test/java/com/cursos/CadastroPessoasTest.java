package com.cursos;


import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CadastroPessoasTest {

    @Test
    public void deveCriarOCadastroDePessoas(){
        //canário e execução
        CadastroPessoas cadastro = new CadastroPessoas();

        //verificacao
        Assertions.assertThat(cadastro.getPessoas()).isEmpty();
    }

    @Test
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

    @Test(expected = PessoaSemNotException.class)
    public void naoDeveAdicionarPessoaComNomeVazio(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();

        //execucao
        cadastroPessoas.adicionar(pessoa);

        //verificacao
        cadastroPessoas.adicionar(pessoa);

    }

    @Test
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

    @Test(expected = CadastroVazioException.class)
    public void deveLancarErrorAoTentarRemoverPessoaInexistente(){
        //cenario
        CadastroPessoas cadastroPessoas = new CadastroPessoas();
        Pessoa pessoa = new Pessoa();

        //execucacao
        cadastroPessoas.remover(pessoa);

    }


}
