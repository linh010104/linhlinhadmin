/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import khongphaigiaodien.StatsApi;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author AlinV
 */
public class RevenueJFrame extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    
    private JSONArray currentInvoices = new JSONArray(); 

    private JLabel lblTotalMoney;
    private JLabel lblTotalCost;
    private JLabel lblTotalVat;
    private JLabel lblProfit;
    private JLabel lblTotalOrders;

    public RevenueJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);

        String today = getTodayDate();

        topPanel.add(createInputPanel("Từ ngày:", txtFromDate = new JTextField(today, 10)));
        topPanel.add(createInputPanel("Đến ngày:", txtToDate = new JTextField(today, 10)));

        JButton btnView = new JButton("Xem Thống Kê");
        styleButton(btnView, new Color(0, 102, 204));
        btnView.addActionListener(e -> loadData());
        topPanel.add(btnView);

        JButton btn7Days = new JButton("7 Ngày Qua");
        styleButton(btn7Days, Color.GRAY);
        btn7Days.addActionListener(e -> {
            txtFromDate.setText(getPastDate(7));
            txtToDate.setText(getTodayDate());
            loadData();
        });
        topPanel.add(btn7Days);

        JButton btn30Days = new JButton("30 Ngày Qua");
        styleButton(btn30Days, Color.GRAY);
        btn30Days.addActionListener(e -> {
            txtFromDate.setText(getPastDate(30));
            txtToDate.setText(getTodayDate());
            loadData();
        });
        topPanel.add(btn30Days);

        // Nút Xuất Excel
        JButton btnExport = new JButton("Xuất Excel");
        styleButton(btnExport, new Color(0, 153, 76));
        btnExport.addActionListener(e -> exportToExcel());
        topPanel.add(btnExport);

        // Nút AI
        JButton btnAiAdvise = new JButton("AI Tư Vấn");
        styleButton(btnAiAdvise, new Color(102, 0, 153));
        btnAiAdvise.addActionListener(e -> getAiFinancialAdvice());
        topPanel.add(btnAiAdvise);

        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
            "ID", "Tên Sản Phẩm", "Mẫu mã/Thông số", "SL Bán", "Doanh Thu", "Lợi Nhuận", "Loại Hàng", "Thương Hiệu"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        table.setAutoCreateRowSorter(true);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(1).setPreferredWidth(200);
        cm.getColumn(2).setPreferredWidth(200);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER (TỔNG KẾT TÀI CHÍNH) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(250, 250, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        lblTotalOrders = new JLabel("Số mặt hàng đã bán: 0");
        lblTotalOrders.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottomPanel.add(lblTotalOrders, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        rightPanel.setOpaque(false);

        rightPanel.add(new JLabel("Tổng Doanh Thu:", SwingConstants.RIGHT));
        lblTotalMoney = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTotalMoney.setForeground(new Color(0, 102, 204));
        rightPanel.add(lblTotalMoney);

        rightPanel.add(new JLabel("Tổng Vốn Nhập (AI):", SwingConstants.RIGHT));
        lblTotalCost = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTotalCost.setForeground(new Color(220, 53, 69));
        rightPanel.add(lblTotalCost);

        rightPanel.add(new JLabel("Thuế VAT Đầu Vào:", SwingConstants.RIGHT));
        lblTotalVat = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTotalVat.setForeground(new Color(255, 140, 0));
        rightPanel.add(lblTotalVat);

        JLabel lblTitleProfit = new JLabel("LỢI NHUẬN RÒNG:", SwingConstants.RIGHT);
        lblTitleProfit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblProfit = new JLabel("0 đ", SwingConstants.RIGHT);
        lblProfit.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblProfit.setForeground(new Color(0, 153, 76));
        rightPanel.add(lblTitleProfit); rightPanel.add(lblProfit);

        bottomPanel.add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        String start = txtFromDate.getText().trim();
        String end = txtToDate.getText().trim();
        if (start.isEmpty() || end.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ ngày!"); return; }

        JSONObject data = StatsApi.getRevenue(start, end);
        if (data == null) return;

        model.setRowCount(0);
        currentInvoices = data.optJSONArray("invoices");

        JSONObject summary = data.getJSONObject("summary");
        lblTotalMoney.setText(String.format("%,.0f đ", summary.getDouble("totalRevenue")));
        lblTotalCost.setText(String.format("- %,.0f đ", summary.getDouble("totalImportCost")));
        lblTotalVat.setText(String.format("- %,.0f đ", summary.optDouble("totalVatPaid", 0)));
        lblProfit.setText(String.format("%,.0f đ", summary.optDouble("totalProfit", 0)));
        lblTotalOrders.setText("Số mặt hàng đã bán: " + summary.getInt("totalProducts"));

        JSONArray productStats = data.getJSONArray("product_stats");
        for (int i = 0; i < productStats.length(); i++) {
            JSONObject p = productStats.getJSONObject(i);
            model.addRow(new Object[]{
                p.getInt("id"),
                p.getString("name"),
                p.optString("specifications", "---"),
                p.getInt("sold_quantity"),
                String.format("%,.0f đ", p.getDouble("total_revenue")),
                String.format("%,.0f đ", p.getDouble("total_profit")),
                p.optString("category_name", "N/A"),
                p.optString("brand_name", "N/A")
            });
        }
    }

    private void exportToExcel() {
        if (table.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!"); return; }

        JFileChooser fileChooser = new JFileChooser();
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm").format(new Date());
        fileChooser.setSelectedFile(new File("BaoCao_LinhStore_" + timeStamp + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("\uFEFF"); // Support UTF-8 tiếng Việt

                bw.write("--- 1. THỐNG KÊ DOANH THU THEO SẢN PHẨM ---"); bw.newLine();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    bw.write(table.getColumnName(i) + (i == table.getColumnCount()-1 ? "" : ","));
                }
                bw.newLine();

                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        String val = table.getValueAt(i, j).toString().replace(",", "");
                        bw.write(val + (j == table.getColumnCount()-1 ? "" : ","));
                    }
                    bw.newLine();
                }
                
                if (currentInvoices != null && currentInvoices.length() > 0) {
                    bw.newLine(); bw.write("--- 2. CHI TIẾT NHẬP HÀNG (DỮ LIỆU AI - MONGODB) ---"); bw.newLine();
                    bw.write("Nhà Cung Cấp,Ngày Nhập,Thuế VAT,Tổng Thanh Toán"); bw.newLine();
                    for (int i = 0; i < currentInvoices.length(); i++) {
                        JSONObject inv = currentInvoices.getJSONObject(i);
                        bw.write(inv.optString("nha_cung_cap") + "," + inv.optString("ngay_nhap").substring(0,10) + "," + 
                                 inv.optDouble("tien_thue_vat") + "," + inv.optDouble("tong_thanh_toan"));
                        bw.newLine();
                    }
                }

                bw.newLine(); bw.write("--- 3. TỔNG KẾT TÀI CHÍNH ---"); bw.newLine();
                bw.write("TỔNG DOANH THU:," + lblTotalMoney.getText().replace(",", "")); bw.newLine();
                bw.write("LỢI NHUẬN RÒNG:," + lblProfit.getText().replace(",", ""));

                JOptionPane.showMessageDialog(this, "Xuất báo cáo CSV thành công!");
                Desktop.getDesktop().open(file);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void getAiFinancialAdvice() {
        String revStr = lblTotalMoney.getText().replaceAll("[^0-9]", "");
        if (revStr.isEmpty() || revStr.equals("0")) {
            JOptionPane.showMessageDialog(this, "Hãy nhấn 'Xem Thống Kê' trước khi nhờ AI tư vấn!");
            return;
        }

        // Bơm chi tiết bán hàng
        StringBuilder dataToAi = new StringBuilder();
        for(int i = 0; i < table.getRowCount(); i++) {
            dataToAi.append("- ").append(table.getValueAt(i, 1))
                    .append(" (Bán: ").append(table.getValueAt(i, 3))
                    .append(", Lãi: ").append(table.getValueAt(i, 5)).append(")\n");
        }

        double dRev = Double.parseDouble(revStr);
        double dCost = Double.parseDouble(lblTotalCost.getText().replaceAll("[^0-9]", ""));
        double dVat = Double.parseDouble(lblTotalVat.getText().replaceAll("[^0-9]", ""));
        double dProfit = dRev - dCost - dVat;

        // 1. TẠO DASHBOARD CHÍNH
        JDialog dashboardDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "📊 Dashboard AI Phân Tích Chiến Lược", true);
        dashboardDialog.setSize(950, 480);
        dashboardDialog.setLocationRelativeTo(this);
        dashboardDialog.setLayout(new BorderLayout());

        // --- BÊN TRÁI: BIỂU ĐỒ TRÒN TỰ VẼ (BẢN PRO MAX) ---
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double profitObj = Math.max(0, dProfit);
                double costObj = dCost + dVat;
                double total = profitObj + costObj;
                if (total == 0) return;

                int profitAngle = (int) Math.round((profitObj / total) * 360);
                int costAngle = 360 - profitAngle;

                int x = 60, y = 30, size = 220;
                int holeSize = 130; 
                int offset = (size - holeSize) / 2;

                Color colorProfit = new Color(16, 185, 129); 
                Color colorCost = new Color(239, 68, 68);    
                Color colorBg = new Color(248, 249, 250);    

                g2d.setColor(colorProfit);
                g2d.fillArc(x, y, size, size, 90, profitAngle);

                g2d.setColor(colorCost);
                g2d.fillArc(x, y, size, size, 90 + profitAngle, costAngle);

                g2d.setColor(colorBg);
                g2d.fillOval(x + offset, y + offset, holeSize, holeSize);

                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String titleCenter = "Tổng Dòng Tiền";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(titleCenter, x + size/2 - fm.stringWidth(titleCenter)/2, y + size/2 - 10);

                g2d.setColor(new Color(0, 80, 160));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String moneyCenter = String.format("%,.0fđ", total);
                fm = g2d.getFontMetrics();
                g2d.drawString(moneyCenter, x + size/2 - fm.stringWidth(moneyCenter)/2, y + size/2 + 15);

                double profitPct = (profitObj / total) * 100;
                double costPct = 100 - profitPct;

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                int legY = 290;

                g2d.setColor(colorProfit);
                g2d.fillRoundRect(50, legY, 16, 16, 4, 4);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.format("Lợi nhuận: %.1f%%", profitPct), 75, legY + 13);

                g2d.setColor(colorCost);
                g2d.fillRoundRect(50, legY + 30, 16, 16, 4, 4);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.format("Vốn & VAT: %.1f%%", costPct), 75, legY + 43);
            }
        };
        chartPanel.setPreferredSize(new Dimension(350, 480));
        chartPanel.setBackground(new Color(248, 249, 250));
        dashboardDialog.add(chartPanel, BorderLayout.WEST);

        // --- BÊN PHẢI: KHUNG CHỨA TEXT AI ---
        String loadingHtml = "<html><body style='font-family:Segoe UI; padding:60px 20px; text-align:center;'>"
                + "<img src='https://cdnjs.cloudflare.com/ajax/libs/lightbox2/2.11.3/images/loading.gif' width='50' height='50'>"
                + "<h3 style='color:#00509E; margin-top: 20px;'>🤖 Giám đốc AI đang tính toán...</h3>"
                + "<p style='color:#666; font-size:12px;'>Việc phân tích dữ liệu lớn có thể mất từ 5 - 10 giây.<br>Vui lòng không đóng cửa sổ này!</p>"
                + "</body></html>";

        JEditorPane aiTextPane = new JEditorPane("text/html", loadingHtml);
        aiTextPane.setEditable(false);
        aiTextPane.setBackground(Color.WHITE);
        dashboardDialog.add(new JScrollPane(aiTextPane), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng Báo Cáo");
        btnClose.setBackground(new Color(220, 53, 69));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.addActionListener(e -> dashboardDialog.dispose());
        JPanel pnlBottom = new JPanel();
        pnlBottom.add(btnClose);
        dashboardDialog.add(pnlBottom, BorderLayout.SOUTH);

        // 2. GỌI API CHẠY NGẦM
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(khongphaigiaodien.ApiConfig.BASE_URL + "/ai/analyze-finance");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                
                // FIX: SẠCH HÓA DỮ LIỆU ĐẦU VÀO ĐỂ AI KHÔNG BỊ "NGÁO"
                String cleanRevenue = lblTotalMoney.getText().replaceAll("[^0-9]", "");
                String cleanCost = lblTotalCost.getText().replaceAll("[^0-9]", "");
                String cleanVat = lblTotalVat.getText().replaceAll("[^0-9]", "");
                String cleanProfit = lblProfit.getText().replaceAll("[^-0-9]", ""); // Giữ lại dấu trừ cho lợi nhuận nếu lỗ

                body.put("totalRevenue", cleanRevenue);
                body.put("totalImportCost", cleanCost);
                body.put("totalVatPaid", cleanVat);
                body.put("profit", cleanProfit);
                body.put("soldData", dataToAi.toString());

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("utf-8"));
                }

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) res.append(line.trim());
                    
                    String rawAdvice = new JSONObject(res.toString()).getString("advice");
                    
                    // LỘT XÁC GIAO DIỆN HIỂN THỊ THÀNH CARD UI
                    String finalHtml = "";
                    try {
                        // Cố gắng parse theo 3 phần để render giao diện Card xịn xò
                        if (rawAdvice.contains("- Phần 2") && rawAdvice.contains("- Phần 3")) {
                            finalHtml = "<html><body style='font-family:Segoe UI, sans-serif; font-size:13px; color:#333; margin: 0; padding: 10px;'>"
                                    + "<h2 style='color:#00509E; text-align:center; margin-top:0;'>💡 CHIẾN LƯỢC KINH DOANH TỪ AI</h2>"
                                    + "<table width='100%' cellpadding='8' cellspacing='0' border='0' style='margin-bottom:10px;'>"
                                    + "<tr><td style='background-color:#E8F4F8; border-left: 4px solid #00509E;'>"
                                    + "<b style='color:#00509E; font-size:14px;'>1. HIỆU QUẢ BÁN HÀNG</b><br>"
                                    + rawAdvice.split("- Phần 2")[0].replace("- Phần 1", "").replace(":", "").trim()
                                    + "</td></tr></table>"
                                    
                                    + "<table width='100%' cellpadding='8' cellspacing='0' border='0' style='margin-bottom:10px;'>"
                                    + "<tr><td style='background-color:#FFF3CD; border-left: 4px solid #FFC107;'>"
                                    + "<b style='color:#856404; font-size:14px;'>2. CHIẾN LƯỢC ĐỊNH GIÁ & TỒN KHO</b><br>"
                                    + rawAdvice.split("- Phần 3")[0].split("- Phần 2")[1].replace(":", "").trim()
                                    + "</td></tr></table>"
                                    
                                    + "<table width='100%' cellpadding='8' cellspacing='0' border='0'>"
                                    + "<tr><td style='background-color:#F8D7DA; border-left: 4px solid #DC3545;'>"
                                    + "<b style='color:#721C24; font-size:14px;'>3. ĐỀ XUẤT HÀNH ĐỘNG TỨC THỜI</b><br>"
                                    + rawAdvice.split("- Phần 3")[1].replace(":", "").trim()
                                    + "</td></tr></table>"
                                    + "</body></html>";
                        } else {
                            // Fallback nếu AI trả lời tự do
                            finalHtml = "<html><body style='font-family:Segoe UI; font-size:14px; padding:20px; color:#333;'>"
                                     + "<h2 style='color:#00509E; border-bottom: 2px solid #ccc; padding-bottom: 5px;'>💡 CHIẾN LƯỢC KINH DOANH TỪ AI:</h2>"
                                     + rawAdvice
                                     + "</body></html>";
                        }
                    } catch (Exception parseEx) {
                        // Fallback an toàn tuyệt đối
                        finalHtml = "<html><body style='padding:20px; font-family:Segoe UI;'>" + rawAdvice + "</body></html>";
                    }

                    final String renderHtml = finalHtml;
                    SwingUtilities.invokeLater(() -> aiTextPane.setText(renderHtml));
                }
            } catch (Exception e) { 
                SwingUtilities.invokeLater(() -> aiTextPane.setText("<html><body style='padding:20px;'><h3 style='color:red;'>Lỗi kết nối AI: " + e.getMessage() + "</h3></body></html>"));
            }
        }).start();

        dashboardDialog.setVisible(true); 
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private String getPastDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    private JPanel createInputPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}