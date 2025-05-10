package com.example.tacos.model;

import java.time.LocalDateTime;

public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId;
    private double amount;
    private String note;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String frequency;
    private String categoryName;
    private String categoryType;

    public Budget(int budgetId, int userId, int categoryId, double amount, String note,
                  LocalDateTime startDate, LocalDateTime endDate, String frequency) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.note = note;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setTransactionId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {return amount;}

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() { return frequency; }

    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }
}
