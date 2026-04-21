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

    public OrderDTO(int id, String receiverName, String phone, String address, double totalAmount, String status, String paymentMethod, String createdAt) {
        this.id = id;
        this.receiverName = receiverName;
        this.phone = phone;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getReceiverName() { return receiverName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCreatedAt() { return createdAt; }
    
    public void setStatus(String status) { this.status = status; }
}

