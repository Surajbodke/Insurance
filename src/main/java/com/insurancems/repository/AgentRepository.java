package com.insurancems.repository;

import com.insurancems.entity.Agent;
import com.insurancems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUser(User user);
    Optional<Agent> findByUser_Username(String username);
    Optional<Agent> findByUser_Email(String email);
    Optional<Agent> findByLicenseNumber(String licenseNumber);
} 