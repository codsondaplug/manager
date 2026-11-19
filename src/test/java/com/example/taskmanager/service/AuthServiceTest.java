package com.example.taskmanager.service;

import com.example.taskmanager.dto.AuthResponse;
import com.example.taskmanager.dto.AuthRequest;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void registerCreatesUserWithUserRoleAndReturnsJwt() {
        AuthRequest request = new AuthRequest();
        request.setEmail("new@test.com");
        request.setPassword("secret123");
        when(users.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertEquals("new@test.com", captor.getValue().getEmail());
        assertEquals("encoded", captor.getValue().getPassword());
        assertEquals(Role.ROLE_USER, captor.getValue().getRole());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        AuthRequest request = new AuthRequest();
        request.setEmail("same@test.com");
        request.setPassword("secret123");
        when(users.existsByEmail("same@test.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(users, never()).save(any());
    }

    @Test
    void loginAuthenticatesUserAndReturnsJwt() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("pass123");
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("encoded");
        user.setRole(Role.ROLE_USER);
        when(users.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("login-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("login-token", response.token());
    }
}
