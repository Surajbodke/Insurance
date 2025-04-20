package com.insurancems.controller;

import com.insurancems.entity.*;
import com.insurancems.entity.enums.ClaimStatus;
import com.insurancems.entity.enums.PolicyStatus;
import com.insurancems.entity.enums.PolicyType;
import com.insurancems.entity.enums.ClaimType;
import com.insurancems.repository.CustomerRepository;
import com.insurancems.repository.UserRepository;
import com.insurancems.repository.PolicyRepository;
import com.insurancems.repository.ClaimRepository;
import com.insurancems.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public String viewPolicies(Model model) {
        try {
            // Get the current customer
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Get all policies for the customer
            List<Policy> customerPolicies = policyRepository.findByCustomer(customer);
            
            // Add dummy data if no policies exist
            if (customerPolicies.isEmpty()) {
                customerPolicies = createDummyPoliciesForCustomer(customer);
            }
            
            // Calculate statistics safely
            long activePolicies = customerPolicies.stream()
                    .filter(p -> p != null && p.getStatus() == PolicyStatus.ACTIVE)
                    .count();
            
            BigDecimal totalPremium = customerPolicies.stream()
                    .filter(p -> p != null && p.getStatus() == PolicyStatus.ACTIVE)
                    .map(Policy::getPremiumAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("policies", customerPolicies);
            model.addAttribute("activePoliciesCount", activePolicies);
            model.addAttribute("totalPremium", totalPremium);
            
            return "customer/policies";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while loading your policies. Please try again later.");
            model.addAttribute("policies", new ArrayList<>()); // Return empty list instead of null
            return "customer/policies";
        }
    }

    @GetMapping("/policies/{id}")
    public String viewPolicyDetails(@PathVariable Long id, Model model) {
        // Get the current customer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Find the policy
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Verify that the policy belongs to the customer
        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized access to policy");
        }

        model.addAttribute("policy", policy);
        return "customer/policy-details";
    }

    @GetMapping("/claims")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public String viewClaims(Model model) {
        try {
            // Get the current customer
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Get all claims for the customer
            List<Claim> customerClaims = claimRepository.findByCustomer(customer);
            
            // Add dummy claims if none exist
            if (customerClaims.isEmpty()) {
                customerClaims = createDummyClaimsForCustomer(customer);
            }

            // Get active policies for the customer
            List<Policy> activePolicies = policyRepository.findByCustomerAndStatus(customer, PolicyStatus.ACTIVE);
            if (activePolicies.isEmpty()) {
                activePolicies = createDummyPoliciesForCustomer(customer);
            }

            // Pre-calculate filtered lists for the template
            List<Claim> pendingClaims = customerClaims.stream()
                    .filter(c -> c.getStatus() == ClaimStatus.PENDING)
                    .toList();
            
            List<Claim> approvedClaims = customerClaims.stream()
                    .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                    .toList();
            
            BigDecimal totalApprovedAmount = approvedClaims.stream()
                    .map(Claim::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Add all data to model
            model.addAttribute("claims", customerClaims);
            model.addAttribute("policies", activePolicies);
            model.addAttribute("totalClaims", customerClaims.size());
            model.addAttribute("pendingClaims", pendingClaims.size());
            model.addAttribute("approvedClaims", approvedClaims.size());
            model.addAttribute("totalApprovedAmount", totalApprovedAmount);
            model.addAttribute("claimTypes", ClaimType.values());
            model.addAttribute("claimStatuses", ClaimStatus.values());
            
            return "customer/claims";
        } catch (Exception e) {
            e.printStackTrace();
            // Add empty statistics to prevent template errors
            model.addAttribute("claims", new ArrayList<>());
            model.addAttribute("policies", new ArrayList<>());
            model.addAttribute("totalClaims", 0);
            model.addAttribute("pendingClaims", 0);
            model.addAttribute("approvedClaims", 0);
            model.addAttribute("totalApprovedAmount", BigDecimal.ZERO);
            model.addAttribute("claimTypes", ClaimType.values());
            model.addAttribute("claimStatuses", ClaimStatus.values());
            model.addAttribute("errorMessage", "An error occurred while loading your claims. Please try again later.");
            return "customer/claims";
        }
    }

    @GetMapping("/claims/{id}")
    public String viewClaimDetails(@PathVariable Long id, Model model) {
        // Get the current customer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Find the claim
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Verify that the claim belongs to the customer
        if (!claim.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized access to claim");
        }

        model.addAttribute("claim", claim);
        return "customer/claim-details";
    }

    @PostMapping("/claims")
    public String fileClaim(@RequestParam Long policyId,
                          @RequestParam ClaimType type,
                          @RequestParam BigDecimal amount,
                          @RequestParam String description,
                          RedirectAttributes redirectAttributes) {
        // Get the current customer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get the policy
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Verify that the policy belongs to the customer
        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized access to policy");
        }

        // Create and save the claim
        Claim claim = new Claim();
        claim.setClaimNumber("CLM-" + System.currentTimeMillis()); // Generate unique claim number
        claim.setPolicy(policy);
        claim.setCustomer(customer);
        claim.setAgent(policy.getAgent());
        claim.setType(type);
        claim.setAmount(amount);
        claim.setDescription(description);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setClaimDate(LocalDate.now());
        claim.setUpdatedAt(LocalDate.now());

        claimRepository.save(claim);

        redirectAttributes.addFlashAttribute("successMessage", "Claim filed successfully!");
        return "redirect:/customer/claims";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Check if user has CUSTOMER role
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/access-denied";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        model.addAttribute("user", user);
        model.addAttribute("customer", customer);
        return "customer/profile";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam LocalDate dateOfBirth,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update user information
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        // Update customer information
        customer.setAddress(address);
        customer.setDateOfBirth(dateOfBirth);
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/customer/profile";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/register";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute Customer customer, 
                                 @RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String email,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String phoneNumber) {
        // Create and save user first
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setRole(com.insurancems.entity.enums.Role.CUSTOMER);
        user = userRepository.save(user);

        // Set the user for the customer
        customer.setUser(user);
        customerRepository.save(customer);
        return "redirect:/login";
    }

    private Policy createDummyPolicy(Long id, String policyNumber, PolicyType type, 
                                   BigDecimal premium, BigDecimal coverage, 
                                   LocalDate startDate, LocalDate endDate, 
                                   PolicyStatus status) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPolicyNumber(policyNumber);
        policy.setPolicyType(type);
        policy.setPremiumAmount(premium);
        policy.setCoverageAmount(coverage);
        policy.setStartDate(startDate);
        policy.setEndDate(endDate);
        policy.setStatus(status);
        policy.setTerms("Standard terms and conditions apply for this policy.");
        return policy;
    }

    private Claim createDummyClaim(Long id, String claimNumber, String policyNumber, 
                                 LocalDate claimDate, BigDecimal amount, 
                                 ClaimType type, ClaimStatus status) {
        Claim claim = new Claim();
        claim.setId(id);
        claim.setClaimNumber(claimNumber);
        
        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        claim.setPolicy(policy);
        
        claim.setClaimDate(claimDate);
        claim.setAmount(amount);
        claim.setType(type);
        claim.setStatus(status);
        claim.setDescription("Sample claim description for " + claimNumber);
        claim.setUpdatedAt(LocalDate.now());
        return claim;
    }

    private List<Policy> createDummyPoliciesForCustomer(Customer customer) {
        List<Policy> dummyPolicies = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Create a default agent if needed
        User agentUser = userRepository.findByUsername("default.agent")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("default.agent");
                    newUser.setPassword(passwordEncoder.encode("agent123"));
                    newUser.setEmail("default.agent@insurance.com");
                    newUser.setFirstName("Default");
                    newUser.setLastName("Agent");
                    newUser.setPhoneNumber("555-0100");
                    newUser.setRole(com.insurancems.entity.enums.Role.AGENT);
                    return userRepository.save(newUser);
                });

        Agent agent = agentRepository.findByUser(agentUser)
                .orElseGet(() -> {
                    Agent newAgent = new Agent();
                    newAgent.setUser(agentUser);
                    newAgent.setLicenseNumber("AG-DEFAULT-001");
                    newAgent.setCommissionRate(new BigDecimal("0.10"));
                    return agentRepository.save(newAgent);
                });

        // Create Health Insurance Policy
        Policy healthPolicy = new Policy();
        healthPolicy.setPolicyNumber("POL-" + System.currentTimeMillis() + "-H");
        healthPolicy.setPolicyType(PolicyType.HEALTH);
        healthPolicy.setPremiumAmount(new BigDecimal("250.00"));
        healthPolicy.setCoverageAmount(new BigDecimal("100000.00"));
        healthPolicy.setStartDate(now.minusMonths(1));
        healthPolicy.setEndDate(now.plusYears(1).minusMonths(1));
        healthPolicy.setStatus(PolicyStatus.ACTIVE);
        healthPolicy.setCustomer(customer);
        healthPolicy.setAgent(agent);
        healthPolicy.setTerms("Comprehensive health coverage including medical, dental, and vision benefits.");
        healthPolicy.setDescription("Standard health insurance policy with family coverage");
        dummyPolicies.add(policyRepository.save(healthPolicy));

        // Create Auto Insurance Policy
        Policy autoPolicy = new Policy();
        autoPolicy.setPolicyNumber("POL-" + System.currentTimeMillis() + "-A");
        autoPolicy.setPolicyType(PolicyType.AUTO);
        autoPolicy.setPremiumAmount(new BigDecimal("175.00"));
        autoPolicy.setCoverageAmount(new BigDecimal("50000.00"));
        autoPolicy.setStartDate(now.minusMonths(2));
        autoPolicy.setEndDate(now.plusYears(1).minusMonths(2));
        autoPolicy.setStatus(PolicyStatus.ACTIVE);
        autoPolicy.setCustomer(customer);
        autoPolicy.setAgent(agent);
        autoPolicy.setTerms("Full coverage auto insurance with collision and liability protection.");
        autoPolicy.setDescription("Comprehensive auto insurance with roadside assistance");
        dummyPolicies.add(policyRepository.save(autoPolicy));

        // Create Home Insurance Policy
        Policy homePolicy = new Policy();
        homePolicy.setPolicyNumber("POL-" + System.currentTimeMillis() + "-HM");
        homePolicy.setPolicyType(PolicyType.HOME);
        homePolicy.setPremiumAmount(new BigDecimal("300.00"));
        homePolicy.setCoverageAmount(new BigDecimal("250000.00"));
        homePolicy.setStartDate(now);
        homePolicy.setEndDate(now.plusYears(1));
        homePolicy.setStatus(PolicyStatus.PENDING);
        homePolicy.setCustomer(customer);
        homePolicy.setAgent(agent);
        homePolicy.setTerms("Home insurance covering property damage, theft, and liability.");
        homePolicy.setDescription("Premium home insurance with natural disaster coverage");
        dummyPolicies.add(policyRepository.save(homePolicy));

        return dummyPolicies;
    }

    private List<Claim> createDummyClaimsForCustomer(Customer customer) {
        List<Claim> dummyClaims = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Get or create a default agent
        User agentUser = userRepository.findByUsername("default.agent")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("default.agent");
                    newUser.setPassword(passwordEncoder.encode("agent123"));
                    newUser.setEmail("default.agent@insurance.com");
                    newUser.setFirstName("Default");
                    newUser.setLastName("Agent");
                    newUser.setPhoneNumber("555-0100");
                    newUser.setRole(com.insurancems.entity.enums.Role.AGENT);
                    return userRepository.save(newUser);
                });

        Agent agent = agentRepository.findByUser(agentUser)
                .orElseGet(() -> {
                    Agent newAgent = new Agent();
                    newAgent.setUser(agentUser);
                    newAgent.setLicenseNumber("AG-DEFAULT-001");
                    newAgent.setCommissionRate(new BigDecimal("0.10"));
                    return agentRepository.save(newAgent);
                });

        // Create dummy policies if needed
        List<Policy> customerPolicies = policyRepository.findByCustomer(customer);
        if (customerPolicies.isEmpty()) {
            customerPolicies = createDummyPoliciesForCustomer(customer);
        }

        // Create Medical Claim
        Claim medicalClaim = new Claim();
        medicalClaim.setClaimNumber("CLM-" + System.currentTimeMillis() + "-M");
        medicalClaim.setPolicy(customerPolicies.stream()
                .filter(p -> p.getPolicyType() == PolicyType.HEALTH)
                .findFirst()
                .orElse(customerPolicies.get(0)));
        medicalClaim.setCustomer(customer);
        medicalClaim.setAgent(agent);
        medicalClaim.setType(ClaimType.MEDICAL);
        medicalClaim.setAmount(new BigDecimal("750.00"));
        medicalClaim.setDescription("Annual medical checkup and preventive care");
        medicalClaim.setStatus(ClaimStatus.APPROVED);
        medicalClaim.setClaimDate(now.minusMonths(2));
        medicalClaim.setUpdatedAt(now.minusMonths(1));
        dummyClaims.add(claimRepository.save(medicalClaim));

        // Create Auto Claim
        Claim autoClaim = new Claim();
        autoClaim.setClaimNumber("CLM-" + System.currentTimeMillis() + "-A");
        autoClaim.setPolicy(customerPolicies.stream()
                .filter(p -> p.getPolicyType() == PolicyType.AUTO)
                .findFirst()
                .orElse(customerPolicies.get(0)));
        autoClaim.setCustomer(customer);
        autoClaim.setAgent(agent);
        autoClaim.setType(ClaimType.ACCIDENT);
        autoClaim.setAmount(new BigDecimal("2500.00"));
        autoClaim.setDescription("Minor fender bender, repair needed for front bumper");
        autoClaim.setStatus(ClaimStatus.PENDING);
        autoClaim.setClaimDate(now.minusWeeks(1));
        autoClaim.setUpdatedAt(now.minusWeeks(1));
        dummyClaims.add(claimRepository.save(autoClaim));

        // Create Home Claim
        Claim homeClaim = new Claim();
        homeClaim.setClaimNumber("CLM-" + System.currentTimeMillis() + "-H");
        homeClaim.setPolicy(customerPolicies.stream()
                .filter(p -> p.getPolicyType() == PolicyType.HOME)
                .findFirst()
                .orElse(customerPolicies.get(0)));
        homeClaim.setCustomer(customer);
        homeClaim.setAgent(agent);
        homeClaim.setType(ClaimType.PROPERTY_DAMAGE);
        homeClaim.setAmount(new BigDecimal("5000.00"));
        homeClaim.setDescription("Water damage from burst pipe in basement");
        homeClaim.setStatus(ClaimStatus.PROCESSING);
        homeClaim.setClaimDate(now.minusDays(3));
        homeClaim.setUpdatedAt(now);
        dummyClaims.add(claimRepository.save(homeClaim));

        return dummyClaims;
    }
} 