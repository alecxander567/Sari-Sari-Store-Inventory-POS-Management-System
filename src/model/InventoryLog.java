package model;

import java.time.LocalDateTime;

public class InventoryLog {

    private int logId;
    private int productId;
    private String productName;
    private int userId;
    private String operation;
    private int quantityChanged;
    private LocalDateTime createdAt;
    private String username;

    public InventoryLog() {}

    public InventoryLog(int productId, int userId, String operation, int quantityChanged) {
        this.productId = productId;
        this.userId = userId;
        this.operation = operation;
        this.quantityChanged = quantityChanged;
        this.createdAt = LocalDateTime.now();
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}