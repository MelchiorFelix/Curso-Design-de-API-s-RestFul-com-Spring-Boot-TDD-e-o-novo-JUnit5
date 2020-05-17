package com.cursos;


import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PrimeiroTeste {

    @Test
    public void deveSomar2Numeros(){
            // cenário
            Calculadora calc = new Calculadora();
            int numero1 = 10, numero2 = 5;

            //execução
            int resultado = calc.somar(numero1, numero2);

            //verificações
            Assertions.assertThat(resultado).isEqualTo(15);
    }

    @Test(expected = RuntimeException.class)
    public void naoDevSomarNumerosNegativos(){
        //canário
        Calculadora calc = new Calculadora();
        int num1 = -10,  num2 = 5;

        //execução
        calc.somar(num1,num2);

    }

    @Test
    public void subtracaoDeNumeros(){
        //cenario
        Calculadora calc = new Calculadora();
        int num = 5, num2 = 10;

        //execução
        calc.subtracao(num,num2);
    }

    @Test
    public void smultiplicacaoDeNumeros(){
        //cenário
        Calculadora calc = new Calculadora();
        int num = 8, num2 = 7;

        //excecução
        int  resultado = calc.multiplicacao(num,num2);

        //verificacao
        Assertions.assertThat(resultado).isEqualTo(56);
    }

    @Test
    public void divisaoDeNumeros(){
        //cenário
        Calculadora calc = new Calculadora();
        int num = 10, num2 = 5;

        //execucao
        int resultado = calc.divisao(num,num2);

        //verficacao
        Assertions.assertThat(resultado).isPositive();
    }

    @Test(expected = RuntimeException.class)
    public void divisaoDeNumerosComZero(){
        //cenário
        Calculadora calc = new Calculadora();
        int num = 10, num2 = 0;

        //execucao
        int resultado = calc.divisao(num,num2);


    }

}

class Calculadora {

    int somar(int  num, int num2){
        if(num < 0 || num2 < 0){
            throw new RuntimeException("Não é permitido somar numeros negativos.");
        }
        return num + num2;
    }

    int subtracao(int num, int num2){
        return num - num2;
    }

    int multiplicacao(int num, int num2){
        return num * num2;
    }

    int divisao(int num, int num2){
        if (num == 0){
            throw new RuntimeException("Não é permitido divisao por 0.");
        }
        return num / num2;
    }
}
