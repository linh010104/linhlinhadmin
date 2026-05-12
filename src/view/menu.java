/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import adminlienketweb.LoginForm;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author AlinV
 */
public class menu extends JFrame{
    private JPanel contentPanel;
    
    public menu() {
        setTitle("HỆ THỐNG QUẢN LÝ LINHLINH STORE");
        setSize(1280, 800); // Rộng hơn xíu cho thoáng
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("HỆ THỐNG QUẢN LÝ DÀNH CHO ADMIN", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 153));
        header.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        // Sidebar Menu
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 15)); // Tăng khoảng cách các nút
        menuPanel.setBackground(new Color(240, 245, 250)); // Màu nền menu hiện đại hơn
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setPreferredSize(new Dimension(250, 0)); // Chốt cứng chiều rộng menu

        JButton btnCategory = createMenuButton("Quản lý loại sản phẩm");
        JButton btnProduct = createMenuButton("Quản lý sản phẩm");
        JButton btnTable = createMenuButton("Quản lý đơn hàng");
        JButton btnDatban = createMenuButton("Danh sách Nhà cung cấp");
        JButton btnAccount = createMenuButton("Quản lý tài khoản");
        JButton btnEmployee = createMenuButton("Quản lý kho hàng");
        JButton btnRevenue = createMenuButton("Quản lý thống kê");
        JButton btnLogout = createMenuButton("Đăng xuất");
        btnLogout.setForeground(new Color(220, 53, 69)); // Nút đăng xuất màu đỏ cho nổi

        menuPanel.add(btnCategory);
        menuPanel.add(btnProduct);
        menuPanel.add(btnTable);
        menuPanel.add(btnDatban);
        menuPanel.add(btnAccount);
        menuPanel.add(btnEmployee);
        menuPanel.add(btnRevenue);
        menuPanel.add(btnLogout);
        add(menuPanel, BorderLayout.WEST);

        // Content Area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        add(contentPanel, BorderLayout.CENTER);

        // Nạp Dashboard mặc định thay cho dòng text cũ
        showPanel(createDashboardPanel());

        // Events
        btnCategory.addActionListener(e -> showPanel(new CategoryBrandJframe()));
        btnProduct.addActionListener(e -> showPanel(new ProductJframe()));
        btnTable.addActionListener(e -> showPanel(new OrderJFrame()));
        btnDatban.addActionListener(e -> showPanel(new VendorJFrame()));
        btnAccount.addActionListener(e -> showPanel(new UserJframe()));
        btnEmployee.addActionListener(e -> showPanel(new InventoryJFrame()));
        btnRevenue.addActionListener(e -> showPanel(new RevenueJFrame()));
        
        // Nút trang chủ (Bấm vào Header để quay về Dashboard)
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showPanel(createDashboardPanel());
            }
        });

        btnLogout.addActionListener(e -> exit());
        setVisible(true);
    }
    
    // Hàm tạo nút Menu dùng chung để dễ custom style
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Color.WHITE);
        return btn;
    }

    // ==========================================================
    // KHU VỰC THIẾT KẾ DASHBOARD TRANG CHỦ
    // ==========================================================
    private JPanel createDashboardPanel() {
        JPanel dashPanel = new JPanel(new BorderLayout(20, 20));
        dashPanel.setBackground(new Color(248, 249, 250));
        dashPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Tiêu đề Dashboard
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("TỔNG QUAN HỆ THỐNG", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 50));
        JLabel lblSub = new JLabel("Cập nhật theo thời gian thực", SwingConstants.LEFT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        
        dashPanel.add(headerPanel, BorderLayout.NORTH);

        // Khu vực chứa các thẻ thống kê
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardsPanel.setOpaque(false);

        // Thẻ 1: Doanh thu (Màu xanh lá)
        cardsPanel.add(createStatCard("Tổng Doanh Thu Trong Ngày", "15,420,000 đ", new Color(40, 167, 69)));
        
        // Thẻ 2: Đơn hàng mới (Màu xanh dương)
        cardsPanel.add(createStatCard("Đơn Hàng Mới Chờ Xử Lý", "12 Đơn", new Color(0, 123, 255)));
        
        // Thẻ 3: Tồn kho (Màu đỏ cảnh báo)
        cardsPanel.add(createStatCard("Sản Phẩm Sắp Hết Hàng", "5 Sản phẩm", new Color(220, 53, 69)));

        // Đẩy các thẻ lên trên cùng, chừa khoảng trống bên dưới
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardsPanel, BorderLayout.NORTH);
        
        dashPanel.add(centerWrapper, BorderLayout.CENTER);

        return dashPanel;
    }

    // Hàm tạo giao diện cho từng thẻ Card thống kê
    private JPanel createStatCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(Color.WHITE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }
    // ==========================================================

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER); 
        contentPanel.revalidate(); 
        contentPanel.repaint();
    }
    
    private void exit(){
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất không?", "Xác nhận", JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.OK_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}