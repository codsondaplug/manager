package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository tasks;
    @Mock UserRepository users;
    @Mock AuditService auditService;
    @InjectMocks TaskService taskService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAssignsTaskToCurrentUserAndWritesAuditRecord() {
        User owner = user("user@test.com", Role.ROLE_USER);
        authenticate("user@test.com");
        when(users.findByEmail("user@test.com")).thenReturn(Optional.of(owner));
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TaskRequest request = request("My task");

        TaskResponse response = taskService.create(request);

        assertEquals("My task", response.title());
        assertEquals("user@test.com", response.ownerEmail());
        assertEquals(Status.NEW, response.status());
        verify(auditService).record("CREATE", null, "My task");
    }

    @Test
    void userCanSeeOnlyOwnTasks() {
        User owner = user("user@test.com", Role.ROLE_USER);
        authenticate("user@test.com");
        when(users.findByEmail("user@test.com")).thenReturn(Optional.of(owner));
        when(tasks.findAllByOwnerOrderByCreatedAtDesc(owner)).thenReturn(java.util.List.of());

        assertTrue(taskService.getAll().isEmpty());
        verify(tasks).findAllByOwnerOrderByCreatedAtDesc(owner);
        verify(tasks, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void adminCanSeeAllTasks() {
        User admin = user("admin@test.com", Role.ROLE_ADMIN);
        authenticate("admin@test.com");
        when(users.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(tasks.findAllByOrderByCreatedAtDesc()).thenReturn(java.util.List.of());

        assertTrue(taskService.getAll().isEmpty());
        verify(tasks).findAllByOrderByCreatedAtDesc();
    }

    private static TaskRequest request(String title) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription("Description");
        request.setPriority(Priority.MEDIUM);
        return request;
    }

    private static User user(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private static void authenticate(String email) {
        var authentication = new UsernamePasswordAuthenticationToken(email, "n/a");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
