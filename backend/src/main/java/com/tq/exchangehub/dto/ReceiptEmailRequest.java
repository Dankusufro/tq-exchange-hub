package com.tq.exchangehub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ReceiptEmailRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo proporcionado no es válido")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
