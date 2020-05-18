package com.cursos;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrimeiroTeste {

    Calculadora calc;
    int num1 = 10, num2 = 5;

    @BeforeEach
    public void setUp(){
        calc = new Calculadora();
    }


    @Test
    public void deveSomar2Numeros(){
            // cenário


            //execução
            int resultado = calc.somar(num1, num2);

            //verificações
            Assertions.assertThat(resultado).isEqualTo(15);
    }

    @Test
    public void naoDevSomarNumerosNegativos(){
        //canário


        //execução
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> calc.somar(-num1,num2));

    }

    @Test
    public void subtracaoDeNumeros(){
        //cenario

        //execução
        int resultado = calc.subtracao(num1,num2);

        //verrificacao
        Assertions.assertThat(resultado).isEqualTo(5);
    }

    @Test
    public void multiplicacaoDeNumeros(){
        //cenário

        //excecução
        int  resultado = calc.multiplicacao(num1,num2);

        //verificacao
        Assertions.assertThat(resultado).isEqualTo(50);
    }

    @Test
    public void divisaoDeNumeros(){
        //cenário

        //execucao
        float resultado = calc.divisao(num1,num2);

        //verficacao
        Assertions.assertThat(resultado).isEqualTo(2);
    }

    @Test
    public void divisaoDeNumerosComZero(){
        //cenário
         num2 = 0;

        //execucao
        org.junit.jupiter.api.Assertions
                .assertThrows(ArithmeticException.class, () ->  calc.divisao(num1,num2));


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

        float divisao(int num, int num2){
            if (num == 0){
                throw new ArithmeticException("Não é permitido divisao por 0.");
            }
            return num / num2;
        }
    }
