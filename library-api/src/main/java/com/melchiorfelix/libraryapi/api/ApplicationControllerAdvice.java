package com.melchiorfelix.libraryapi.api;

import com.melchiorfelix.libraryapi.api.exception.ApiErrors;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrors handleDataConflict() {
        return new ApiErrors("A unique identifier is already registered or the record is still referenced");
    }

    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrors handleConcurrentUpdate() {
        return new ApiErrors("Another circulation operation is in progress; please retry");
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleUnreadableRequest() {
        return new ApiErrors("Request body contains invalid JSON or field values");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException exception){
        BindingResult bindingResult = exception.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessExceptions(BusinessException exception){
        return new ApiErrors(exception);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex){
        return new ResponseEntity(new ApiErrors(ex), ex.getStatusCode());

    }
}
