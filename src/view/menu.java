/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import adminlienketweb.LoginForm;
import java.awt.BorderLayout;
import java.awt.Color;
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
        
        setTitle("FORM QUẢN LÝ CỦA LINH");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("HỆ THỐNG QUẢN LÝ DÀNH CHO ADMIN", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 153));
        header.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuPanel.setBackground(new Color(230, 240, 250));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnCategory = new JButton(" Quản lý loại sản phẩm");
        JButton btnProduct = new JButton(" Quản lý sản phẩm");
        JButton btnTable = new JButton(" Quản lý đơn hàng ");
        JButton btnDatban = new JButton(" Danh sách Nhà cung cấp ");
        JButton btnAccount = new JButton(" Quản lý tài khoản ");
        JButton btnEmployee = new JButton(" Quản lý kho hàng");
        JButton btnRevenue = new JButton(" Quản lý thống kê");
        JButton btnLogout = new JButton(" Đăng xuất");

        menuPanel.add(btnCategory);
        menuPanel.add(btnProduct);
        menuPanel.add(btnTable);
        menuPanel.add(btnDatban);
        menuPanel.add(btnAccount);
        menuPanel.add(btnEmployee);
        menuPanel.add(btnRevenue);
        menuPanel.add(btnLogout);
        add(menuPanel, BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        add(contentPanel, BorderLayout.CENTER);

        btnCategory.addActionListener(e -> showPanel(new CategoryJframe()));
        btnProduct.addActionListener(e -> showPanel(new ProductJframe()));
        btnTable.addActionListener(e -> showPanel(new OrderJFrame()));
        btnDatban.addActionListener(e -> showPanel(new VendorJFrame()));
        btnAccount.addActionListener(e -> showPanel(new UserJframe()));
        btnEmployee.addActionListener(e -> showPanel(new InventoryJFrame()));
        btnRevenue.addActionListener(e -> showPanel(new RevenueJFrame()));
        btnLogout.addActionListener(e -> exit());

        JLabel defaultLabel = new JLabel("Chào mừng bạn đến với hệ thống quản lý!", SwingConstants.CENTER);
        defaultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentPanel.add(defaultLabel, BorderLayout.CENTER);

        setVisible(true);
    }

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
