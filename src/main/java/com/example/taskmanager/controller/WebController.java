package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.service.AuditService;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private final TaskService taskService;
    private final AuditService auditService;

    public WebController(TaskService taskService, AuditService auditService) {
        this.taskService = taskService;
        this.auditService = auditService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/web/tasks";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/web/tasks")
    public String tasks(Model model, Authentication authentication) {
        model.addAttribute("tasks", taskService.getAll());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("email", authentication.getName());
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("admin", admin);
        if (admin) model.addAttribute("auditLogs", auditService.getAll());
        return "tasks";
    }

    @PostMapping("/web/tasks")
    public String create(@Valid @ModelAttribute TaskRequest request, RedirectAttributes redirect) {
        taskService.create(request);
        redirect.addFlashAttribute("message", "Task created");
        return "redirect:/web/tasks";
    }

    @PostMapping("/web/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Status status) {
        taskService.updateStatus(id, status);
        return "redirect:/web/tasks";
    }

    @PostMapping("/web/tasks/{id}/delete")
    public String delete(@PathVariable Long id) {
        taskService.delete(id);
        return "redirect:/web/tasks";
    }
}
