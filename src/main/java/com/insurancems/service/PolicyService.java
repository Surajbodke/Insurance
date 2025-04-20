package com.insurancems.service;

import com.insurancems.dto.CustomerPremiumDTO;
import com.insurancems.entity.Customer;
import com.insurancems.entity.Policy;
import com.insurancems.entity.User;
import com.insurancems.entity.enums.PolicyStatus;
import com.insurancems.entity.enums.PolicyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PolicyService {
    
    // Dummy data initialization
    private final List<Policy> dummyPolicies;
    private final List<Customer> dummyCustomers;
    
    public PolicyService() {
        this.dummyCustomers = createDummyCustomers();
        this.dummyPolicies = createDummyPolicies();
    }
    
    private List<Customer> createDummyCustomers() {
        List<Customer> customers = new ArrayList<>();
        
        // Create dummy customers with their users
        customers.add(createCustomer(1L, "John", "Doe", "john.doe@example.com"));
        customers.add(createCustomer(2L, "Jane", "Smith", "jane.smith@example.com"));
        customers.add(createCustomer(3L, "Robert", "Johnson", "robert.j@example.com"));
        customers.add(createCustomer(4L, "Maria", "Garcia", "maria.g@example.com"));
        customers.add(createCustomer(5L, "James", "Wilson", "james.w@example.com"));
        
        return customers;
    }
    
    private Customer createCustomer(Long id, String firstName, String lastName, String email) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUser(user);
        return customer;
    }
    
    private List<Policy> createDummyPolicies() {
        List<Policy> policies = new ArrayList<>();
        Random random = new Random();
        LocalDate now = LocalDate.now();
        
        // Create policies for each customer with different types and statuses
        for (Customer customer : dummyCustomers) {
            // Add multiple policies per customer
            for (int i = 0; i < random.nextInt(3) + 1; i++) {
                Policy policy = new Policy();
                policy.setId((long) policies.size() + 1);
                policy.setCustomer(customer);
                policy.setPolicyType(PolicyType.values()[random.nextInt(PolicyType.values().length)]);
                policy.setStatus(PolicyStatus.values()[random.nextInt(PolicyStatus.values().length)]);
                policy.setPremiumAmount(BigDecimal.valueOf(1000 + random.nextDouble() * 4000)
                    .setScale(2, RoundingMode.HALF_UP));
                policy.setCoverageAmount(BigDecimal.valueOf(50000 + random.nextDouble() * 450000)
                    .setScale(2, RoundingMode.HALF_UP));
                
                // Set dates
                LocalDate startDate = now.minusMonths(random.nextInt(12));
                policy.setStartDate(startDate);
                policy.setEndDate(startDate.plusYears(1));
                
                policies.add(policy);
            }
        }
        
        return policies;
    }

    @Transactional(readOnly = true)
    public Map<PolicyType, Long> getPolicyTypeDistribution() {
        return dummyPolicies.stream()
                .collect(Collectors.groupingBy(Policy::getPolicyType, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyPremiumRevenue() {
        return dummyPolicies.stream()
                .collect(Collectors.groupingBy(
                    policy -> policy.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.mapping(
                        Policy::getPremiumAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    @Transactional(readOnly = true)
    public Map<PolicyStatus, Long> getPolicyStatusDistribution() {
        return dummyPolicies.stream()
                .collect(Collectors.groupingBy(Policy::getStatus, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public List<CustomerPremiumDTO> getTopCustomersByPremium(int limit) {
        return dummyPolicies.stream()
                .collect(Collectors.groupingBy(Policy::getCustomer))
                .entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Policy> policies = entry.getValue();
                    BigDecimal totalPremium = policies.stream()
                            .map(Policy::getPremiumAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CustomerPremiumDTO(
                            customer.getUser().getFirstName() + " " + customer.getUser().getLastName(),
                            customer.getUser().getEmail(),
                            totalPremium.doubleValue(),
                            policies.size()
                    );
                })
                .sorted(Comparator.comparing(CustomerPremiumDTO::getTotalPremium).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPolicyExpirationSummary() {
        LocalDate now = LocalDate.now();
        Map<String, Long> summary = new HashMap<>();
        
        summary.put("Expired", dummyPolicies.stream()
                .filter(p -> p.getEndDate().isBefore(now))
                .count());
                
        summary.put("Expiring in 30 Days", dummyPolicies.stream()
                .filter(p -> !p.getEndDate().isBefore(now) && 
                           p.getEndDate().isBefore(now.plusDays(30)))
                .count());
                
        summary.put("Expiring in 90 Days", dummyPolicies.stream()
                .filter(p -> !p.getEndDate().isBefore(now) && 
                           p.getEndDate().isBefore(now.plusDays(90)))
                .count());
                
        return summary;
    }
} 