package com.example.taskmanager.service;

import com.example.taskmanager.annotation.Loggable;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;
    private final AuditService auditService;

    public TaskService(TaskRepository tasks, UserRepository users, AuditService auditService) {
        this.tasks = tasks;
        this.users = users;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll() {
        User user = currentUser();
        List<Task> result = user.getRole() == Role.ROLE_ADMIN
                ? tasks.findAllByOrderByCreatedAtDesc()
                : tasks.findAllByOwnerOrderByCreatedAtDesc(user);
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return toResponse(findOwnedTask(id));
    }

    @Loggable
    public TaskResponse create(TaskRequest request) {
        User user = currentUser();
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(Status.NEW);
        task.setOwner(user);
        Task saved = tasks.save(task);
        auditService.record("CREATE", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Loggable
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findOwnedTask(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        Task saved = tasks.save(task);
        auditService.record("UPDATE", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    public TaskResponse updateStatus(Long id, Status status) {
        Task task = findOwnedTask(id);
        task.setStatus(status);
        Task saved = tasks.save(task);
        auditService.record("UPDATE_STATUS", saved.getId(), status.name());
        return toResponse(saved);
    }

    @Loggable
    public void delete(Long id) {
        Task task = findOwnedTask(id);
        tasks.delete(task);
        auditService.record("DELETE", id, task.getTitle());
    }

    private Task findOwnedTask(Long id) {
        Task task = tasks.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + id));
        User user = currentUser();
        if (user.getRole() != Role.ROLE_ADMIN
                && (task.getOwner() == null || !task.getOwner().getId().equals(user.getId()))) {
            throw new AccessDeniedException("You cannot access this task");
        }
        return task;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(),
                task.getPriority(), task.getStatus(), task.getOwner().getEmail(), task.getCreatedAt());
    }
}
