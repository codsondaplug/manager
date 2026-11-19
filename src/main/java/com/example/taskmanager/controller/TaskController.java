package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.AuditLog;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.service.AuditService;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api")
public class TaskController {
    private final TaskService taskService;
    private final AuditService auditService;

    public TaskController(TaskService taskService, AuditService auditService) {
        this.taskService = taskService;
        this.auditService = auditService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> getAll() {
        return taskService.getAll();
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @RequestParam Status status) {
        return taskService.updateStatus(id, status);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> audit() {
        return auditService.getAll();
    }
}
