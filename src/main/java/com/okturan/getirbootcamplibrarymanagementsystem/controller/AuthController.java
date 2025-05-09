package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.controller.api.AuthApi;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import com.okturan.getirbootcamplibrarymanagementsystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> registerPatron(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        AuthResultDTO result = authService.registerPatron(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }

    @Override
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JwtResponseDTO> registerWithRole(
            @Valid @RequestBody AdminUserRegistrationDTO registrationDTO) {
        AuthResultDTO result = authService.registerWithRole(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResultDTO result = authService.login(loginDTO);
        return ResponseEntity.ok(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }
}
