package com.insurancems.controller;

import com.insurancems.entity.*;
import com.insurancems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/policy")
public class PolicyController {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/create")
    public String showCreatePolicyForm(Model model) {
        model.addAttribute("policy", new Policy());
        return "policy/create";
    }

    @PostMapping("/create")
    public String createPolicy(@ModelAttribute Policy policy,
                             @RequestParam String customerUsername,
                             @RequestParam String agentUsername) {
        // Get or create customer
        User customerUser = userRepository.findByUsername(customerUsername)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(customerUsername);
                newUser.setPassword(passwordEncoder.encode("default123"));
                newUser.setEmail(customerUsername + "@example.com");
                newUser.setFirstName("Customer");
                newUser.setLastName("User");
                newUser.setPhoneNumber("1234567890");
                newUser.setRole(com.insurancems.entity.enums.Role.CUSTOMER);
                return userRepository.save(newUser);
            });

        Customer customer = customerRepository.findByUser(customerUser)
            .orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setUser(customerUser);
                newCustomer.setAddress("Default Address");
                newCustomer.setDateOfBirth(LocalDate.now());
                return customerRepository.save(newCustomer);
            });

        // Get or create agent
        User agentUser = userRepository.findByUsername(agentUsername)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(agentUsername);
                newUser.setPassword(passwordEncoder.encode("default123"));
                newUser.setEmail(agentUsername + "@example.com");
                newUser.setFirstName("Agent");
                newUser.setLastName("User");
                newUser.setPhoneNumber("1234567891");
                newUser.setRole(com.insurancems.entity.enums.Role.AGENT);
                return userRepository.save(newUser);
            });

        Agent agent = agentRepository.findByUser(agentUser)
            .orElseGet(() -> {
                Agent newAgent = new Agent();
                newAgent.setUser(agentUser);
                newAgent.setLicenseNumber("AGENT" + System.currentTimeMillis());
                newAgent.setCommissionRate(new BigDecimal("0.10"));
                return agentRepository.save(newAgent);
            });

        // Set customer and agent for the policy
        policy.setCustomer(customer);
        policy.setAgent(agent);
        policyRepository.save(policy);

        return "redirect:/policy/list";
    }

    @GetMapping("/list")
    public String listPolicies(Model model) {
        model.addAttribute("policies", policyRepository.findAll());
        return "policy/list";
    }
} 