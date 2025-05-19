package edu.eci.arsw.controller;

import edu.eci.arsw.model.dto.AuthDTOs;
import edu.eci.arsw.model.dto.AuthDTOs.LoginRequest;
import edu.eci.arsw.model.dto.AuthDTOs.RegisterRequest;
import edu.eci.arsw.model.dto.AuthDTOs.TokenResponse;
import edu.eci.arsw.exception.AuthException;
import edu.eci.arsw.security.JwtTokenProvider;
import edu.eci.arsw.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.createToken(authentication);

            return ResponseEntity.ok(new TokenResponse(token));
        } catch (Exception e) {
            throw new AuthException("Usuario o contraseña incorrectos");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            String token = authService.registerUser(registerRequest);
            return ResponseEntity.ok(new AuthDTOs.TokenResponse(token));
        } catch (AuthException e) {
            // **CAMBIO AQUI:** Envuelve el mensaje de error en un objeto JSON
            Map<String, String> errorResponse = Collections.singletonMap("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // **CAMBIO AQUI:** También para errores inesperados
            System.err.println("Error durante el registro: " + e.getMessage());
            Map<String, String> errorResponse = Collections.singletonMap("message", "Error interno del servidor al intentar registrar el usuario.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}