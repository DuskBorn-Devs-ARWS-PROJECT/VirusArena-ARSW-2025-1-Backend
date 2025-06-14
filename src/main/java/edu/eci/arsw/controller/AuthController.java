package edu.eci.arsw.controller;

import edu.eci.arsw.model.dto.AuthDTOs;
import edu.eci.arsw.model.dto.AuthDTOs.LoginRequest;
import edu.eci.arsw.model.dto.AuthDTOs.RegisterRequest;
import edu.eci.arsw.model.dto.AuthDTOs.TokenResponse;
import edu.eci.arsw.exception.AuthException;
import edu.eci.arsw.security.JwtTokenProvider;
import edu.eci.arsw.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import edu.eci.arsw.repository.UserRepository;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          AuthService authService,
                          UserRepository userRepository) { // Añadir este parámetro
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.userRepository = userRepository; // Inicializar el repositorio
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
    public ResponseEntity<Object> register(@RequestBody RegisterRequest registerRequest) {
        try {
            String token = authService.registerUser(registerRequest);
            return ResponseEntity.ok(new AuthDTOs.TokenResponse(token));
        } catch (AuthException e) {
            logger.warn("Error de autenticación durante el registro: {}", e.getMessage());
            Map<String, String> errorResponse = Collections.singletonMap("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error inesperado durante el registro", e);  // Esto registrará el stack trace completo
            Map<String, String> errorResponse = Collections.singletonMap("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/update-role")
    public ResponseEntity<Object> updateUserRole(@RequestBody Map<String, String> request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<edu.eci.arsw.model.User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                edu.eci.arsw.model.User user = userOpt.get();
                user.setRole(request.get("role"));
                userRepository.save(user);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}