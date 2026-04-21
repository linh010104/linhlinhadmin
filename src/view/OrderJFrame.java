/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.OrderDTO;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import khongphaigiaodien.OrderApi;
/**
 *
 * @author AlinV
 */
public class OrderJFrame extends JPanel{
   private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbbFilter;
    private JLabel lblStatus;
    private int currentOrderCount = 0;

    public OrderJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. HEADER ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        // Filter
        JLabel lblFilter = new JLabel("Lọc trạng thái:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(lblFilter);
        
        cbbFilter = new JComboBox<>(new String[]{"Tất cả", "NEW", "CONFIRMED", "SHIPPING", "DONE", "CANCELLED"});
        cbbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbbFilter.setBackground(Color.WHITE);
        topPanel.add(cbbFilter);

        JButton btnRefresh = new JButton("Tải lại");
        styleButton(btnRefresh, Color.GRAY);
        topPanel.add(btnRefresh);
        
        topPanel.add(Box.createHorizontalStrut(30)); // Khoảng cách

        // Buttons Action
        JButton btnConfirm = new JButton("Duyệt Đơn");
        styleButton(btnConfirm, new Color(46, 204, 113)); // Xanh lá

        JButton btnShip = new JButton("Giao Hàng");
        styleButton(btnShip, new Color(52, 152, 219)); // Xanh dương

        JButton btnDone = new JButton("Hoàn Tất");
        styleButton(btnDone, new Color(149, 165, 166)); // Xám

        JButton btnCancel = new JButton("Hủy Đơn");
        styleButton(btnCancel, new Color(231, 76, 60)); // Đỏ

        topPanel.add(btnConfirm);
        topPanel.add(btnShip);
        topPanel.add(btnDone);
        topPanel.add(btnCancel);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABLE ---
        model = new DefaultTableModel() {
             @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{ "ID", "Người nhận", "SĐT", "Địa chỉ", "Tổng tiền", "Thanh toán", "Trạng thái", "Ngày tạo" });

        table = new JTable(model);
        styleTable(table);

        // Custom Renderer cho dòng NEW
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getModel().getValueAt(row, 6);
                if ("NEW".equals(status)) {
                    c.setBackground(new Color(255, 248, 225)); // Vàng rất nhạt
                    c.setForeground(new Color(211, 84, 0));   // Cam đậm
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (isSelected) {
                    c.setBackground(new Color(232, 242, 254));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        lblStatus = new JLabel("Hệ thống sẵn sàng...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.GRAY);
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- EVENTS ---
        cbbFilter.addActionListener(e -> loadOrders());
        btnRefresh.addActionListener(e -> loadOrders());
        btnConfirm.addActionListener(e -> changeStatus("CONFIRMED"));
        btnShip.addActionListener(e -> changeStatus("SHIPPING"));
        btnDone.addActionListener(e -> changeStatus("DONE"));
        btnCancel.addActionListener(e -> changeStatus("CANCELLED"));

        // Auto Refresh
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (table.getSelectedRow() == -1) loadOrders();
            }
        }, 0, 5000);
    }

    // --- LOGIC GIỮ NGUYÊN ---
    private void loadOrders() {
        SwingUtilities.invokeLater(() -> {
            List<OrderDTO> orders = OrderApi.getAllOrders();
            String filter = (String) cbbFilter.getSelectedItem();
            int newOrderCount = 0;
            for(OrderDTO o : orders) if("NEW".equals(o.getStatus())) newOrderCount++;
            
            if (newOrderCount > currentOrderCount) Toolkit.getDefaultToolkit().beep();
            currentOrderCount = newOrderCount;
            lblStatus.setText("Cập nhật: " + java.time.LocalTime.now() + " | Đơn mới: " + newOrderCount);

            model.setRowCount(0);
            for (OrderDTO o : orders) {
                if ("Tất cả".equals(filter) || o.getStatus().equals(filter)) {
                    model.addRow(new Object[]{ o.getId(), o.getReceiverName(), o.getPhone(), o.getAddress(), String.format("%,.0f đ", o.getTotalAmount()), o.getPaymentMethod(), o.getStatus(), o.getCreatedAt() });
                }
            }
        });
    }

    private void changeStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn đơn hàng trước!"); return; }
        int orderId = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Đổi trạng thái thành " + newStatus + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (OrderApi.updateStatus(orderId, newStatus)) {
                JOptionPane.showMessageDialog(this, "Thành công!");
                loadOrders();
            }
        }
    }

    // --- STYLE HELPER ---
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
