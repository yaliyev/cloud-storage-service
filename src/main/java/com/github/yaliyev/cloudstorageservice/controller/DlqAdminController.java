package com.github.yaliyev.cloudstorageservice.controller;

import com.github.yaliyev.cloudstorageservice.service.DlqAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dlq")
public class DlqAdminController {

    private final DlqAdminService dlqAdminService;

    public DlqAdminController(DlqAdminService dlqAdminService) {
        this.dlqAdminService = dlqAdminService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<String>> peekMessages(@RequestParam(defaultValue = "10") int max) {
        return ResponseEntity.ok(dlqAdminService.peekDlqMessages(max));
    }

    @PostMapping("/redrive")
    public ResponseEntity<Map<String, Object>> redriveMessages(@RequestParam(defaultValue = "10") int max) {
        int count = dlqAdminService.redriveMessages(max);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "messagesRedriven", count
        ));
    }
}
