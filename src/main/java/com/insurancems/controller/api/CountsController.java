package com.insurancems.controller.api;

import com.insurancems.entity.User;
import com.insurancems.repository.UserRepository;
import com.insurancems.service.MessageService;
import com.insurancems.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CountsController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getCounts(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Long> counts = new HashMap<>();
        counts.put("notificationCount", notificationService.getUnreadNotificationCount(user));
        counts.put("messageCount", messageService.getUnreadMessageCount(username));
        
        return ResponseEntity.ok(counts);
    }
} 