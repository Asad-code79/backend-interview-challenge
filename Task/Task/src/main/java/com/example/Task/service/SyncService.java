package com.example.Task.service;

import com.example.Task.Model.SyncQueue;
import com.example.Task.Model.Task;
import com.example.Task.repository.SyncQueueRepository;
import com.example.Task.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SyncService {

    private final SyncQueueRepository queueRepo;
    private final TaskRepository taskRepo;
    private final ObjectMapper objectMapper;  // <-- use Spring's ObjectMapper

    @Value("${sync.batch.size:50}")
    private int batchSize;

    // Constructor injection for ObjectMapper
    public SyncService(SyncQueueRepository queueRepo,
                       TaskRepository taskRepo,
                       ObjectMapper objectMapper) {
        this.queueRepo = queueRepo;
        this.taskRepo = taskRepo;
        this.objectMapper = objectMapper;  // Spring Boot auto-configured ObjectMapper
    }


    public Map<String, Object> syncTasks() {
        List<SyncQueue> pending = queueRepo.findByStatus("pending");
        int synced = 0, failed = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        for (SyncQueue item : pending.stream().limit(batchSize).toList()) {
            try {
                Task task = objectMapper.readValue(item.getTaskData(), Task.class);
                task.setSyncStatus("synced");
                task.setLastSyncedAt(LocalDateTime.now());
                taskRepo.save(task);
                queueRepo.delete(item);
                synced++;
            } catch (Exception e) {
                item.setRetryAttempts(item.getRetryAttempts() + 1);
                if (item.getRetryAttempts() >= 3) item.setStatus("error");
                queueRepo.save(item);

                Map<String, Object> err = new HashMap<>();
                err.put("task_id", item.getTaskId());
                err.put("operation", item.getOperationType());
                err.put("error", e.getMessage());
                err.put("timestamp", LocalDateTime.now().toString());
                errors.add(err);
                failed++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("synced_items", synced);
        result.put("failed_items", failed);
        result.put("errors", errors);
        return result;
    }

    public Map<String, Object> getSyncStatus() {
        long pendingCount = queueRepo.countByStatus("pending");
        Map<String, Object> status = new HashMap<>();
        status.put("pending_sync_count", pendingCount);
        status.put("last_sync_timestamp", LocalDateTime.now().toString());
        status.put("is_online", true);
        status.put("sync_queue_size", pendingCount);
        return status;
    }

    @Transactional
    public Map<String, Object> batchSync(Map<String, Object> request) {
        List<Map<String, Object>> processedItems = new ArrayList<>();

        // Extract items from request
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        if (items == null) items = new ArrayList<>();

        for (Map<String, Object> item : items) {
            try {
                String operation = (String) item.get("operation");
                String taskId = (String) item.get("task_id");
                Map<String, Object> data = (Map<String, Object>) item.get("data");

                Task task = null;
                Map<String, Object> resolvedData = new HashMap<>();

                switch (operation) {
                    case "create":
                        task = new Task();
                        task.setId(UUID.randomUUID().toString());
                        task.setTitle((String) data.get("title"));
                        task.setDescription((String) data.get("description"));
                        task.setCompleted(false);
                        task.setDeleted(false);
                        task.setCreatedAt(LocalDateTime.now());
                        task.setUpdatedAt(LocalDateTime.now());
                        task.setSyncStatus("synced");
                        task.setServerId("srv_" + task.getId());
                        task.setLastSyncedAt(LocalDateTime.now());
                        taskRepo.save(task);

                        resolvedData.put("id", task.getServerId());
                        resolvedData.put("title", task.getTitle());
                        resolvedData.put("description", task.getDescription());
                        resolvedData.put("completed", task.isCompleted());
                        resolvedData.put("created_at", task.getCreatedAt().toString());
                        resolvedData.put("updated_at", task.getUpdatedAt().toString());
                        break;

                    case "update":
                        task = taskRepo.findById(taskId).orElse(null);
                        if (task != null) {
                            task.setTitle((String) data.getOrDefault("title", task.getTitle()));
                            task.setDescription((String) data.getOrDefault("description", task.getDescription()));
                            task.setCompleted((Boolean) data.getOrDefault("completed", task.isCompleted()));
                            task.setUpdatedAt(LocalDateTime.now());
                            task.setSyncStatus("synced");
                            taskRepo.save(task);

                            resolvedData.put("id", task.getServerId());
                            resolvedData.put("title", task.getTitle());
                            resolvedData.put("description", task.getDescription());
                            resolvedData.put("completed", task.isCompleted());
                            resolvedData.put("created_at", task.getCreatedAt().toString());
                            resolvedData.put("updated_at", task.getUpdatedAt().toString());
                        } else {
                            resolvedData.put("error", "Task not found for update");
                        }
                        break;

                    case "delete":
                        task = taskRepo.findById(taskId).orElse(null);
                        if (task != null) {
                            task.setDeleted(true);
                            task.setUpdatedAt(LocalDateTime.now());
                            task.setSyncStatus("synced");
                            taskRepo.save(task);

                            resolvedData.put("id", task.getServerId());
                            resolvedData.put("deleted", true);
                        } else {
                            resolvedData.put("error", "Task not found for delete");
                        }
                        break;
                }

                // Build processed item
                Map<String, Object> processed = new HashMap<>();
                processed.put("client_id", taskId);
                processed.put("server_id", task != null ? task.getServerId() : null);
                processed.put("status", resolvedData.containsKey("error") ? "error" : "success");
                processed.put("resolved_data", resolvedData);

                processedItems.add(processed);

            } catch (Exception e) {
                // Catch any errors per item
                Map<String, Object> errorItem = new HashMap<>();
                errorItem.put("client_id", item.get("task_id"));
                errorItem.put("server_id", null);
                errorItem.put("status", "error");
                Map<String, Object> resolvedData = new HashMap<>();
                resolvedData.put("error", e.getMessage());
                errorItem.put("resolved_data", resolvedData);
                processedItems.add(errorItem);
            }
        }

        // Build final response
        Map<String, Object> response = new HashMap<>();
        response.put("processed_items", processedItems);
        return response;
    }

}
