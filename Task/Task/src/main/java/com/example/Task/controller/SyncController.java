package com.example.Task.controller;

import com.example.Task.service.SyncService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final SyncService syncService;
    public SyncController(SyncService syncService) { this.syncService = syncService; }

    @PostMapping("/sync")
    public Map<String, Object> triggerSync() { return syncService.syncTasks(); }

    @GetMapping("/status")
    public Map<String, Object> getSyncStatus() { return syncService.getSyncStatus(); }

    @PostMapping("/batch")
    public Map<String, Object> batchSync(@RequestBody Map<String,Object> request) { return syncService.batchSync(request); }
}
