package com.melchiorfelix.libraryapi.api.exception;

import com.melchiorfelix.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;

import javax.xml.transform.sax.SAXResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach( error ->  this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException exception) {
        this.errors = Arrays.asList(exception.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }
}
