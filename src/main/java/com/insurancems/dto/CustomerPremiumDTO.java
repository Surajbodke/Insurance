package com.insurancems.dto;

public class CustomerPremiumDTO {
    private String customerName;
    private String customerEmail;
    private Double totalPremium;
    private Integer policyCount;

    public CustomerPremiumDTO(String customerName, String customerEmail, Double totalPremium, Integer policyCount) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalPremium = totalPremium;
        this.policyCount = policyCount;
    }

    // Getters and setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Double getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(Double totalPremium) {
        this.totalPremium = totalPremium;
    }

    public Integer getPolicyCount() {
        return policyCount;
    }

    public void setPolicyCount(Integer policyCount) {
        this.policyCount = policyCount;
    }
} 