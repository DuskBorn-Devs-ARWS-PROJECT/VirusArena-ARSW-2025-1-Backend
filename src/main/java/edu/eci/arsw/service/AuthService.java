package edu.eci.arsw.service;

import edu.eci.arsw.model.dto.AuthDTOs.RegisterRequest;
import edu.eci.arsw.exception.AuthException;
import edu.eci.arsw.model.User;
import edu.eci.arsw.repository.UserRepository;
import edu.eci.arsw.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar SimpleGrantedAuthority
import java.util.Collections; // Importar Collections

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AuthException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        user.setRole("ROLE_USER");

        userRepository.save(user);

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        var principal = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        return jwtTokenProvider.createToken(authentication);
    }
}