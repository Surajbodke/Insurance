package com.insurancems.config;

import com.insurancems.entity.Agent;
import com.insurancems.entity.Customer;
import com.insurancems.entity.User;
import com.insurancems.entity.enums.Role;
import com.insurancems.repository.AgentRepository;
import com.insurancems.repository.CustomerRepository;
import com.insurancems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@insurance.com");
            admin.setRole(Role.ADMIN);
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setPhoneNumber("1234567890");
            userRepository.save(admin);
        }

        // Create agent if not exists
        if (!userRepository.existsByUsername("agent")) {
            User agentUser = new User();
            agentUser.setUsername("agent");
            agentUser.setPassword(passwordEncoder.encode("agent123"));
            agentUser.setEmail("agent@insurance.com");
            agentUser.setRole(Role.AGENT);
            agentUser.setFirstName("Agent");
            agentUser.setLastName("User");
            agentUser.setPhoneNumber("1234567891");
            userRepository.save(agentUser);

            Agent agent = new Agent();
            agent.setUser(agentUser);
            agent.setLicenseNumber("AGENT123");
            agent.setCommissionRate(new BigDecimal("0.10"));
            agentRepository.save(agent);
        }

        // Create customer if not exists
        if (!userRepository.existsByUsername("customer")) {
            User customerUser = new User();
            customerUser.setUsername("customer");
            customerUser.setPassword(passwordEncoder.encode("customer123"));
            customerUser.setEmail("customer@insurance.com");
            customerUser.setRole(Role.CUSTOMER);
            customerUser.setFirstName("Customer");
            customerUser.setLastName("User");
            customerUser.setPhoneNumber("1234567892");
            userRepository.save(customerUser);

            Customer customer = new Customer();
            customer.setUser(customerUser);
            customer.setAddress("123 Main St");
            customer.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
            customerRepository.save(customer);
        }
    }
} 