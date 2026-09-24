package com.melchiorfelix.libraryapi.api.exception;

import com.melchiorfelix.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(String message) {
        this.errors = java.util.List.of(message);
    }

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach( error ->  this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException exception) {
        this.errors = Arrays.asList(exception.getMessage());
    }
    public ApiErrors(ResponseStatusException exception) {
        this.errors = java.util.List.of(exception.getReason() == null ? "Resource not found" : exception.getReason());
    }

    public List<String> getErrors() {
        return errors;
    }
}
