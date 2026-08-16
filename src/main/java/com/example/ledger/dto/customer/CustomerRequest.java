package com.example.ledger.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 120, message = "Customer name must be at most 120 characters")
    private String name;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @Pattern(
            regexp = "^$|^\\+?[0-9]{7,15}$",
            message = "Phone number format is invalid"
    )
    private String phone;

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }
}