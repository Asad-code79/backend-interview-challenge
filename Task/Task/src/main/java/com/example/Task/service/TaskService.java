package com.example.Task.service;


import com.example.Task.Model.SyncQueue;
import com.example.Task.Model.Task;
import com.example.Task.exception.TaskNotFoundException;
import com.example.Task.repository.SyncQueueRepository;
import com.example.Task.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SyncQueueRepository syncQueueRepository;
    private final ObjectMapper objectMapper; // <-- Spring's ObjectMapper

    // Inject ObjectMapper via constructor
    public TaskService(TaskRepository taskRepository,
                       SyncQueueRepository syncQueueRepository,
                       ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.syncQueueRepository = syncQueueRepository;
        this.objectMapper = objectMapper; // Spring Boot auto-configured
    }

    public List<Task> getAllTasks() {
        return taskRepository.findByIsDeletedFalse();
    }

    public Task getTask(String id) {
        return taskRepository.findById(id)
                .filter(task -> !task.isDeleted())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }

    @Transactional
    public Task createTask(Task task) throws Exception {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setCompleted(false);
        task.setDeleted(false);
        task.setSyncStatus("pending");
        task.setServerId(null);
        task.setLastSyncedAt(null);

        Task savedTask = taskRepository.save(task);
        addToSyncQueue(savedTask, "create");
        return savedTask;
    }

    @Transactional
    public Task updateTask(String id, Task updates) throws Exception {
        Task task = getTask(id);

        task.setTitle(updates.getTitle() != null ? updates.getTitle() : task.getTitle());
        task.setDescription(updates.getDescription() != null ? updates.getDescription() : task.getDescription());
        task.setCompleted(updates.isCompleted());
        task.setUpdatedAt(LocalDateTime.now());
        task.setSyncStatus("pending");

        Task savedTask = taskRepository.save(task);
        addToSyncQueue(savedTask, "update");
        return savedTask;
    }

    @Transactional
    public void deleteTask(String id) throws Exception {
        Task task = getTask(id);

        task.setDeleted(true);
        task.setUpdatedAt(LocalDateTime.now());
        task.setSyncStatus("pending");

        taskRepository.save(task);
        addToSyncQueue(task, "delete");
    }

    private void addToSyncQueue(Task task, String operationType) throws Exception {
        SyncQueue queueItem = new SyncQueue();
        queueItem.setTaskId(task.getId());
        queueItem.setOperationType(operationType);
        queueItem.setTaskData(objectMapper.writeValueAsString(task));
        queueItem.setRetryAttempts(0);
        queueItem.setStatus("pending");

        syncQueueRepository.save(queueItem);
    }
}

