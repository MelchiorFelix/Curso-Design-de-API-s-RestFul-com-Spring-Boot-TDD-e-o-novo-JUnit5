package com.curso.melchior.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cliente")
public class MeuResource {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente salvar(@RequestBody Cliente cliente){
        //service.save(cliente);
        return cliente;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        //service.buscarPorId(id);
        //service.delete(cliente);
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @RequestBody Cliente cliente){
        //service.buscarPorId(id);
        //service.update(cliente);
        return cliente;
    }

    @GetMapping("/{id}")
    public Cliente obertDadosCliente(@PathVariable Long id){
        System.out.println(String.format("Id recebido via url: %d", id));
        return new Cliente("John", "000.000.000-00");
    }
}
