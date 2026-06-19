/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.UserDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import khongphaigiaodien.UserApi;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/**
 *
 * @author AlinV
 */
public class UserJframe extends JPanel{
    JTable table;
    DefaultTableModel model;
    private List<UserDTO> fullUserList;

    public UserJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        top.setBackground(Color.WHITE);

        JButton btnLock = new JButton("Khóa / Mở Tài Khoản");
        styleButton(btnLock, new Color(230, 126, 34)); // Màu cam

        btnLock.addActionListener(e -> toggleStatus());
        top.add(btnLock);
        top.add(new JLabel("  |  Tìm kiếm:"));
        JTextField txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(txtSearch.getWidth(), 30));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        top.add(txtSearch);

        JButton btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, new Color(0, 123, 255));
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim().toLowerCase();
            filterUsers(keyword);
        });
        top.add(btnSearch);

        JButton btnReset = new JButton("Làm mới");
        styleButton(btnReset, Color.GRAY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers();
        });
        top.add(btnReset);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{ "ID", "Username", "Họ tên", "Email", "SĐT", "Quyền", "Trạng thái" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        styleTable(table);
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (table.getSelectedRow() != -1) showUserDetails(table.getSelectedRow());
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(sp, BorderLayout.CENTER);

        loadUsers();
    }

    private void loadUsers() {
        model.setRowCount(0);
        fullUserList = UserApi.getAll(); // Gán vào đây thì filter mới biết đường mà lọc
        for (UserDTO u : fullUserList) {
            model.addRow(new Object[]{ u.id, u.username, u.fullName, u.email, u.phone, u.roleName, u.status == 1 ? "Hoạt động" : "Khóa" });
        }
    }
    private void filterUsers(String keyword) {
        if (fullUserList == null) return;
        model.setRowCount(0);
        for (UserDTO u : fullUserList) {
            // Tìm theo Username, Email, Họ Tên hoặc SĐT
            if (u.username.toLowerCase().contains(keyword) || 
                (u.email != null && u.email.toLowerCase().contains(keyword)) ||
                (u.fullName != null && u.fullName.toLowerCase().contains(keyword)) ||
                (u.phone != null && u.phone.contains(keyword))) {
                
                model.addRow(new Object[]{ u.id, u.username, u.fullName, u.email, u.phone, u.roleName, u.status == 1 ? "Hoạt động" : "Khóa" });
            }
        }
    }
    
    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn một tài khoản để thao tác!"); return; }
        int id = (int) model.getValueAt(row, 0);
        String statusText = model.getValueAt(row, 6).toString();
        int newStatus = statusText.equals("Hoạt động") ? 0 : 1;
        String confirmMsg = newStatus == 0 ? "KHÓA tài khoản này?" : "MỞ KHÓA tài khoản này?";
        if (JOptionPane.showConfirmDialog(this, confirmMsg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (UserApi.changeStatus(id, newStatus)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadUsers();
            }
        }
    }

    private void showUserDetails(int row) {
        String msg = "THÔNG TIN CHI TIẾT\n-----------------\n" +
                     "Username: " + model.getValueAt(row, 1) + "\n" +
                     "Họ tên: " + model.getValueAt(row, 2) + "\n" +
                     "Email: " + model.getValueAt(row, 3) + "\n" +
                     "SĐT: " + model.getValueAt(row, 4) + "\n" +
                     "Quyền: " + model.getValueAt(row, 5) + "\n" +
                     "Trạng thái: " + model.getValueAt(row, 6);
        JOptionPane.showMessageDialog(this, msg, "Chi tiết", JOptionPane.INFORMATION_MESSAGE);
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
