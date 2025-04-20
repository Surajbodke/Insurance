package com.insurancems.service;

import com.insurancems.entity.Message;
import com.insurancems.entity.User;
import com.insurancems.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(String senderUsername, String recipientUsername, String content) {
        Message message = new Message();
        message.setSenderUsername(senderUsername);
        message.setRecipientUsername(recipientUsername);
        message.setContent(content);
        message.setRead(false);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getReceivedMessages(String recipientUsername) {
        return messageRepository.findByRecipientUsernameOrderByCreatedAtDesc(recipientUsername);
    }

    @Transactional(readOnly = true)
    public List<Message> getSentMessages(String senderUsername) {
        return messageRepository.findBySenderUsernameOrderByCreatedAtDesc(senderUsername);
    }

    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(String recipientUsername) {
        return messageRepository.findByRecipientUsernameAndReadFalseOrderByCreatedAtDesc(recipientUsername);
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String recipientUsername) {
        return messageRepository.countByRecipientUsernameAndReadFalse(recipientUsername);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }

    @Transactional
    public void markAllAsRead(String recipientUsername) {
        List<Message> unreadMessages = getUnreadMessages(recipientUsername);
        unreadMessages.forEach(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }
} 