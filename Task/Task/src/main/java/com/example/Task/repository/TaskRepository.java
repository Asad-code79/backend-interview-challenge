package com.example.Task.repository;


import com.example.Task.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByIsDeletedFalse();
}
