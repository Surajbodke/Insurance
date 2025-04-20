package com.insurancems.repository;

import com.insurancems.entity.Customer;
import com.insurancems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
    
    // User-related queries
    Optional<Customer> findByUser_Username(String username);
    Optional<Customer> findByUser_Email(String email);
    List<Customer> findByUser_FirstNameContainingOrUser_LastNameContaining(String firstName, String lastName);
    
    // Address-based queries
    List<Customer> findByAddressContaining(String addressPart);
    
    // Age-based queries
    List<Customer> findByDateOfBirthBefore(LocalDate date);
    List<Customer> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);
    
    // Policy-related queries
    List<Customer> findByPolicies_PolicyType(com.insurancems.entity.enums.PolicyType policyType);
    List<Customer> findByPolicies_Status(com.insurancems.entity.enums.PolicyStatus status);
    
    // Claims-related queries
    List<Customer> findByClaims_Status(com.insurancems.entity.enums.ClaimStatus status);
    List<Customer> findDistinctByClaimsIsNotEmpty();
} 