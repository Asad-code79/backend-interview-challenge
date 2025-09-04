package com.example.Task.Model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

    @Entity
    public class SyncQueue {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String taskId;
        private String operationType; // create, update, delete
        @Column(columnDefinition = "TEXT")
        private String taskData; // JSON of task
        private int retryAttempts;
        private String status; // pending, error
        private LocalDateTime createdAt = LocalDateTime.now();

        public SyncQueue() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public String getTaskData() {
            return taskData;
        }

        public void setTaskData(String taskData) {
            this.taskData = taskData;
        }

        public int getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
