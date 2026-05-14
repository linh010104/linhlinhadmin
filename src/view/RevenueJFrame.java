/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
public class RevenueJFrame extends JPanel{
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

        // --- 1. HEADER (BỘ LỌC) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);

        String today = getTodayDate();

        topPanel.add(createInputPanel("Từ ngày:", txtFromDate = new JTextField(today, 10)));
        topPanel.add(createInputPanel("Đến ngày:", txtToDate = new JTextField(today, 10)));

        // Nút Xem Thống Kê
        JButton btnView = new JButton("Xem Thống Kê");
        styleButton(btnView, new Color(0, 102, 204));
        btnView.addActionListener(e -> loadData());
        topPanel.add(btnView);

        // NÚT LỌC NHANH 7 NGÀY
        JButton btn7Days = new JButton("7 Ngày Qua");
        styleButton(btn7Days, Color.GRAY);
        btn7Days.addActionListener(e -> {
            txtFromDate.setText(getPastDate(7));
            txtToDate.setText(getTodayDate());
            loadData();
        });
        topPanel.add(btn7Days);

        // NÚT LỌC NHANH 30 NGÀY
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

        // --- 2. BẢNG DỮ LIỆU (THEO SẢN PHẨM) ---
        model = new DefaultTableModel(new String[]{
            "ID", "Tên Sản Phẩm", "Mẫu mã/Thông số", "SL Bán", "Doanh Thu", "Lợi Nhuận", "Loại Hàng", "Thương Hiệu"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            // Định nghĩa kiểu dữ liệu để Sắp xếp (Sorting) chính xác
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // --- TÍNH NĂNG MỚI: TỰ ĐỘNG SẮP XẾP KHI CLICK VÀO TIÊU ĐỀ CỘT ---
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
                    .append(" (Bán ra: ").append(table.getValueAt(i, 3))
                    .append(", Lãi: ").append(table.getValueAt(i, 5)).append(")\n");
        }

        JDialog loadingDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Vui lòng đợi", true);
        loadingDialog.setLayout(new BorderLayout(10, 10));
        loadingDialog.setSize(350, 120);
        loadingDialog.setLocationRelativeTo(this);
        // KHÓA NÚT X (Buộc phải đợi AI chạy xong)
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 
        
        JPanel pnlLoad = new JPanel(new BorderLayout(15, 15));
        pnlLoad.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pnlLoad.setBackground(Color.WHITE);

        JLabel lblMsg = new JLabel("🤖 Giám đốc AI đang đọc báo cáo...", SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); 
        progressBar.setForeground(new Color(102, 0, 153)); // Màu tím tone-sur-tone với cái nút
        progressBar.setBackground(new Color(240, 240, 240));
        
        pnlLoad.add(lblMsg, BorderLayout.NORTH);
        pnlLoad.add(progressBar, BorderLayout.CENTER);
        loadingDialog.add(pnlLoad);

        // ==========================================
        // MỞ LUỒNG NGẦM GỌI API (BACKGROUND THREAD)
        // ==========================================
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://localhost:3000/api/ai/analyze-finance");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("totalRevenue", lblTotalMoney.getText());
                body.put("totalImportCost", lblTotalCost.getText());
                body.put("totalVatPaid", lblTotalVat.getText());
                body.put("profit", lblProfit.getText());
                body.put("soldData", dataToAi.toString());
                body.put("importData", "Dữ liệu nhập đã tính gộp trong Vốn."); 

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("utf-8"));
                }

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) res.append(line.trim());
                    
                    String rawAdvice = new JSONObject(res.toString()).getString("advice");
                    
                    // Dịch Markdown sang HTML
                    String htmlAdvice = rawAdvice
                            .replaceAll("\\*\\*(.*?)\\*\\*", "<b style='color:#00509E;'>$1</b>")
                            .replaceAll("\\* (.*?)\n", "<li style='margin-bottom:8px;'>$1</li>")
                            .replaceAll("\n", "<br>");

                    String finalHtml = "<html><body style='font-family:Segoe UI; font-size:14px; padding:15px; color:#333333;'>" 
                                     + htmlAdvice + "</body></html>";

                    // Gọi lại luồng chính để hiển thị UI
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose(); // BƯỚC QUAN TRỌNG: API CHẠY XONG TỰ TẮT FORM LOADING
                        
                        JEditorPane editorPane = new JEditorPane("text/html", finalHtml);
                        editorPane.setEditable(false);
                        editorPane.setBackground(new Color(245, 248, 250)); 
                        
                        JScrollPane pane = new JScrollPane(editorPane);
                        pane.setPreferredSize(new Dimension(700, 450));
                        pane.setBorder(BorderFactory.createEmptyBorder());
                        
                        JOptionPane.showMessageDialog(this, pane, "🤖 Giám Đốc AI Phân Tích Tài Chính", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception e) { 
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose(); // Lỗi cũng phải tắt form loading
                    JOptionPane.showMessageDialog(this, "Lỗi kết nối AI: " + e.getMessage());
                });
            }
        }).start();
        loadingDialog.setVisible(true); 
    }
    // --- HELPER METHODS ---
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