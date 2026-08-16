package com.example.ledger.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExsistException extends ApiException {
    
    public EmailAlreadyExsistException(String message){
        super(HttpStatus.CONFLICT, message);
    }
}
