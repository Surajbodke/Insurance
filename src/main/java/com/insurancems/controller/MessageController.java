package com.insurancems.controller;

import com.insurancems.entity.Message;
import com.insurancems.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public String redirectToInbox() {
        return "redirect:/messages/inbox";
    }

    @GetMapping("/inbox")
    public String viewInbox(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Message> receivedMessages = messageService.getReceivedMessages(username);
        long unreadCount = messageService.getUnreadMessageCount(username);
        
        model.addAttribute("messages", receivedMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "messages/inbox";
    }

    @GetMapping("/sent")
    public String viewSentMessages(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Message> sentMessages = messageService.getSentMessages(username);
        
        model.addAttribute("messages", sentMessages);
        return "messages/sent";
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(
            @RequestParam String recipientUsername,
            @RequestParam String content,
            Authentication authentication) {
        try {
            String senderUsername = authentication.getName();
            Message message = messageService.sendMessage(senderUsername, recipientUsername, content);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to send message: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{messageId}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long messageId) {
        try {
            messageService.markAsRead(messageId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark message as read: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            messageService.markAllAsRead(username);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All messages marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark all messages as read: " + e.getMessage()
            ));
        }
    }
} 