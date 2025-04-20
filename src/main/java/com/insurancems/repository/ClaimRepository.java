package com.insurancems.repository;

import com.insurancems.entity.Claim;
import com.insurancems.entity.Customer;
import com.insurancems.entity.enums.ClaimStatus;
import com.insurancems.entity.enums.ClaimType;
import com.insurancems.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByAgent_User_Username(String username);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByPolicy_PolicyNumber(String policyNumber);
    
    // Find claims by customer ID
    List<Claim> findByCustomer_Id(Long customerId);
    
    // Find claims by agent ID
    List<Claim> findByAgent_Id(Long agentId);
    
    // Find claims by type
    List<Claim> findByType(ClaimType type);
    
    // Find claims by date range
    List<Claim> findByClaimDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find claims by amount range
    List<Claim> findByAmountGreaterThanEqual(BigDecimal minAmount);
    List<Claim> findByAmountLessThanEqual(BigDecimal maxAmount);
    
    // Find claims by resolution date
    List<Claim> findByResolutionDateIsNull();
    List<Claim> findByResolutionDateBefore(LocalDate date);
    
    // Find claims by policy ID
    List<Claim> findByPolicy_Id(Long policyId);
    
    // Find claims by customer and status
    List<Claim> findByCustomer_IdAndStatus(Long customerId, ClaimStatus status);
    
    // Find claims by agent and status
    List<Claim> findByAgent_IdAndStatus(Long agentId, ClaimStatus status);

    List<Claim> findByCustomer(Customer customer);

    List<Claim> findByAgent(Agent agent);
} 