package com.example.Task.controller;


import com.example.Task.Model.Task;
import com.example.Task.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks - Get all non-deleted tasks
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // GET /api/tasks/{id} - Get a specific task
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable String id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    // POST /api/tasks - Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) throws Exception {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // PUT /api/tasks/{id} - Update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id,
                                           @RequestBody Task updates) throws Exception {
        Task updatedTask = taskService.updateTask(id, updates);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE /api/tasks/{id} - Soft delete a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) throws Exception {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
