/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author AlinV
 */
public class OrderDTO {
    private int id;
    private String receiverName;
    private String phone;
    private String address;
    private double totalAmount;
    private String status;
    private String paymentMethod;
    private String createdAt;
    
    // ✅ THÊM 2 TRƯỜNG DỮ LIỆU VNPay VÀO ĐÂY
    private String paymentStatus;
    private String vnpayTransactionNo;

    // ✅ CẬP NHẬT LẠI CONSTRUCTOR ĐỂ NHẬN 2 TRƯỜNG MỚI NÀY
    public OrderDTO(int id, String receiverName, String phone, String address, double totalAmount, String status, String paymentMethod, String createdAt, String paymentStatus, String vnpayTransactionNo) {
        this.id = id;
        this.receiverName = receiverName;
        this.phone = phone;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.paymentStatus = paymentStatus;
        this.vnpayTransactionNo = vnpayTransactionNo;
    }

    public int getId() { return id; }
    public String getReceiverName() { return receiverName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCreatedAt() { return createdAt; }
    
    // ✅ THÊM GETTER CHO 2 TRƯỜNG MỚI
    public String getPaymentStatus() { return paymentStatus; }
    public String getVnpayTransactionNo() { return vnpayTransactionNo; }
    
    public void setStatus(String status) { this.status = status; }
}

