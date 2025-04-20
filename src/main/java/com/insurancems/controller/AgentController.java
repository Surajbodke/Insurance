package com.insurancems.controller;

import com.insurancems.entity.*;
import com.insurancems.entity.enums.ClaimStatus;
import com.insurancems.entity.enums.PolicyType;
import com.insurancems.entity.enums.PolicyStatus;
import com.insurancems.entity.enums.ClaimType;
import com.insurancems.repository.AgentRepository;
import com.insurancems.repository.CustomerRepository;
import com.insurancems.repository.PolicyRepository;
import com.insurancems.repository.UserRepository;
import com.insurancems.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("agent", new Agent());
        return "agent/register";
    }

    @PostMapping("/register")
    public String registerAgent(@ModelAttribute Agent agent,
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
        user.setRole(com.insurancems.entity.enums.Role.AGENT);
        user = userRepository.save(user);

        // Set the user for the agent
        agent.setUser(user);
        agentRepository.save(agent);
        return "redirect:/login";
    }

    @PostMapping("/customers")
    public String createCustomer(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String phoneNumber,
                               @RequestParam String address,
                               @RequestParam String dateOfBirth) {
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

        // Create and save customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setAddress(address);
        customer.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
        customerRepository.save(customer);

        return "redirect:/agent/customers";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // Create dummy agent profile data
        Agent agent = createDummyAgent(1L);
        agent.getUser().setFirstName("John");
        agent.getUser().setLastName("Smith");
        agent.getUser().setEmail("john.smith@insurance.com");
        agent.getUser().setPhoneNumber("555-0123-4567");
        agent.setLicenseNumber("AG2023-001");
        agent.setCommissionRate(new BigDecimal("0.12"));
        
        // Add some performance metrics
        model.addAttribute("totalPolicies", 45);
        model.addAttribute("activePolicies", 38);
        model.addAttribute("totalCustomers", 32);
        model.addAttribute("monthlyCommission", new BigDecimal("2850.00"));
        model.addAttribute("yearlyCommission", new BigDecimal("34200.00"));
        model.addAttribute("agent", agent);
        
        return "agent/profile";
    }

    @GetMapping("/customers")
    public String customerManagement(Model model) {
        // Create a list of dummy customers with varied profiles
        List<Customer> customers = Arrays.asList(
            createDetailedCustomer(1L, "Sarah", "Johnson", "sarah.j@email.com", "555-0001", 
                "123 Oak Street, Springfield", LocalDate.of(1985, 3, 15), 3),
            createDetailedCustomer(2L, "Michael", "Chen", "m.chen@email.com", "555-0002", 
                "456 Maple Ave, Riverside", LocalDate.of(1992, 7, 22), 1),
            createDetailedCustomer(3L, "Emily", "Williams", "e.williams@email.com", "555-0003", 
                "789 Pine Road, Lakeside", LocalDate.of(1978, 11, 30), 2),
            createDetailedCustomer(4L, "James", "Brown", "j.brown@email.com", "555-0004", 
                "321 Cedar Lane, Mountain View", LocalDate.of(1989, 5, 8), 4),
            createDetailedCustomer(5L, "Maria", "Garcia", "m.garcia@email.com", "555-0005", 
                "654 Birch Court, Sunnyvale", LocalDate.of(1995, 9, 17), 1)
        );
        
        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("activeCustomers", customers.size() - 1);
        return "agent/customers";
    }

    @GetMapping("/claims")
    public String claimsProcessing(Model model) {
        try {
            // Get the current agent
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            Agent agent = agentRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Agent not found for user: " + username));

            // Get all claims for the current agent
            List<Claim> claims = claimRepository.findByAgent(agent);
            
            // Create dummy claims if none exist
            if (claims == null || claims.isEmpty()) {
                claims = createDummyClaims(agent);
            }

            // Calculate statistics
            BigDecimal totalClaimAmount = BigDecimal.ZERO;
            long pendingClaims = 0;
            long approvedClaims = 0;
            long processingClaims = 0;

            for (Claim claim : claims) {
                if (claim.getAmount() != null) {
                    if (claim.getStatus() == ClaimStatus.APPROVED) {
                        totalClaimAmount = totalClaimAmount.add(claim.getAmount());
                        approvedClaims++;
                    } else if (claim.getStatus() == ClaimStatus.PENDING) {
                        pendingClaims++;
                    } else if (claim.getStatus() == ClaimStatus.PROCESSING) {
                        processingClaims++;
                    }
                }
            }

            // Add data to model
            model.addAttribute("claims", claims);
            model.addAttribute("totalClaims", claims.size());
            model.addAttribute("pendingClaims", pendingClaims);
            model.addAttribute("approvedClaims", approvedClaims);
            model.addAttribute("processingClaims", processingClaims);
            model.addAttribute("totalClaimAmount", totalClaimAmount.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("agent", agent);

            return "agent/claims";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading claims page: " + e.getMessage(), e);
        }
    }

    private List<Claim> createDummyClaims(Agent agent) {
        List<Claim> dummyClaims = new ArrayList<>();
        Random random = new Random();
        LocalDate now = LocalDate.now();

        // Get existing policies for the agent
        List<Policy> policies = policyRepository.findByAgent(agent);
        if (policies.isEmpty()) {
            // Create dummy policies if none exist
            policies = createDummyPolicies();
            for (Policy policy : policies) {
                policy.setAgent(agent);
                policyRepository.save(policy);
            }
        }

        // Create claims for active policies
        List<Policy> activePolicies = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Policy policy : activePolicies) {
            // Create 2-4 claims per policy for more data
            int numClaims = random.nextInt(3) + 2;
            for (int i = 0; i < numClaims; i++) {
                try {
                    Claim claim = new Claim();
                    
                    // Generate unique claim number with policy type prefix
                    claim.setClaimNumber(String.format("CLM-%s-%s-%04d",
                        policy.getPolicyType().toString().substring(0, 3),
                        now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")),
                        random.nextInt(10000)));
                    
                    claim.setPolicy(policy);
                    claim.setCustomer(policy.getCustomer());
                    claim.setAgent(agent);
                    
                    // Set claim date within policy period, but not too recent
                    long daysBetween = ChronoUnit.DAYS.between(policy.getStartDate(), 
                        now.isBefore(policy.getEndDate()) ? now : policy.getEndDate());
                    LocalDate claimDate = policy.getStartDate().plusDays(
                        random.nextInt((int) (daysBetween * 0.8))); // Use only first 80% of policy period
                    claim.setClaimDate(claimDate);
                    
                    // Set claim type based on policy type with more variety
                    ClaimType claimType = getClaimTypeForPolicy(policy.getPolicyType());
                    claim.setType(claimType);
                    
                    // Generate detailed description based on claim type
                    claim.setDescription(generateDetailedClaimDescription(claimType));
                    
                    // Set amount based on policy type and coverage
                    double basePercentage = switch (policy.getPolicyType()) {
                        case HEALTH -> 0.05 + (random.nextDouble() * 0.15); // 5-20% of coverage
                        case AUTO -> 0.10 + (random.nextDouble() * 0.30);   // 10-40% of coverage
                        case HOME -> 0.15 + (random.nextDouble() * 0.35);   // 15-50% of coverage
                        case LIFE -> 0.50 + (random.nextDouble() * 0.50);   // 50-100% of coverage
                        case TRAVEL -> 0.10 + (random.nextDouble() * 0.20); // 10-30% of coverage
                        case BUSINESS -> 0.20 + (random.nextDouble() * 0.30); // 20-50% of coverage
                    };
                    
                    BigDecimal claimAmount = policy.getCoverageAmount()
                        .multiply(new BigDecimal(basePercentage))
                        .setScale(2, RoundingMode.HALF_UP);
                    claim.setAmount(claimAmount);
                    
                    // Set status with weighted probability and time-based logic
                    long daysAgo = ChronoUnit.DAYS.between(claimDate, now);
                    ClaimStatus status;
                    String notes;
                    
                    if (daysAgo > 30) {
                        // Older claims are more likely to be resolved
                        status = random.nextInt(100) < 80 ? ClaimStatus.APPROVED : ClaimStatus.REJECTED;
                        notes = status == ClaimStatus.APPROVED ? 
                            "Claim approved after thorough review. Payment processed." :
                            "Claim rejected due to policy exclusions or insufficient documentation.";
                    } else if (daysAgo > 14) {
                        // Claims 2-4 weeks old are likely being processed
                        status = random.nextInt(100) < 70 ? ClaimStatus.PROCESSING : ClaimStatus.PENDING;
                        notes = status == ClaimStatus.PROCESSING ?
                            "Claim under review. Verification of documents in progress." :
                            "Initial review completed. Awaiting additional documentation.";
                    } else {
                        // Recent claims are usually pending
                        status = ClaimStatus.PENDING;
                        notes = "New claim received. Initial review pending.";
                    }
                    
                    claim.setStatus(status);
                    claim.setNotes(notes);
                    claim.setUpdatedAt(status == ClaimStatus.PENDING ? claimDate : 
                                     now.minusDays(random.nextInt((int)daysAgo + 1)));
                    
                    Claim savedClaim = claimRepository.save(claim);
                    if (savedClaim != null) {
                        dummyClaims.add(savedClaim);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continue with next claim if one fails
                }
            }
        }
        
        return dummyClaims;
    }

    private String generateDetailedClaimDescription(ClaimType claimType) {
        Random random = new Random();
        return switch (claimType) {
            case MEDICAL -> {
                String[] conditions = {"Annual checkup", "Emergency room visit", "Surgery", "Specialist consultation"};
                String[] details = {"Including lab tests", "With follow-up care", "Including medication", "With rehabilitation"};
                yield conditions[random.nextInt(conditions.length)] + ". " + details[random.nextInt(details.length)];
            }
            case PRESCRIPTION -> {
                String[] medications = {"Chronic condition medication", "Antibiotics prescription", "Preventive medication"};
                String[] durations = {"3-month supply", "1-month supply", "6-month supply"};
                yield medications[random.nextInt(medications.length)] + " - " + durations[random.nextInt(durations.length)];
            }
            case ACCIDENT -> {
                String[] causes = {"Rear-end collision", "Side impact", "Parking lot incident", "Multi-vehicle accident"};
                String[] damages = {"Front bumper damage", "Side panel repair", "Windshield replacement", "Total loss"};
                yield causes[random.nextInt(causes.length)] + " resulting in " + damages[random.nextInt(damages.length)];
            }
            case THEFT -> {
                String[] items = {"Vehicle theft", "Parts stolen", "Personal items from vehicle"};
                String[] circumstances = {"Parked at residence", "Public parking", "Shopping center"};
                yield items[random.nextInt(items.length)] + " while " + circumstances[random.nextInt(circumstances.length)];
            }
            case PROPERTY_DAMAGE -> {
                String[] causes = {"Water damage", "Fire damage", "Storm damage", "Structural damage"};
                String[] locations = {"Kitchen", "Basement", "Roof", "Living room"};
                yield causes[random.nextInt(causes.length)] + " in " + locations[random.nextInt(locations.length)];
            }
            case NATURAL_DISASTER -> {
                String[] events = {"Hurricane", "Flood", "Earthquake", "Tornado"};
                String[] impacts = {"Structural damage", "Water damage", "Wind damage", "Complete loss"};
                yield events[random.nextInt(events.length)] + " causing " + impacts[random.nextInt(impacts.length)];
            }
            case DEATH_BENEFIT -> "Life insurance benefit claim - Standard policy payout";
            case TRIP_CANCELLATION -> {
                String[] reasons = {"Medical emergency", "Weather conditions", "Travel restrictions", "Family emergency"};
                String[] costs = {"Flight tickets", "Hotel booking", "Tour package", "Cruise reservation"};
                yield "Cancellation due to " + reasons[random.nextInt(reasons.length)] + " - " + costs[random.nextInt(costs.length)];
            }
            case LOST_BAGGAGE -> {
                String[] locations = {"International flight", "Domestic flight", "Train travel", "Cruise ship"};
                String[] contents = {"Personal items", "Business materials", "Valuable equipment", "Travel documents"};
                yield "Lost during " + locations[random.nextInt(locations.length)] + " containing " + contents[random.nextInt(contents.length)];
            }
            case LIABILITY -> {
                String[] incidents = {"Slip and fall", "Property damage", "Professional error", "Product liability"};
                String[] contexts = {"Business premises", "Client location", "Public space", "During service delivery"};
                yield incidents[random.nextInt(incidents.length)] + " at " + contexts[random.nextInt(contexts.length)];
            }
            case BUSINESS_INTERRUPTION -> {
                String[] causes = {"Natural disaster", "Equipment failure", "Utility outage", "Supply chain disruption"};
                String[] impacts = {"Revenue loss", "Operational costs", "Employee wages", "Contract penalties"};
                yield causes[random.nextInt(causes.length)] + " leading to " + impacts[random.nextInt(impacts.length)];
            }
            default -> "General insurance claim - Standard processing";
        };
    }

    private ClaimType getClaimTypeForPolicy(PolicyType policyType) {
        Random random = new Random();
        switch (policyType) {
            case HEALTH:
                return random.nextBoolean() ? ClaimType.MEDICAL : ClaimType.PRESCRIPTION;
            case AUTO:
                return random.nextBoolean() ? ClaimType.ACCIDENT : ClaimType.THEFT;
            case HOME:
                return random.nextBoolean() ? ClaimType.PROPERTY_DAMAGE : ClaimType.NATURAL_DISASTER;
            case LIFE:
                return ClaimType.DEATH_BENEFIT;
            case TRAVEL:
                return random.nextBoolean() ? ClaimType.TRIP_CANCELLATION : ClaimType.LOST_BAGGAGE;
            case BUSINESS:
                return random.nextBoolean() ? ClaimType.LIABILITY : ClaimType.BUSINESS_INTERRUPTION;
            default:
                return ClaimType.OTHER;
        }
    }

    @GetMapping("/policies")
    public String policyManagement(Model model) {
        try {
            // Get the current agent
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            Agent agent = agentRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Agent not found for user: " + username));

            // Get all policies for the current agent
            List<Policy> policies = policyRepository.findByAgent(agent);
            if (policies == null || policies.isEmpty()) {
                // Create dummy customers if none exist
                List<Customer> customers = customerRepository.findAll();
                if (customers == null || customers.isEmpty()) {
                    customers = createDummyCustomers();
                }

                // Create dummy policies using the current agent
                policies = createDummyPolicies();
                
                // Update all policies to be assigned to the current agent
                for (Policy policy : policies) {
                    policy.setAgent(agent);
                    policyRepository.save(policy);
                }
            }

            // Calculate statistics
            BigDecimal totalPremium = BigDecimal.ZERO;
            BigDecimal totalCoverage = BigDecimal.ZERO;
            long activePolicies = 0;
            long pendingPolicies = 0;

            for (Policy policy : policies) {
                if (policy.getStatus() == PolicyStatus.ACTIVE) {
                    activePolicies++;
                    if (policy.getPremiumAmount() != null) {
                        totalPremium = totalPremium.add(policy.getPremiumAmount());
                    }
                    if (policy.getCoverageAmount() != null) {
                        totalCoverage = totalCoverage.add(policy.getCoverageAmount());
                    }
                } else if (policy.getStatus() == PolicyStatus.PENDING) {
                    pendingPolicies++;
                }
            }

            // Get all customers for the policy creation form
            List<Customer> customers = customerRepository.findAll();
            if (customers == null || customers.isEmpty()) {
                customers = createDummyCustomers();
            }

            // Add all required data to the model
            model.addAttribute("policies", policies);
            model.addAttribute("customers", customers);
            model.addAttribute("policyTypes", PolicyType.values());
            model.addAttribute("policyStatuses", PolicyStatus.values());
            model.addAttribute("totalPolicies", policies.size());
            model.addAttribute("activePolicies", activePolicies);
            model.addAttribute("pendingPolicies", pendingPolicies);
            model.addAttribute("totalPremium", totalPremium.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("totalCoverage", totalCoverage.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("agent", agent);

            return "agent/policies";
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading policy management page: " + e.getMessage(), e);
        }
    }

    @PostMapping("/policies")
    public String createPolicy(@RequestParam Long customerId,
                             @RequestParam PolicyType policyType,
                             @RequestParam PolicyStatus status,
                             @RequestParam BigDecimal premiumAmount,
                             @RequestParam BigDecimal coverageAmount,
                             @RequestParam LocalDate startDate,
                             @RequestParam LocalDate endDate,
                             @RequestParam String terms,
                             RedirectAttributes redirectAttributes) {
        
        // Get the current agent
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Agent agent = agentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Get the customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Create and save the policy
        Policy policy = new Policy();
        policy.setPolicyNumber("POL-" + System.currentTimeMillis()); // Generate unique policy number
        policy.setPolicyType(policyType);
        policy.setStatus(status);
        policy.setPremiumAmount(premiumAmount);
        policy.setCoverageAmount(coverageAmount);
        policy.setStartDate(startDate);
        policy.setEndDate(endDate);
        policy.setTerms(terms);
        policy.setCustomer(customer);
        policy.setAgent(agent);
        policy.setDescription("Policy created by agent " + agent.getUser().getFirstName() + " " + agent.getUser().getLastName());

        policyRepository.save(policy);

        redirectAttributes.addFlashAttribute("successMessage", "Policy created successfully!");
        return "redirect:/agent/policies";
    }

    @GetMapping("/claims/{id}")
    public String viewClaimDetails(@PathVariable Long id, Model model) {
        // Get the current agent
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Agent agent = agentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Find the claim
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Verify that the claim belongs to the agent
        if (!claim.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Unauthorized access to claim");
        }

        model.addAttribute("claim", claim);
        return "agent/claim-details";
    }

    @GetMapping("/policies/{id}")
    public String viewPolicyDetails(@PathVariable Long id, Model model) {
        // Get the current agent
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Agent agent = agentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Find the policy
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Verify that the policy belongs to the agent
        if (!policy.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Unauthorized access to policy");
        }

        model.addAttribute("policy", policy);
        return "agent/policy-details";
    }

    private Policy createDummyPolicy(Long id, Customer customer) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPolicyNumber("POL-2023-00" + id);
        policy.setPolicyType(PolicyType.HEALTH);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now().minusMonths(6));
        policy.setEndDate(LocalDate.now().plusMonths(6));
        policy.setPremiumAmount(new BigDecimal("150.00"));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        policy.setCustomer(customer);
        policy.setAgent(createDummyAgent(1L));
        policy.setDescription("Sample health insurance policy");
        policy.setTerms("Standard terms and conditions apply");
        return policy;
    }

    private Customer createDummyCustomer(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("customer" + id);
        user.setFirstName("Customer");
        user.setLastName("" + id);
        user.setEmail("customer" + id + "@example.com");
        user.setPhoneNumber("123-456-789" + id);
        user.setRole(com.insurancems.entity.enums.Role.CUSTOMER);

        Customer customer = new Customer();
        customer.setId(id);
        customer.setUser(user);
        customer.setAddress("123 Main St #" + id);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return customer;
    }

    private Agent createDummyAgent(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("agent" + id);
        user.setFirstName("Agent");
        user.setLastName("" + id);
        user.setEmail("agent" + id + "@example.com");
        user.setPhoneNumber("987-654-321" + id);
        user.setRole(com.insurancems.entity.enums.Role.AGENT);

        Agent agent = new Agent();
        agent.setId(id);
        agent.setUser(user);
        agent.setLicenseNumber("AG" + id);
        agent.setCommissionRate(new BigDecimal("0.10"));
        return agent;
    }

    // Helper method for creating detailed customer profiles
    private Customer createDetailedCustomer(Long id, String firstName, String lastName, 
            String email, String phone, String address, LocalDate dob, int numPolicies) {
        User user = new User();
        user.setId(id);
        user.setUsername(firstName.toLowerCase() + "." + lastName.toLowerCase());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setRole(com.insurancems.entity.enums.Role.CUSTOMER);

        Customer customer = new Customer();
        customer.setId(id);
        customer.setUser(user);
        customer.setAddress(address);
        customer.setDateOfBirth(dob);
        return customer;
    }

    // Helper method for creating detailed policies
    private Policy createDetailedPolicy(Long id, String policyNumber, PolicyType type, 
            PolicyStatus status, BigDecimal premium, BigDecimal coverage,
            LocalDate startDate, LocalDate endDate, String description,
            Customer customer, Agent agent) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPolicyNumber(policyNumber);
        policy.setPolicyType(type);
        policy.setStatus(status);
        policy.setPremiumAmount(premium);
        policy.setCoverageAmount(coverage);
        policy.setStartDate(startDate);
        policy.setEndDate(endDate);
        policy.setDescription(description);
        policy.setCustomer(customer);
        policy.setAgent(agent);
        policy.setTerms("Standard terms and conditions apply for " + type.toString() + " insurance");
        return policy;
    }

    private List<Customer> createDummyCustomers() {
        List<Customer> dummyCustomers = new ArrayList<>();
        List<Customer> savedCustomers = new ArrayList<>();
        
        try {
            // Create 3 dummy customers (reduced from 5 to minimize potential issues)
            dummyCustomers.add(createDetailedCustomer(1L, "Sarah", "Johnson", "sarah.j@email.com", "555-0001", 
                "123 Oak Street, Springfield", LocalDate.of(1985, 3, 15), 3));
            dummyCustomers.add(createDetailedCustomer(2L, "Michael", "Chen", "m.chen@email.com", "555-0002", 
                "456 Maple Ave, Riverside", LocalDate.of(1992, 7, 22), 1));
            dummyCustomers.add(createDetailedCustomer(3L, "Emily", "Williams", "e.williams@email.com", "555-0003", 
                "789 Pine Road, Lakeside", LocalDate.of(1978, 11, 30), 2));
            
            // Save customers one by one to handle individual failures
            for (Customer customer : dummyCustomers) {
                try {
                    Customer savedCustomer = customerRepository.save(customer);
                    if (savedCustomer != null) {
                        savedCustomers.add(savedCustomer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continue with next customer if one fails to save
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return savedCustomers;
    }

    private List<Policy> createDummyPoliciesForAgent(Agent agent, List<Customer> customers) {
        List<Policy> dummyPolicies = new ArrayList<>();
        Random random = new Random();
        LocalDate now = LocalDate.now();

        // Create policies for each customer
        for (Customer customer : customers) {
            try {
                // Create 1-2 policies per customer (reduced from 2-4 to minimize potential issues)
                int numPolicies = random.nextInt(2) + 1;
                for (int i = 0; i < numPolicies; i++) {
                    Policy policy = new Policy();
                    policy.setPolicyNumber("POL-" + System.currentTimeMillis() + "-" + customer.getId() + "-" + i);
                    policy.setCustomer(customer);
                    policy.setAgent(agent);
                    
                    // Set policy type and status (with null checks)
                    PolicyType[] types = PolicyType.values();
                    PolicyStatus[] statuses = PolicyStatus.values();
                    policy.setPolicyType(types[random.nextInt(types.length)]);
                    policy.setStatus(statuses[random.nextInt(statuses.length)]);
                    
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
                    double multiplier = 0.8 + (random.nextDouble() * 0.4);
                    policy.setPremiumAmount(basePremium.multiply(new BigDecimal(multiplier)).setScale(2, RoundingMode.HALF_UP));
                    
                    // Set coverage amount (100-200 times the premium - reduced from 500 to minimize potential issues)
                    int coverageMultiplier = random.nextInt(101) + 100;
                    policy.setCoverageAmount(policy.getPremiumAmount().multiply(new BigDecimal(coverageMultiplier)));
                    
                    // Set dates
                    policy.setStartDate(now.minusDays(random.nextInt(30))); // Start within last 30 days
                    policy.setEndDate(policy.getStartDate().plusYears(1)); // 1 year duration
                    
                    // Set terms
                    policy.setTerms("Standard terms and conditions for " + policy.getPolicyType() + " insurance. " +
                                  "Coverage period: " + policy.getStartDate() + " to " + policy.getEndDate());
                    
                    try {
                        Policy savedPolicy = policyRepository.save(policy);
                        if (savedPolicy != null) {
                            dummyPolicies.add(savedPolicy);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Continue with next policy if one fails to save
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Continue with next customer if policy creation fails
            }
        }
        
        return dummyPolicies;
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

        // Policy type specific configurations
        Map<PolicyType, PolicyConfig> policyConfigs = new HashMap<>();
        policyConfigs.put(PolicyType.HEALTH, new PolicyConfig(
            500, 2000,    // Premium range
            50000, 500000, // Coverage range
            "Standard health insurance policy covering medical expenses, hospitalization, and prescription drugs.",
            "1. Coverage for in-patient and out-patient treatments\n" +
            "2. 24/7 emergency medical assistance\n" +
            "3. Prescription drug coverage\n" +
            "4. Annual health checkup included"
        ));
        
        policyConfigs.put(PolicyType.AUTO, new PolicyConfig(
            200, 1000,    // Premium range
            20000, 100000, // Coverage range
            "Comprehensive auto insurance policy with collision and liability coverage.",
            "1. Collision and comprehensive coverage\n" +
            "2. Third-party liability protection\n" +
            "3. 24/7 roadside assistance\n" +
            "4. Rental car coverage"
        ));
        
        policyConfigs.put(PolicyType.HOME, new PolicyConfig(
            800, 3000,    // Premium range
            200000, 1000000, // Coverage range
            "Complete home insurance coverage protecting your property and belongings.",
            "1. Property damage coverage\n" +
            "2. Personal belongings protection\n" +
            "3. Liability coverage\n" +
            "4. Natural disaster protection"
        ));
        
        policyConfigs.put(PolicyType.LIFE, new PolicyConfig(
            1000, 5000,   // Premium range
            500000, 2000000, // Coverage range
            "Term life insurance policy providing financial security for your loved ones.",
            "1. Death benefit protection\n" +
            "2. Terminal illness coverage\n" +
            "3. Optional critical illness rider\n" +
            "4. Flexible premium payment terms"
        ));
        
        policyConfigs.put(PolicyType.TRAVEL, new PolicyConfig(
            50, 500,      // Premium range
            10000, 100000, // Coverage range
            "Comprehensive travel insurance for domestic and international trips.",
            "1. Trip cancellation coverage\n" +
            "2. Medical emergency coverage\n" +
            "3. Lost baggage protection\n" +
            "4. 24/7 travel assistance"
        ));
        
        policyConfigs.put(PolicyType.BUSINESS, new PolicyConfig(
            1500, 10000,  // Premium range
            1000000, 5000000, // Coverage range
            "Complete business insurance package for commercial operations.",
            "1. Property damage coverage\n" +
            "2. Business interruption protection\n" +
            "3. Liability coverage\n" +
            "4. Employee injury protection"
        ));

        // Create policies for each customer
        for (Customer customer : customers) {
            // Create 2-4 policies per customer
            int numPolicies = random.nextInt(3) + 2;
            for (int i = 0; i < numPolicies; i++) {
                try {
                    // Select random policy type
                    PolicyType[] types = PolicyType.values();
                    PolicyType selectedType = types[random.nextInt(types.length)];
                    PolicyConfig config = policyConfigs.get(selectedType);
                    
                    Policy policy = new Policy();
                    policy.setPolicyNumber(generatePolicyNumber(selectedType));
                    policy.setCustomer(customer);
                    policy.setAgent(defaultAgent);
                    policy.setPolicyType(selectedType);
                    
                    // Set status with weighted probability
                    int statusRoll = random.nextInt(100);
                    if (statusRoll < 70) {
                        policy.setStatus(PolicyStatus.ACTIVE);
                    } else if (statusRoll < 85) {
                        policy.setStatus(PolicyStatus.PENDING);
                    } else if (statusRoll < 95) {
                        policy.setStatus(PolicyStatus.EXPIRED);
                    } else {
                        policy.setStatus(PolicyStatus.CANCELLED);
                    }
                    
                    // Set amounts based on policy type
                    double premiumMultiplier = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
                    int premiumBase = random.nextInt(config.maxPremium - config.minPremium) + config.minPremium;
                    policy.setPremiumAmount(new BigDecimal(premiumBase * premiumMultiplier).setScale(2, RoundingMode.HALF_UP));
                    
                    int coverageBase = random.nextInt(config.maxCoverage - config.minCoverage) + config.minCoverage;
                    policy.setCoverageAmount(new BigDecimal(coverageBase).setScale(2, RoundingMode.HALF_UP));
                    
                    // Set dates based on status
                    LocalDate startDate;
                    LocalDate endDate;
                    
                    switch (policy.getStatus()) {
                        case ACTIVE:
                            startDate = now.minusDays(random.nextInt(180)); // Within last 6 months
                            endDate = startDate.plusYears(1); // 1 year duration
                            break;
                        case PENDING:
                            startDate = now.plusDays(random.nextInt(30)); // Within next month
                            endDate = startDate.plusYears(1);
                            break;
                        case EXPIRED:
                            startDate = now.minusYears(2).plusDays(random.nextInt(365));
                            endDate = startDate.plusYears(1);
                            break;
                        case CANCELLED:
                            startDate = now.minusMonths(random.nextInt(6));
                            endDate = startDate.plusYears(1);
                            break;
                        default:
                            startDate = now;
                            endDate = now.plusYears(1);
                    }
                    
                    policy.setStartDate(startDate);
                    policy.setEndDate(endDate);
                    
                    policy.setDescription(config.description);
                    policy.setTerms(config.terms);
                    
                    Policy savedPolicy = policyRepository.save(policy);
                    if (savedPolicy != null) {
                        dummyPolicies.add(savedPolicy);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continue with next policy if one fails to save
                }
            }
        }
        
        return dummyPolicies;
    }

    private String generatePolicyNumber(PolicyType type) {
        return String.format("%s-%s-%04d",
            type.toString().substring(0, 3),
            LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")),
            new Random().nextInt(10000)
        );
    }

    // Helper class for policy type specific configurations
    private static class PolicyConfig {
        final int minPremium;
        final int maxPremium;
        final int minCoverage;
        final int maxCoverage;
        final String description;
        final String terms;

        PolicyConfig(int minPremium, int maxPremium, int minCoverage, int maxCoverage, 
                    String description, String terms) {
            this.minPremium = minPremium;
            this.maxPremium = maxPremium;
            this.minCoverage = minCoverage;
            this.maxCoverage = maxCoverage;
            this.description = description;
            this.terms = terms;
        }
    }

    private Agent createDefaultAgent() {
        try {
            // Check if default agent already exists
            Optional<User> existingUser = userRepository.findByUsername("default.agent");
            if (existingUser.isPresent()) {
                Optional<Agent> existingAgent = agentRepository.findByUser(existingUser.get());
                if (existingAgent.isPresent()) {
                    return existingAgent.get();
                }
            }

            // Create default user for agent
            User user = new User();
            user.setUsername("default.agent");
            user.setPassword(passwordEncoder.encode("agent123"));
            user.setEmail("default.agent@insurance.com");
            user.setFirstName("Default");
            user.setLastName("Agent");
            user.setPhoneNumber("000-000-0000");
            user.setRole(com.insurancems.entity.enums.Role.AGENT);
            user = userRepository.save(user);

            // Create default agent
            Agent agent = new Agent();
            agent.setUser(user);
            agent.setLicenseNumber("AG-DEFAULT-001");
            agent.setCommissionRate(new BigDecimal("0.10"));
            return agentRepository.save(agent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create default agent", e);
        }
    }
} 