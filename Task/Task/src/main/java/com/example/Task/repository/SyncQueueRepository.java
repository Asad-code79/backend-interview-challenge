package com.example.Task.repository;

import com.example.Task.Model.SyncQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncQueueRepository extends JpaRepository<SyncQueue, Long> {

    List<SyncQueue> findByStatus(String status);
    long countByStatus(String status);
}
