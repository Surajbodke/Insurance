package com.insurancems.repository;

import com.insurancems.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByRecipientUsernameAndReadFalseOrderByCreatedAtDesc(String recipientUsername);
    
    long countByRecipientUsernameAndReadFalse(String recipientUsername);
    
    List<Message> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);
    
    List<Message> findBySenderUsernameOrderByCreatedAtDesc(String senderUsername);
} 