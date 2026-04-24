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

        // --- 1. HEADER (THANH CÔNG CỤ) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        JLabel lblFilter = new JLabel("Lọc trạng thái:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(lblFilter);
        
        // Thêm trạng thái RETURNED vào bộ lọc
        cbbFilter = new JComboBox<>(new String[]{"Tất cả", "NEW", "CONFIRMED", "SHIPPING", "DONE", "RETURNED", "CANCELLED"});
        cbbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbbFilter.setBackground(Color.WHITE);
        topPanel.add(cbbFilter);

        JButton btnRefresh = new JButton("Tải lại");
        styleButton(btnRefresh, Color.GRAY);
        topPanel.add(btnRefresh);
        
        topPanel.add(Box.createHorizontalStrut(20));

        // Các nút hành động
        JButton btnConfirm = new JButton("Duyệt Đơn");
        styleButton(btnConfirm, new Color(46, 204, 113));

        JButton btnShip = new JButton("Giao Hàng");
        styleButton(btnShip, new Color(52, 152, 219));

        JButton btnDone = new JButton("Hoàn Tất");
        styleButton(btnDone, new Color(39, 174, 96));

        JButton btnCancel = new JButton("Hủy Đơn");
        styleButton(btnCancel, new Color(231, 76, 60));

        // NÚT MỚI: HOÀN TRẢ
        JButton btnReturn = new JButton("Hoàn Trả");
        styleButton(btnReturn, new Color(155, 89, 182)); // Màu tím chuyên nghiệp

        topPanel.add(btnConfirm);
        topPanel.add(btnShip);
        topPanel.add(btnDone);
        topPanel.add(btnReturn); // Thêm nút Hoàn trả vào thanh công cụ
        topPanel.add(btnCancel);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        model = new DefaultTableModel() {
             @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{ "ID", "Người nhận", "SĐT", "Địa chỉ", "Tổng tiền", "Thanh toán", "Trạng thái", "Ngày tạo" });

        table = new JTable(model);
        styleTable(table);

        // Đánh dấu đơn hàng mới (Highlight NEW)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getModel().getValueAt(row, 6);
                if ("NEW".equals(status)) {
                    c.setBackground(new Color(255, 248, 225));
                    c.setForeground(new Color(211, 84, 0));
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

        // --- SỰ KIỆN (EVENTS) ---
        cbbFilter.addActionListener(e -> loadOrders());
        btnRefresh.addActionListener(e -> loadOrders());
        btnConfirm.addActionListener(e -> changeStatus("CONFIRMED"));
        btnShip.addActionListener(e -> changeStatus("SHIPPING"));
        btnDone.addActionListener(e -> changeStatus("DONE"));
        btnCancel.addActionListener(e -> changeStatus("CANCELLED"));
        
        // Sự kiện cho nút Hoàn trả
        btnReturn.addActionListener(e -> showReturnDialog());

        // Tự động làm mới mỗi 5 giây
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (table.getSelectedRow() == -1) loadOrders();
            }
        }, 0, 5000);
    }

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
                    model.addRow(new Object[]{ 
                        o.getId(), o.getReceiverName(), o.getPhone(), o.getAddress(), 
                        String.format("%,.0f đ", o.getTotalAmount()), o.getPaymentMethod(), 
                        o.getStatus(), o.getCreatedAt() 
                    });
                }
            }
        });
    }

    private void changeStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng!"); return; }
        int orderId = (int) model.getValueAt(row, 0);
        
        if (JOptionPane.showConfirmDialog(this, "Đổi trạng thái thành " + newStatus + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (OrderApi.updateStatus(orderId, newStatus)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadOrders();
            }
        }
    }

    // LOGIC XỬ LÝ HOÀN TRẢ
    private void showReturnDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn đơn hàng muốn hoàn trả!"); return; }

        int orderId = (int) model.getValueAt(row, 0);
        String currentStatus = (String) model.getValueAt(row, 6);

        // Kiểm tra điều kiện: Thường chỉ cho hoàn trả đơn đã hoàn tất (DONE) hoặc đang giao (SHIPPING)
        if ("CANCELLED".equals(currentStatus) || "RETURNED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Đơn hàng này không thể thực hiện hoàn trả!");
            return;
        }

        JTextField txtReason = new JTextField();
        String[] conditions = {"Hàng Tốt (Cộng lại vào kho bán tiếp)", "Hàng Lỗi (Không cộng lại kho)"};
        JComboBox<String> cbCondition = new JComboBox<>(conditions);

        Object[] form = {
            "Lý do hoàn trả:", txtReason,
            "Tình trạng sản phẩm:", cbCondition
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Xử lý Hoàn trả Đơn #" + orderId, JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String reason = txtReason.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do hoàn trả!");
                return;
            }

            String condition = (cbCondition.getSelectedIndex() == 0) ? "GOOD" : "BAD";

            if (OrderApi.returnOrder(orderId, reason, condition)) {
                JOptionPane.showMessageDialog(this, "Đã xử lý hoàn trả và ghi nhận lịch sử kho!");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Thao tác thất bại, vui lòng kiểm tra lại server!");
            }
        }
    }

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
