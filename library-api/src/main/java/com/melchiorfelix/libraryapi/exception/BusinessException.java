package com.melchiorfelix.libraryapi.exception;

import org.springframework.validation.BindingResult;

public class BusinessException extends RuntimeException {



    public BusinessException(String menssage) {
        super(menssage);
    }


}
