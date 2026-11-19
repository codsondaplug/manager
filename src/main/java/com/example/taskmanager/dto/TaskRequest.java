package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
    @Size(max = 2000)
    private String description;
    @NotNull
    private Priority priority;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}
