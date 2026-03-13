package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {
    private int        productId;
    private String     productName;
    private BigDecimal price;
    private int        stockQuantity;
    private Integer    createdBy;
    private Timestamp  createdAt;
    private Integer    categoryId;
    private Integer    supplierId;
    private String     categoryName;
    private String     supplierName;

    public int        getProductId()            { return productId; }
    public void       setProductId(int v)        { this.productId = v; }
    public String     getProductName()           { return productName; }
    public void       setProductName(String v)   { this.productName = v; }
    public BigDecimal getPrice()                 { return price; }
    public void       setPrice(BigDecimal v)     { this.price = v; }
    public int        getStockQuantity()         { return stockQuantity; }
    public void       setStockQuantity(int v)    { this.stockQuantity = v; }
    public Integer    getCreatedBy()             { return createdBy; }
    public void       setCreatedBy(Integer v)    { this.createdBy = v; }
    public Timestamp  getCreatedAt()             { return createdAt; }
    public void       setCreatedAt(Timestamp v)  { this.createdAt = v; }
    public Integer    getCategoryId()            { return categoryId; }
    public void       setCategoryId(Integer v)   { this.categoryId = v; }
    public Integer    getSupplierId()            { return supplierId; }
    public void       setSupplierId(Integer v)   { this.supplierId = v; }
    public String     getCategoryName()          { return categoryName; }
    public void       setCategoryName(String v)  { this.categoryName = v; }
    public String     getSupplierName()          { return supplierName; }
    public void       setSupplierName(String v)  { this.supplierName = v; }
}