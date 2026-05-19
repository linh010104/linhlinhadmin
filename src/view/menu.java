/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import adminlienketweb.LoginForm;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

public class menu extends JFrame {
    private JPanel contentPanel;
    
    // Biến toàn cục UI
    private JLabel lblRevenueValue;
    private JLabel lblNewOrdersValue;
    private JLabel lblLowStockValue;
    
    private int[] chartData = {0, 0, 0, 0, 0, 0, 0};
    private String[] chartLabels = {"", "", "", "", "", "", ""};
    private int maxChartValue = 100; // Mức trần để scale cột
    private JPanel chartView; // Panel vẽ biểu đồ
    
    private DefaultTableModel recentOrdersModel;
    
    public menu() {
        setTitle("HỆ THỐNG QUẢN LÝ LINHLINH STORE");
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(new Color(0, 102, 153));
        headerContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel headerTitle = new JLabel("HỆ THỐNG QUẢN LÝ ", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerTitle.setForeground(Color.WHITE);
        headerContainer.add(headerTitle, BorderLayout.CENTER);

        JButton btnHomeIcon = new JButton("HOME");
        btnHomeIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHomeIcon.setForeground(Color.WHITE);
        btnHomeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHomeIcon.setFocusPainted(false);
        btnHomeIcon.setContentAreaFilled(false);
        btnHomeIcon.setBorderPainted(false);
        btnHomeIcon.addActionListener(e -> showPanel(createDashboardPanel()));
        
        headerContainer.add(btnHomeIcon, BorderLayout.EAST);
        add(headerContainer, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 12));
        menuPanel.setBackground(new Color(240, 245, 250));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setPreferredSize(new Dimension(260, 0));

        JButton btnCategory = createMenuButton("Quản lý loại sản phẩm");
        JButton btnProduct = createMenuButton("Quản lý sản phẩm");
        JButton btnTable = createMenuButton("Quản lý đơn hàng");
        JButton btnDatban = createMenuButton("Danh sách Nhà cung cấp");
        JButton btnAccount = createMenuButton("Quản lý tài khoản");
        JButton btnEmployee = createMenuButton("Quản lý kho hàng");
        JButton btnRevenue = createMenuButton("Quản lý thống kê");
        JButton btnLogout = createMenuButton("Đăng xuất");
        btnLogout.setForeground(new Color(220, 53, 69));

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
        contentPanel.setBackground(new Color(248, 249, 250));
        add(contentPanel, BorderLayout.CENTER);

        showPanel(createDashboardPanel());

        btnCategory.addActionListener(e -> showPanel(new CategoryBrandJframe()));
        btnProduct.addActionListener(e -> showPanel(new ProductJframe()));
        btnTable.addActionListener(e -> showPanel(new OrderJFrame()));
        btnDatban.addActionListener(e -> showPanel(new VendorJFrame()));
        btnAccount.addActionListener(e -> showPanel(new UserJframe()));
        btnEmployee.addActionListener(e -> showPanel(new InventoryJFrame()));
        btnRevenue.addActionListener(e -> showPanel(new RevenueJFrame()));
        btnLogout.addActionListener(e -> exit());
        
        setVisible(true);
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 240, 250)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private JPanel createDashboardPanel() {
        JPanel dashPanel = new JPanel(new BorderLayout(20, 20));
        dashPanel.setBackground(new Color(248, 249, 250));
        dashPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("TỔNG QUAN HỆ THỐNG", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 50));
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        dashPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 30));
        centerWrapper.setOpaque(false);

        // THẺ THỐNG KÊ (TOP)
        lblRevenueValue = new JLabel("Đang tải...", SwingConstants.CENTER);
        lblNewOrdersValue = new JLabel("Đang tải...", SwingConstants.CENTER);
        lblLowStockValue = new JLabel("Đang tải...", SwingConstants.CENTER);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 140));

        JPanel cardRev = createStatCard("Tổng Doanh Thu Hôm Nay", lblRevenueValue, new Color(40, 167, 69));
        cardRev.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPanel(new RevenueJFrame()); } });
        
        JPanel cardOrder = createStatCard("Đơn Hàng Mới Chờ Xử Lý", lblNewOrdersValue, new Color(0, 123, 255));
        cardOrder.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPanel(new OrderJFrame("NEW")); } });

        JPanel cardStock = createStatCard("Sản Phẩm Sắp Hết Hàng", lblLowStockValue, new Color(220, 53, 69));
        cardStock.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showPanel(new InventoryJFrame(true)); } });

        cardsPanel.add(cardRev);
        cardsPanel.add(cardOrder);
        cardsPanel.add(cardStock);
        centerWrapper.add(cardsPanel, BorderLayout.NORTH);

        // WIDGETS DƯỚI (BOTTOM)
        JPanel bottomWidgets = new JPanel(new GridLayout(1, 2, 30, 0));
        bottomWidgets.setOpaque(false);

        bottomWidgets.add(createChartPanel());       
        bottomWidgets.add(createRecentOrderPanel()); 

        centerWrapper.add(bottomWidgets, BorderLayout.CENTER);
        dashPanel.add(centerWrapper, BorderLayout.CENTER);

        // GỌI API ĐỂ FILL SỐ LIỆU THẬT VÀO TẤT CẢ CÁC THÀNH PHẦN
        fetchDashboardData();

        return dashPanel;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36)); 
        lblValue.setForeground(Color.WHITE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // PANEL VẼ BIỂU ĐỒ BẰNG GRAPHICS 2D VỚI DATA ĐỘNG
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Biểu Đồ Doanh Thu 7 Ngày Qua");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50));
        panel.add(title, BorderLayout.NORTH);

        chartView = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int padding = 30;
                int width = getWidth() - padding * 2;
                int height = getHeight() - padding * 2;
                int barWidth = width / chartData.length - 20;

                // Vẽ trục X
                g2.setColor(new Color(200, 200, 200));
                g2.drawLine(padding, height + padding, getWidth() - padding, height + padding); 

                // Vẽ các cột dựa theo tỷ lệ maxChartValue
                for (int i = 0; i < chartData.length; i++) {
                    int barHeight = (int) (((double) chartData[i] / maxChartValue) * height);
                    if (barHeight < 2) barHeight = 2; // Cột tối thiểu 2px để nhận biết ngày có mốc 0đ

                    int x = padding + 10 + i * (barWidth + 20);
                    int y = height + padding - barHeight;

                    g2.setColor(new Color(52, 152, 219)); 
                    g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString(chartLabels[i], x + barWidth / 6, height + padding + 20);
                }
            }
        };
        chartView.setBackground(Color.WHITE);
        panel.add(chartView, BorderLayout.CENTER);

        return panel;
    }

    // PANEL BẢNG ĐƠN HÀNG VỚI DATA ĐỘNG
    private JPanel createRecentOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Đơn Hàng Vừa Đặt");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Mã Đơn", "Khách Hàng", "Tổng Tiền", "Trạng Thái"};
        recentOrdersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(recentOrdersModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // HÀM LẤY DATA THẬT VÀ BƠM VÀO UI
    private void fetchDashboardData() {
        new Thread(() -> {
            try {
                URL url = new URL(khongphaigiaodien.ApiConfig.BASE_URL + "/stats/dashboard");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject json = new JSONObject(response.toString());
                if (json.getBoolean("success")) {
                    JSONObject data = json.getJSONObject("data");
                    
                    // 1. Lấy Thẻ Thống Kê
                    int revenue = data.getInt("revenueToday");
                    int newOrders = data.getInt("newOrders");
                    int lowStock = data.getInt("lowStock");

                    DecimalFormat formatter = new DecimalFormat("###,###,###");
                    String revString = formatter.format(revenue) + " đ";

                    // 2. Lấy Data Biểu Đồ
                    JSONArray labelsArr = data.getJSONArray("chartLabels");
                    JSONArray dataArr = data.getJSONArray("chartData");
                    maxChartValue = 100; // Reset mức tối thiểu
                    
                    for (int i = 0; i < 7; i++) {
                        chartLabels[i] = labelsArr.getString(i);
                        chartData[i] = dataArr.getInt(i);
                        if (chartData[i] > maxChartValue) maxChartValue = chartData[i]; // Tìm số lớn nhất để scale chiều cao
                    }

                    // Cập nhật UI trên Main Thread
                    SwingUtilities.invokeLater(() -> {
                        // Bơm text
                        lblRevenueValue.setText(revString);
                        lblNewOrdersValue.setText(newOrders + " Đơn");
                        lblLowStockValue.setText(lowStock + " SP");
                        
                        // Bơm lại biểu đồ
                        if (chartView != null) chartView.repaint();
                        
                        // Bơm bảng đơn hàng
                        recentOrdersModel.setRowCount(0); // Xóa data cũ
                        JSONArray ordersArr = data.getJSONArray("recentOrders");
                        for (int i = 0; i < ordersArr.length(); i++) {
                            JSONObject o = ordersArr.getJSONObject(i);
                            String id = "#" + o.getInt("id");
                            String cus = o.getString("customer");
                            String total = formatter.format(o.optDouble("total_amount", 0)) + " đ";
                            String status = o.getString("status");
                            recentOrdersModel.addRow(new Object[]{id, cus, total, status});
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Lỗi load API: " + e.getMessage());
            }
        }).start();
    }

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER); 
        contentPanel.revalidate(); 
        contentPanel.repaint();
    }
    
    private void exit(){
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.OK_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}