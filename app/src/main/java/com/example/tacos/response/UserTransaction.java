package com.example.tacos.response;

import java.time.LocalDateTime;

public class UserTransaction {
    private int transactionId;
    private int userId;
    private int categoryId;
    private double amount;
    private String note;
    private LocalDateTime date;
    private String categoryName;
    private String categoryType;

    public UserTransaction(int transactionId, int userId, int categoryId, double amount,
                           String note, LocalDateTime date) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.note = note;
        this.date = date;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

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
