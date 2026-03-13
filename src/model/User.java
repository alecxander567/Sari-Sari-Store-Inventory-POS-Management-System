package model;

import java.sql.Timestamp;

public class User {

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private Timestamp createdAt;
    private Integer storeId;

    public User() {
    }

    public User(int userId, String username, String password, String fullName, Timestamp createdAt, Integer storeId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.storeId = storeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }
}