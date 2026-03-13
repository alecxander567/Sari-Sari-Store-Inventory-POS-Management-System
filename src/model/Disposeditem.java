package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Disposeditem {
    private int        disposeId;
    private int        productId;
    private String     productName;
    private int        quantity;
    private String     reason;
    private Timestamp  disposedAt;
    private BigDecimal price;
    private String     categoryName;
    private String     supplierName;

    public int        getDisposeId()             { return disposeId; }
    public void       setDisposeId(int v)         { this.disposeId = v; }
    public int        getProductId()              { return productId; }
    public void       setProductId(int v)         { this.productId = v; }
    public String     getProductName()            { return productName; }
    public void       setProductName(String v)    { this.productName = v; }
    public int        getQuantity()               { return quantity; }
    public void       setQuantity(int v)          { this.quantity = v; }
    public String     getReason()                 { return reason; }
    public void       setReason(String v)         { this.reason = v; }
    public Timestamp  getDisposedAt()             { return disposedAt; }
    public void       setDisposedAt(Timestamp v)  { this.disposedAt = v; }
    public BigDecimal getPrice()                  { return price; }
    public void       setPrice(BigDecimal v)      { this.price = v; }
    public String     getCategoryName()           { return categoryName; }
    public void       setCategoryName(String v)   { this.categoryName = v; }
    public String     getSupplierName()           { return supplierName; }
    public void       setSupplierName(String v)   { this.supplierName = v; }
}