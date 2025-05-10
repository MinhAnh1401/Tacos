package com.example.tacos;

public class User {
    private int userId;
    private String fullName;
    private String phone;
    private String password;
    
    public User(int userId, String fullName, String phone, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
    }

    public int getUserId() { return userId; }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
