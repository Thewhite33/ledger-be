package com.example.ledger.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentException extends ApiException {
    
    public InvalidPaymentException(String message){
        super(HttpStatus.BAD_REQUEST, message);
    }

}
