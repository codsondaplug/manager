package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        Status status,
        String ownerEmail,
        LocalDateTime createdAt
) {}
