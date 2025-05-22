package edu.eci.arsw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTOs {
    // Constructor privado para evitar instanciación
    private AuthDTOs() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class TokenResponse {
        private String token;
    }
}