package com.insurancems.controller;

import com.insurancems.entity.*;
import com.insurancems.entity.enums.Role;
import com.insurancems.entity.enums.PolicyStatus;
import com.insurancems.entity.enums.PolicyType;
import com.insurancems.repository.UserRepository;
import com.insurancems.repository.PolicyRepository;
import com.insurancems.repository.CustomerRepository;
import com.insurancems.repository.AgentRepository;
import com.insurancems.service.PolicyService;
import com.insurancems.dto.CustomerPremiumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private AgentRepository agentRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Add dummy statistics
        model.addAttribute("totalUsers", 45);
        model.addAttribute("activeUsers", 42);
        model.addAttribute("newUsersThisMonth", 5);
        model.addAttribute("totalAgents", 15);
        model.addAttribute("totalCustomers", 28);
        model.addAttribute("totalAdmins", 2);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        // Create dummy user management data
        List<UserManagementDTO> managedUsers = Arrays.asList(
            // Active Users
            createManagedUser(1L, "admin.smith", "John Smith", Role.ADMIN, true,
                LocalDateTime.now().minusMonths(12), "System Administrator",
                Arrays.asList("User Management", "System Configuration", "Security")),
            createManagedUser(2L, "agent.wilson", "Mike Wilson", Role.AGENT, true,
                LocalDateTime.now().minusMonths(6), "Senior Agent",
                Arrays.asList("Policy Management", "Claims Processing")),
            createManagedUser(3L, "customer.johnson", "Robert Johnson", Role.CUSTOMER, true,
                LocalDateTime.now().minusMonths(3), "Premium Customer",
                Arrays.asList("View Policies", "Submit Claims")),
            
            // Recently Added Users
            createManagedUser(4L, "agent.davis", "Emma Davis", Role.AGENT, true,
                LocalDateTime.now().minusDays(5), "Junior Agent",
                Arrays.asList("Policy Viewing", "Customer Support")),
            createManagedUser(5L, "customer.lee", "Jennifer Lee", Role.CUSTOMER, true,
                LocalDateTime.now().minusDays(3), "New Customer",
                Arrays.asList("View Policies")),
                
            // Inactive Users
            createManagedUser(6L, "agent.brown", "James Brown", Role.AGENT, false,
                LocalDateTime.now().minusMonths(4), "Inactive Agent",
                Arrays.asList("None")),
            createManagedUser(7L, "customer.taylor", "William Taylor", Role.CUSTOMER, false,
                LocalDateTime.now().minusWeeks(2), "Inactive Customer",
                Arrays.asList("None"))
        );

        // Add user statistics
        model.addAttribute("managedUsers", managedUsers);
        model.addAttribute("totalManagedUsers", managedUsers.size());
        model.addAttribute("activeUsers", managedUsers.stream().filter(UserManagementDTO::isActive).count());
        model.addAttribute("inactiveUsers", managedUsers.stream().filter(u -> !u.isActive()).count());
        model.addAttribute("newUsers", managedUsers.stream()
            .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count());

        // Role counts
        model.addAttribute("adminCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.ADMIN).count());
        model.addAttribute("agentCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.AGENT).count());
        model.addAttribute("customerCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.CUSTOMER).count());

        // Add roles for the dropdown
        model.addAttribute("roles", Role.values());
        
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String phoneNumber,
                           @RequestParam Role role,
                           RedirectAttributes redirectAttributes) {
        // Create new user logic would go here
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Toggle user status logic would go here
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Delete user logic would go here
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/manage-users")
    public String manageUsers(Model model) {
        // Create dummy user management data
        List<UserManagementDTO> managedUsers = Arrays.asList(
            // Active Users
            createManagedUser(1L, "admin.smith", "John Smith", Role.ADMIN, true,
                LocalDateTime.now().minusMonths(12), "System Administrator",
                Arrays.asList("User Management", "System Configuration", "Security")),
            createManagedUser(2L, "agent.wilson", "Mike Wilson", Role.AGENT, true,
                LocalDateTime.now().minusMonths(6), "Senior Agent",
                Arrays.asList("Policy Management", "Claims Processing")),
            createManagedUser(3L, "customer.johnson", "Robert Johnson", Role.CUSTOMER, true,
                LocalDateTime.now().minusMonths(3), "Premium Customer",
                Arrays.asList("View Policies", "Submit Claims")),
            
            // Recently Added Users
            createManagedUser(4L, "agent.davis", "Emma Davis", Role.AGENT, true,
                LocalDateTime.now().minusDays(5), "Junior Agent",
                Arrays.asList("Policy Viewing", "Customer Support")),
            createManagedUser(5L, "customer.lee", "Jennifer Lee", Role.CUSTOMER, true,
                LocalDateTime.now().minusDays(3), "New Customer",
                Arrays.asList("View Policies")),
                
            // Inactive Users
            createManagedUser(6L, "agent.brown", "James Brown", Role.AGENT, false,
                LocalDateTime.now().minusMonths(4), "Inactive Agent",
                Arrays.asList("None")),
            createManagedUser(7L, "customer.taylor", "William Taylor", Role.CUSTOMER, false,
                LocalDateTime.now().minusWeeks(2), "Inactive Customer",
                Arrays.asList("None"))
        );

        // Add user statistics
        model.addAttribute("managedUsers", managedUsers);
        model.addAttribute("totalManagedUsers", managedUsers.size());
        model.addAttribute("activeUsers", managedUsers.stream().filter(UserManagementDTO::isActive).count());
        model.addAttribute("inactiveUsers", managedUsers.stream().filter(u -> !u.isActive()).count());
        model.addAttribute("newUsers", managedUsers.stream()
            .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count());

        // Role counts
        model.addAttribute("adminCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.ADMIN).count());
        model.addAttribute("agentCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.AGENT).count());
        model.addAttribute("customerCount", managedUsers.stream()
            .filter(u -> u.getRole() == Role.CUSTOMER).count());

        return "admin/manage-users";
    }

    // Helper DTO class for user management
    private static class UserManagementDTO {
        private Long id;
        private String username;
        private String fullName;
        private Role role;
        private boolean active;
        private LocalDateTime createdAt;
        private String userType;
        private List<String> permissions;
        private String status;

        public UserManagementDTO(Long id, String username, String fullName, Role role,
                               boolean active, LocalDateTime createdAt, String userType,
                               List<String> permissions) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.active = active;
            this.createdAt = createdAt;
            this.userType = userType;
            this.permissions = permissions;
            this.status = active ? "Active" : "Inactive";
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public Role getRole() { return role; }
        public boolean isActive() { return active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getUserType() { return userType; }
        public List<String> getPermissions() { return permissions; }
        public String getStatus() { return status; }
        
        public String getCreatedDate() {
            if (createdAt.isAfter(LocalDateTime.now().minusWeeks(1))) {
                return "New User";
            }
            return createdAt.toLocalDate().toString();
        }
    }

    // Helper method to create managed users
    private UserManagementDTO createManagedUser(Long id, String username, String fullName,
            Role role, boolean active, LocalDateTime createdAt, String userType,
            List<String> permissions) {
        return new UserManagementDTO(id, username, fullName, role, active, createdAt,
                                   userType, permissions);
    }

    private List<UserManagementDTO> createManagedUsersList(List<User> users) {
        return users.stream().map(user -> {
            String userType = switch (user.getRole()) {
                case ADMIN -> "System Administrator";
                case AGENT -> "Insurance Agent";
                case CUSTOMER -> "Policy Holder";
            };
            
            List<String> permissions = switch (user.getRole()) {
                case ADMIN -> Arrays.asList("User Management", "System Configuration", "Policy Management", "Reports Access");
                case AGENT -> Arrays.asList("Policy Creation", "Customer Management", "Claims Processing");
                case CUSTOMER -> Arrays.asList("View Policies", "Submit Claims", "Update Profile");
            };
            
            return new UserManagementDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedDate(),
                userType,
                permissions
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/policies")
    public String policyManagement(Model model) {
        List<Policy> policies = policyRepository.findAll();
        
        // Add dummy data if no policies exist
        if (policies.isEmpty()) {
            policies = createDummyPolicies();
        }
        
        // Calculate statistics
        long totalPolicies = policies.size();
        long activePolicies = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .count();
        
        BigDecimal totalPremium = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .map(Policy::getPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCoverage = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .map(Policy::getCoverageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Add data to model
        model.addAttribute("policies", policies);
        model.addAttribute("totalPolicies", totalPolicies);
        model.addAttribute("activePolicies", activePolicies);
        model.addAttribute("totalPremium", totalPremium);
        model.addAttribute("totalCoverage", totalCoverage);
        model.addAttribute("policyTypes", PolicyType.values());
        model.addAttribute("policyStatuses", PolicyStatus.values());
        model.addAttribute("customers", customerRepository.findAll());
        
        return "admin/policies";
    }

    private Agent createDefaultAgent() {
        // Check if default agent exists
        Optional<User> existingUser = userRepository.findByUsername("default.agent");
        if (existingUser.isPresent()) {
            return agentRepository.findByUser(existingUser.get())
                .orElseThrow(() -> new RuntimeException("Default agent not found"));
        }

        // Create default agent user
        User agentUser = new User();
        agentUser.setUsername("default.agent");
        agentUser.setPassword(passwordEncoder.encode("agent123"));
        agentUser.setEmail("default.agent@insurance.com");
        agentUser.setFirstName("Default");
        agentUser.setLastName("Agent");
        agentUser.setPhoneNumber("555-0100");
        agentUser.setRole(Role.AGENT);
        userRepository.save(agentUser);

        // Create default agent
        Agent agent = new Agent();
        agent.setUser(agentUser);
        agent.setLicenseNumber("AG-DEFAULT-001");
        agent.setCommissionRate(new BigDecimal("0.10"));
        return agentRepository.save(agent);
    }

    private List<Policy> createDummyPolicies() {
        List<Policy> dummyPolicies = new ArrayList<>();
        Random random = new Random();
        LocalDate now = LocalDate.now();

        // Get or create default agent
        Agent defaultAgent = createDefaultAgent();

        // Create dummy customers if none exist
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            customers = createDummyCustomers();
        }

        // Create policies for each customer
        for (Customer customer : customers) {
            // Create 2-4 policies per customer
            int numPolicies = random.nextInt(3) + 2;
            for (int i = 0; i < numPolicies; i++) {
                Policy policy = new Policy();
                policy.setPolicyNumber(generatePolicyNumber());
                policy.setCustomer(customer);
                policy.setAgent(defaultAgent); // Set the default agent
                policy.setPolicyType(PolicyType.values()[random.nextInt(PolicyType.values().length)]);
                policy.setStatus(PolicyStatus.values()[random.nextInt(PolicyStatus.values().length)]);
                
                // Set realistic premium amounts based on policy type
                BigDecimal basePremium = switch (policy.getPolicyType()) {
                    case HEALTH -> new BigDecimal("500");
                    case AUTO -> new BigDecimal("300");
                    case HOME -> new BigDecimal("800");
                    case LIFE -> new BigDecimal("1000");
                    case TRAVEL -> new BigDecimal("200");
                    case BUSINESS -> new BigDecimal("1500");
                };
                
                // Add some randomness to premiums (±20%)
                double multiplier = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
                policy.setPremiumAmount(basePremium.multiply(BigDecimal.valueOf(multiplier))
                    .setScale(2, RoundingMode.HALF_UP));
                
                // Set coverage amount (typically 100x to 500x the premium)
                int coverageMultiplier = random.nextInt(401) + 100; // 100 to 500
                policy.setCoverageAmount(policy.getPremiumAmount()
                    .multiply(BigDecimal.valueOf(coverageMultiplier))
                    .setScale(2, RoundingMode.HALF_UP));
                
                // Set dates
                LocalDate startDate = now.minusMonths(random.nextInt(12));
                policy.setStartDate(startDate);
                policy.setEndDate(startDate.plusYears(1));
                
                // Set terms based on policy type
                policy.setTerms(generateTerms(policy.getPolicyType()));
                
                // Set description
                policy.setDescription(generateDescription(policy));
                
                dummyPolicies.add(policy);
                policyRepository.save(policy);
            }
        }
        
        return dummyPolicies;
    }

    private List<Customer> createDummyCustomers() {
        List<Customer> dummyCustomers = new ArrayList<>();
        String[][] customerData = {
            {"John", "Doe", "123 Main St", "john.doe@example.com", "555-0101"},
            {"Jane", "Smith", "456 Oak Ave", "jane.smith@example.com", "555-0102"},
            {"Robert", "Johnson", "789 Pine Rd", "robert.j@example.com", "555-0103"},
            {"Maria", "Garcia", "321 Elm St", "maria.g@example.com", "555-0104"},
            {"James", "Wilson", "654 Maple Dr", "james.w@example.com", "555-0105"}
        };

        for (int i = 0; i < customerData.length; i++) {
            String[] data = customerData[i];
            
            // Create user
            User user = new User();
            user.setUsername(data[0].toLowerCase() + "." + data[1].toLowerCase());
            user.setPassword(passwordEncoder.encode("password123"));
            user.setEmail(data[3]);
            user.setFirstName(data[0]);
            user.setLastName(data[1]);
            user.setPhoneNumber(data[4]);
            user.setRole(Role.CUSTOMER);
            userRepository.save(user);

            // Create customer
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setAddress(data[2]);
            customer.setDateOfBirth(LocalDate.now().minusYears(25 + i)); // Ages 25-29
            customerRepository.save(customer);
            
            dummyCustomers.add(customer);
        }

        return dummyCustomers;
    }

    private String generateTerms(PolicyType type) {
        return switch (type) {
            case HEALTH -> "Standard health insurance terms including medical, dental, and vision coverage. " +
                         "Annual deductible applies. Pre-existing conditions may be covered after waiting period.";
            case AUTO -> "Comprehensive auto insurance covering collision, liability, and personal injury. " +
                       "Includes roadside assistance. Safe driver discounts available.";
            case HOME -> "Property insurance covering structural damage, personal belongings, and liability. " +
                       "Natural disasters covered with specific deductibles. Security system discount included.";
            case LIFE -> "Term life insurance with fixed premium. Includes accidental death benefit. " +
                       "Convertible to permanent policy. Suicide exclusion applies for first two years.";
            case TRAVEL -> "Travel insurance covering trip cancellation, medical emergencies, and lost baggage. " +
                         "International coverage included. Pre-existing conditions exclusion may apply.";
            case BUSINESS -> "Commercial insurance covering property, liability, and business interruption. " +
                           "Workers compensation optional. Cyber liability coverage included.";
        };
    }

    private String generateDescription(Policy policy) {
        return String.format("%s policy for %s %s. Premium: $%s monthly. Coverage: $%s. Valid from %s to %s.",
            policy.getPolicyType().toString(),
            policy.getCustomer().getUser().getFirstName(),
            policy.getCustomer().getUser().getLastName(),
            policy.getPremiumAmount(),
            policy.getCoverageAmount(),
            policy.getStartDate(),
            policy.getEndDate());
    }

    @PostMapping("/policies")
    public String createPolicy(@RequestParam Long customerId,
                             @RequestParam PolicyType policyType,
                             @RequestParam BigDecimal premiumAmount,
                             @RequestParam BigDecimal coverageAmount,
                             @RequestParam LocalDate startDate,
                             @RequestParam LocalDate endDate,
                             @RequestParam String terms,
                             RedirectAttributes redirectAttributes) {
        try {
            // Get the customer
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Get or create default agent for admin-created policies
            Agent defaultAgent = createDefaultAgent();
            
            Policy policy = new Policy();
            policy.setPolicyNumber("POL-" + System.currentTimeMillis());
            policy.setCustomer(customer);
            policy.setAgent(defaultAgent);
            policy.setPolicyType(policyType);
            policy.setPremiumAmount(premiumAmount);
            policy.setCoverageAmount(coverageAmount);
            policy.setStartDate(startDate);
            policy.setEndDate(endDate);
            policy.setTerms(terms);
            policy.setStatus(PolicyStatus.PENDING);
            policy.setDescription("Policy created by admin for customer " + customer.getUser().getFirstName() + " " + customer.getUser().getLastName());
            
            // Validate dates
            if (startDate.isAfter(endDate)) {
                throw new RuntimeException("Start date cannot be after end date");
            }
            if (endDate.isBefore(LocalDate.now())) {
                throw new RuntimeException("End date cannot be in the past");
            }
            
            // Validate amounts
            if (premiumAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Premium amount must be greater than zero");
            }
            if (coverageAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Coverage amount must be greater than zero");
            }
            
            policyRepository.save(policy);
            redirectAttributes.addFlashAttribute("successMessage", "Policy created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create policy: " + e.getMessage());
        }
        return "redirect:/admin/policies";
    }

    @PostMapping("/policies/{id}/status")
    public String updatePolicyStatus(@PathVariable Long id,
                                   @RequestParam PolicyStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            Policy policy = policyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Policy not found"));
            policy.setStatus(status);
            policyRepository.save(policy);
            redirectAttributes.addFlashAttribute("successMessage", "Policy status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update policy status: " + e.getMessage());
        }
        return "redirect:/admin/policies";
    }

    @PostMapping("/policies/{id}/delete")
    public String deletePolicy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            policyRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Policy deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete policy: " + e.getMessage());
        }
        return "redirect:/admin/policies";
    }

    private String generatePolicyNumber() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "POL-" + year + month + "-" + random;
    }

    @GetMapping("/policies/reports")
    public String getPolicyReports(Model model) {
        // Policy type distribution
        Map<PolicyType, Long> policyTypeDistribution = policyService.getPolicyTypeDistribution();
        model.addAttribute("policyTypeDistribution", policyTypeDistribution);

        // Monthly premium revenue
        Map<String, BigDecimal> monthlyPremiumRevenue = policyService.getMonthlyPremiumRevenue();
        model.addAttribute("monthlyPremiumRevenue", monthlyPremiumRevenue);

        // Policy status distribution
        Map<PolicyStatus, Long> policyStatusDistribution = policyService.getPolicyStatusDistribution();
        model.addAttribute("policyStatusDistribution", policyStatusDistribution);

        // Top customers by premium
        List<CustomerPremiumDTO> topCustomers = policyService.getTopCustomersByPremium(10);
        model.addAttribute("topCustomers", topCustomers);

        // Policy expiration summary
        Map<String, Long> expirationSummary = policyService.getPolicyExpirationSummary();
        model.addAttribute("expirationSummary", expirationSummary);

        return "admin/policy-reports";
    }
} 