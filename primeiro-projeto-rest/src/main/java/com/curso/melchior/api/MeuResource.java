package com.curso.melchior.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente/")
public class MeuResource {

    @GetMapping("{id}")
    public Cliente obertDadosCliente(@PathVariable Long id){
        System.out.println(String.format("Id recebido via url: %d", id));
        return new Cliente("John", "000.000.000-00");
    }
}
