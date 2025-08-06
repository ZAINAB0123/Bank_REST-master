package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody AuthRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        User user = userService.findByUsername(loginRequest.getUsername());
        
        return ResponseEntity.ok(new AuthResponseDto(jwt, user.getUsername(), user.getRole().getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody AuthRequestDto registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        User newUser = userService.createUser(registerRequest.getUsername(), registerRequest.getPassword(), "USER");
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponseDto(jwt, newUser.getUsername(), newUser.getRole().getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        return ResponseEntity.ok(new AuthResponseDto(null, user.getUsername(), user.getRole().getId()));
    }
} 