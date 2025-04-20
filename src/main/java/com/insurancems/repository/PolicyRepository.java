package com.insurancems.repository;

import com.insurancems.entity.Customer;
import com.insurancems.entity.Policy;
import com.insurancems.entity.Agent;
import com.insurancems.entity.enums.PolicyStatus;
import com.insurancems.entity.enums.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    // Basic queries
    List<Policy> findByStatus(PolicyStatus status);
    List<Policy> findByPolicyType(PolicyType policyType);
    List<Policy> findByPolicyNumberContaining(String policyNumber);
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    // Date-based queries
    List<Policy> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Policy> findByEndDateBefore(LocalDate date);
    List<Policy> findByEndDateAfter(LocalDate date);
    
    // Amount-based queries
    List<Policy> findByPremiumAmountLessThan(BigDecimal amount);
    List<Policy> findByPremiumAmountGreaterThan(BigDecimal amount);
    List<Policy> findByCoverageAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Customer and Agent related queries
    List<Policy> findByCustomer_Id(Long customerId);
    List<Policy> findByAgent_Id(Long agentId);
    List<Policy> findByCustomer_User_Username(String username);
    List<Policy> findByAgent_User_Username(String username);
    List<Policy> findByAgent(Agent agent);
    
    // Complex queries
    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate < :date")
    List<Policy> findExpiredPoliciesByStatus(@Param("status") PolicyStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT p FROM Policy p WHERE p.customer.id = :customerId AND p.status = :status")
    List<Policy> findCustomerPoliciesByStatus(@Param("customerId") Long customerId, @Param("status") PolicyStatus status);
    
    @Query("SELECT COUNT(p) > 0 FROM Policy p WHERE p.customer.id = :customerId AND p.status = :status")
    boolean hasActivePolicy(@Param("customerId") Long customerId, @Param("status") PolicyStatus status);
    
    @Query("SELECT p FROM Policy p WHERE p.endDate < CURRENT_DATE AND p.status = 'ACTIVE'")
    List<Policy> findExpiredActivePolicies();
    
    @Query("SELECT p FROM Policy p WHERE p.startDate <= CURRENT_DATE AND p.status = 'PENDING'")
    List<Policy> findPendingPoliciesToActivate();
    
    @Query("SELECT SUM(p.premiumAmount) FROM Policy p WHERE p.status = 'ACTIVE'")
    BigDecimal calculateTotalActivePremiums();
    
    @Query("SELECT SUM(p.coverageAmount) FROM Policy p WHERE p.status = 'ACTIVE'")
    BigDecimal calculateTotalActiveCoverage();

    List<Policy> findByCustomer(Customer customer);
    List<Policy> findByCustomerAndStatus(Customer customer, PolicyStatus status);
} 