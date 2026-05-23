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
        
        cbbFilter = new JComboBox<>(new String[]{"Tất cả", "NEW", "CONFIRMED", "SHIPPING", "DONE", "RETURN_REQUESTED", "RETURNED", "CANCELLED"});
        cbbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbbFilter.setBackground(Color.WHITE);
        topPanel.add(cbbFilter);

        JButton btnRefresh = new JButton("Tải lại");
        styleButton(btnRefresh, Color.GRAY);
        topPanel.add(btnRefresh);
        
        topPanel.add(Box.createHorizontalStrut(20));

        JButton btnConfirm = new JButton("Duyệt Đơn");
        styleButton(btnConfirm, new Color(46, 204, 113));

        JButton btnShip = new JButton("Giao Hàng");
        styleButton(btnShip, new Color(52, 152, 219));

        JButton btnDone = new JButton("Hoàn Tất");
        styleButton(btnDone, new Color(39, 174, 96));

        JButton btnReturn = new JButton("Xử Lý Hoàn Trả");
        styleButton(btnReturn, new Color(155, 89, 182)); 

        JButton btnCancel = new JButton("Hủy Đơn");
        styleButton(btnCancel, new Color(231, 76, 60));

        topPanel.add(btnConfirm);
        topPanel.add(btnShip);
        topPanel.add(btnDone);
        topPanel.add(btnReturn); 
        topPanel.add(btnCancel);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        model = new DefaultTableModel() {
             @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        // ✅ CẬP NHẬT TÊN CỘT ĐỂ HIỂN THỊ THÊM TRẠNG THÁI THANH TOÁN VÀ MÃ GD VNPay
        model.setColumnIdentifiers(new String[]{ "ID", "Người nhận", "SĐT", "Địa chỉ", "Tổng tiền", "PT Thanh toán", "TT Thanh toán", "Mã GD VNPay", "Trạng thái", "Ngày tạo" });

        table = new JTable(model);
        styleTable(table);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getModel().getValueAt(row, 8); // ✅ Đổi index từ 6 thành 8 do đã chèn thêm 2 cột ở trước
                String paymentStatus = (String) table.getModel().getValueAt(row, 6);
                
                // Tô màu riêng biệt nếu đơn hàng ĐÃ THANH TOÁN VNPay (PAID)
                if ("PAID".equals(paymentStatus) && column == 6) {
                    c.setForeground(new Color(46, 204, 113)); // Màu xanh lá uy tín
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }

                if ("RETURN_REQUESTED".equals(status)) {
                    c.setBackground(new Color(255, 235, 238));
                    c.setForeground(new Color(192, 57, 43));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if ("NEW".equals(status)) {
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
        
        btnReturn.addActionListener(e -> showReturnDialog());

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (table.getSelectedRow() == -1) loadOrders();
            }
        }, 0, 5000);
        
    }
    public OrderJFrame(String initialFilter) {
        this(); // Gọi lại toàn bộ giao diện ở hàm mặc định
        cbbFilter.setSelectedItem(initialFilter); // Tự động đổi combo box sang trạng thái truyền vào (vd: "NEW")
    }

    private void loadOrders() {
        SwingUtilities.invokeLater(() -> {
            List<OrderDTO> orders = OrderApi.getAllOrders();
            String filter = (String) cbbFilter.getSelectedItem();
            
            int reqCount = 0; // Đếm yêu cầu trả hàng
            int stuckVnpayCount = 0; // Đếm đơn VNPay treo quá 30p
            
            for(OrderDTO o : orders) {
                if("RETURN_REQUESTED".equals(o.getStatus())) {
                    reqCount++;
                }
                // Tính toán đơn VNPay treo (NEW + VNPAY + PENDING)
                if("NEW".equals(o.getStatus()) && "VNPAY".equals(o.getPaymentMethod()) && "PENDING".equals(o.getPaymentStatus())) {
                    try {
                        // So sánh thời gian tạo đơn với thời gian hiện tại
                        java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(o.getCreatedAt().replace(" ", "T").substring(0, 19));
                        long minutesBetween = java.time.temporal.ChronoUnit.MINUTES.between(createdAt, java.time.LocalDateTime.now());
                        
                        if(minutesBetween >= 30) {
                            stuckVnpayCount++;
                        }
                    } catch (Exception ex) {
                        // Bỏ qua nếu parse ngày tháng lỗi
                    }
                }
            }
            
            if (reqCount > currentOrderCount || stuckVnpayCount > 0) Toolkit.getDefaultToolkit().beep();
            currentOrderCount = reqCount;
            
            // 🔥 CẬP NHẬT LABEL TRẠNG THÁI (Đổi màu đỏ nếu có đơn treo)
            String statusText = "Cập nhật: " + java.time.LocalTime.now() + " | Yêu cầu trả hàng: " + reqCount;
            if (stuckVnpayCount > 0) {
                statusText += " | ⚠️ CẢNH BÁO: CÓ " + stuckVnpayCount + " ĐƠN VNPAY TREO QUÁ 30 PHÚT!";
                lblStatus.setForeground(Color.RED);
                lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD));
            } else {
                lblStatus.setForeground(Color.GRAY);
                lblStatus.setFont(lblStatus.getFont().deriveFont(Font.PLAIN));
            }
            lblStatus.setText(statusText);

            model.setRowCount(0);
            for (OrderDTO o : orders) {
                if ("Tất cả".equals(filter) || o.getStatus().equals(filter)) {
                    model.addRow(new Object[]{ 
                        o.getId(), o.getReceiverName(), o.getPhone(), o.getAddress(), 
                        o.getTotalAmount(), o.getPaymentMethod(), 
                        o.getPaymentStatus(), 
                        o.getVnpayTransactionNo() == null || o.getVnpayTransactionNo().isEmpty() ? "Trống" : o.getVnpayTransactionNo(), 
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
        String paymentStatus = (String) model.getValueAt(row, 6); // Cột trạng thái thanh toán
        
        // ✅ Cảnh báo nếu Admin chọn Hủy đơn nhưng đơn này đã được khách chuyển tiền qua VNPay
        if ("CANCELLED".equals(newStatus) && "PAID".equals(paymentStatus)) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "⚠️ Đơn hàng này đã được KHÁCH THANH TOÁN (VNPay).\nBạn có chắc chắn muốn hủy? (Cần hoàn tiền thủ công cho khách sau khi hủy)", 
                "Cảnh báo hủy đơn đã thanh toán", 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        } else {
             if (JOptionPane.showConfirmDialog(this, "Đổi trạng thái thành " + newStatus + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                 return;
             }
        }

        if (OrderApi.updateStatus(orderId, newStatus)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadOrders();
        }
    }

    // --- LOGIC XỬ LÝ HOÀN TRẢ VỚI 3 NÚT ---
    private void showReturnDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn đơn hàng muốn xử lý!"); return; }

        int orderId = (int) model.getValueAt(row, 0);
        String currentStatus = (String) model.getValueAt(row, 8); // ✅ Đổi index từ 6 thành 8
        String paymentStatus = (String) model.getValueAt(row, 6);

        if (!"RETURN_REQUESTED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Chỉ xử lý được những đơn đang có Yêu cầu trả hàng (RETURN_REQUESTED)!");
            return;
        }
        
        // ✅ Nếu khách yêu cầu trả hàng, hệ thống nhắc admin nhớ hoàn lại tiền VNPay
        String msgVNPay = "";
        if ("PAID".equals(paymentStatus)) {
            msgVNPay = "\n(🔔 Lưu ý: Đơn này khách đã thanh toán VNPay, hãy nhớ hoàn tiền cho khách qua cổng)";
        }

        String khachYeuCau = OrderApi.getOrderReturnReason(orderId);

        JTextArea txtKhach = new JTextArea(4, 30);
        txtKhach.setText(khachYeuCau + msgVNPay);
        txtKhach.setEditable(false);
        txtKhach.setLineWrap(true);
        txtKhach.setWrapStyleWord(true);
        txtKhach.setBackground(new java.awt.Color(240, 240, 240));
        JScrollPane scrollKhach = new JScrollPane(txtKhach);

        JTextField txtAdminNote = new JTextField();
        String[] conditions = {"Hàng Tốt (Cộng lại vào kho)", "Hàng Lỗi (Không cộng kho)"};
        JComboBox<String> cbCondition = new JComboBox<>(conditions);

        Object[] form = {
            "Lý do khách hàng yêu cầu:", scrollKhach,
            "Ghi chú của Shop (Gửi vào Email khách):", txtAdminNote,
            "Tình trạng sản phẩm (Chỉ dùng nếu Chấp nhận):", cbCondition
        };

        // Tạo 3 nút tùy chỉnh
        Object[] options = {"Chấp nhận hoàn trả", "Từ chối trả hàng", "Hủy bỏ"};
        
        int res = JOptionPane.showOptionDialog(this, form, 
                "Duyệt Hoàn trả Đơn #" + orderId,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (res == 0) { 
            // BẤM: CHẤP NHẬN
            String adminNote = txtAdminNote.getText().trim();
            String condition = cbCondition.getSelectedIndex() == 0 ? "GOOD" : "BAD";
            
            if (OrderApi.processReturn(orderId, "ACCEPT", adminNote, condition)) {
                JOptionPane.showMessageDialog(this, "Đã CHẤP NHẬN! Tiền hoàn, kho cập nhật và Email đã được gửi.");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi server, vui lòng kiểm tra lại Node.js!");
            }
            
        } else if (res == 1) { 
            // BẤM: TỪ CHỐI
            String adminNote = txtAdminNote.getText().trim();
            if (adminNote.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bắt buộc phải nhập 'Ghi chú của Shop' để giải thích lý do Từ chối!");
                return; 
            }
            
            if (OrderApi.processReturn(orderId, "REJECT", adminNote, "")) {
                JOptionPane.showMessageDialog(this, "Đã TỪ CHỐI! Đơn chuyển về DONE và Email đã được gửi.");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi server, vui lòng kiểm tra lại Node.js!");
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
