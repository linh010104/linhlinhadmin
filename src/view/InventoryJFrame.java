package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import khongphaigiaodien.InventoryApi;
import khongphaigiaodien.ApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class InventoryJFrame extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private boolean showLowStockOnly = false;

    // --- CÁC LABEL ĐỂ HIỂN THỊ SỐ LIỆU DASHBOARD ---
    private JLabel lblTotalVal, lblPhoneVal, lblLaptopVal, lblAccVal;
    private JLabel lblPhonePct, lblLaptopPct, lblAccPct;

    public InventoryJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ==========================================
        // 1. PHẦN HEADER (NÚT BẤM + DASHBOARD)
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnImport = new JButton("Nhập Hàng Vào Kho");
        styleButton(btnImport, new Color(0, 153, 76)); 
        btnImport.addActionListener(e -> showImportChoiceDialog());

        JButton btnRefresh = new JButton("Làm mới");
        styleButton(btnRefresh, Color.GRAY);
        btnRefresh.addActionListener(e -> loadData());

        buttonPanel.add(btnImport);
        buttonPanel.add(btnRefresh);
        headerPanel.add(buttonPanel, BorderLayout.NORTH);

        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        dashboardPanel.setBackground(Color.WHITE);

        lblTotalVal = createLabel("0 SP", 24, new Color(41, 128, 185));
        lblPhoneVal = createLabel("0 SP", 24, new Color(39, 174, 96));
        lblLaptopVal = createLabel("0 SP", 24, new Color(142, 68, 173));
        lblAccVal = createLabel("0 SP", 24, new Color(230, 126, 34));

        lblPhonePct = createLabel("Chiếm: 0%", 12, Color.DARK_GRAY);
        lblLaptopPct = createLabel("Chiếm: 0%", 12, Color.DARK_GRAY);
        lblAccPct = createLabel("Chiếm: 0%", 12, Color.DARK_GRAY);

        dashboardPanel.add(buildCard("Tổng Tồn Kho", lblTotalVal, createLabel("100%", 12, Color.DARK_GRAY), new Color(41, 128, 185)));
        dashboardPanel.add(buildCard("Điện Thoại", lblPhoneVal, lblPhonePct, new Color(39, 174, 96)));
        dashboardPanel.add(buildCard("Laptop", lblLaptopVal, lblLaptopPct, new Color(142, 68, 173)));
        dashboardPanel.add(buildCard("Phụ Kiện", lblAccVal, lblAccPct, new Color(230, 126, 34)));

        headerPanel.add(dashboardPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. PHẦN BẢNG DANH SÁCH (TABLE)
        // ==========================================
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{ "ID", "Tên Sản Phẩm", "Mã SKU", "Tồn Kho", "Cập nhật lần cuối" });

        table = new JTable(model);
        styleTable(table);

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                int qty = Integer.parseInt(value.toString());
                if(qty < 10) c.setForeground(new Color(220, 53, 69)); 
                else c.setForeground(new Color(0, 102, 204)); 
                return c;
            }
        });

        // 🔥 SỰ KIỆN CLICK ĐÚP: XEM CHI TIẾT TỒN KHO PHÂN LOẠI 🔥
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    int prodId = (int) table.getValueAt(row, 0);
                    String prodName = table.getValueAt(row, 1).toString();
                    showVariantDetailsDialog(prodId, prodName); // Bật form xem chi tiết màu sắc/mẫu mã
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    public InventoryJFrame(boolean showLowStockOnly) {
        this(); 
        this.showLowStockOnly = showLowStockOnly;
        if (showLowStockOnly) {
            loadData(); 
        }
    }

    // ==========================================
    // 3. TẢI DỮ LIỆU & TỰ ĐỘNG TÍNH TOÁN DASHBOARD
    // ==========================================
    private void loadData() {
        String json = InventoryApi.getAll();
        if (json == null) return;
        
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        
        int totalStock = 0, phoneStock = 0, laptopStock = 0, accStock = 0;
        
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            int qty = o.getInt("quantity");
            
            if (showLowStockOnly && qty >= 10) continue; 
            
            String date = o.optString("updated_at", "Chưa cập nhật");
            if(date.contains("T")) date = date.replace("T", " ").substring(0, 16);
            
            String name = o.getString("name");
            model.addRow(new Object[]{ o.getInt("id"), name, o.optString("sku", "---"), qty, date });
            
            totalStock += qty;
            String lowerName = name.toLowerCase();
            if (lowerName.contains("iphone") || lowerName.contains("samsung") || lowerName.contains("oppo") || lowerName.contains("điện thoại")) {
                phoneStock += qty;
            } else if (lowerName.contains("laptop") || lowerName.contains("macbook") || lowerName.contains("ipad")) {
                laptopStock += qty;
            } else {
                accStock += qty;
            }
        }
        
        lblTotalVal.setText(totalStock + " SP");
        lblPhoneVal.setText(phoneStock + " SP");
        lblLaptopVal.setText(laptopStock + " SP");
        lblAccVal.setText(accStock + " SP");
        
        if (totalStock > 0) {
            lblPhonePct.setText(String.format("Chiếm: %.1f%%", (double)phoneStock / totalStock * 100));
            lblLaptopPct.setText(String.format("Chiếm: %.1f%%", (double)laptopStock / totalStock * 100));
            lblAccPct.setText(String.format("Chiếm: %.1f%%", (double)accStock / totalStock * 100));
        } else {
            lblPhonePct.setText("Chiếm: 0%");
            lblLaptopPct.setText("Chiếm: 0%");
            lblAccPct.setText("Chiếm: 0%");
        }
    }

    // ==========================================
    // 4. HÀM XEM CHI TIẾT PHÂN LOẠI (MÀU SẮC, DUNG LƯỢNG...)
    // ==========================================
    private void showVariantDetailsDialog(int productId, String productName) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Chi tiết phân loại: " + productName, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        // Tiêu đề
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("<html><div style='text-align: center; color: #d70018;'><b>" + productName + "</b><br/>Tồn kho theo phân loại</div></html>");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        header.add(lblTitle);
        dialog.add(header, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] cols = {"Phân Bản / Màu Sắc", "Tồn Kho Hiện Tại"};
        DefaultTableModel varModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable varTable = new JTable(varModel);
        styleTable(varTable);

        // Gọi API lấy thông tin biến thể
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/products/" + productId))
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                if (json.has("variants") && !json.isNull("variants")) {
                    JSONArray variants = json.getJSONArray("variants");
                    for (int i = 0; i < variants.length(); i++) {
                        JSONObject v = variants.getJSONObject(i);
                        String vName = v.optString("variant_name", "Chưa phân loại");
                        int vStock = v.optInt("stock_quantity", 0);
                        varModel.addRow(new Object[]{vName, vStock + " cái"});
                    }
                }
                
                // Nếu sản phẩm không có phân loại nào
                if (varModel.getRowCount() == 0) {
                    varModel.addRow(new Object[]{"Mặc định (Sản phẩm nguyên bản)", json.optInt("stock_quantity", 0) + " cái"});
                }
            } else {
                varModel.addRow(new Object[]{"Không thể tải dữ liệu", "---"});
            }
        } catch (Exception ex) {
            varModel.addRow(new Object[]{"Lỗi kết nối máy chủ", "---"});
        }

        JScrollPane scroll = new JScrollPane(varTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        dialog.add(scroll, BorderLayout.CENTER);
        
        // Nút Đóng
        JPanel botPanel = new JPanel();
        botPanel.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");
        styleButton(btnClose, Color.GRAY);
        btnClose.addActionListener(e -> dialog.dispose());
        botPanel.add(btnClose);
        dialog.add(botPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH VÀ XỬ LÝ SỰ KIỆN
    // ==========================================
    private void showImportChoiceDialog() {
        Object[] options = {"Nhập Thủ Công", "Quét Hóa Đơn AI"};
        int choice = JOptionPane.showOptionDialog(this, "Bạn muốn nhập kho bằng phương thức nào?", "Chọn phương thức nhập kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if (choice == 0) showManualImportDialog();
        else if (choice == 1) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ScanInvoiceDialog dialog = new ScanInvoiceDialog(parentFrame);
            dialog.setVisible(true);
            loadData();
        }
    }

    // Lưu ý: Bây giờ hàm này chỉ được gọi qua nút "Nhập Hàng Vào Kho" -> "Nhập Thủ Công"
    private void showManualImportDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 sản phẩm trên bảng để nhập thêm!"); return; }

        String prodName = model.getValueAt(row, 1).toString();
        int prodId = (int) model.getValueAt(row, 0);
        String input = JOptionPane.showInputDialog(this, "Nhập số lượng nhập thêm kho cho:\n" + prodName, "Nhập kho thủ công", JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int amount = Integer.parseInt(input.trim());
                if (amount <= 0) { JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!"); return; }
                if (InventoryApi.importGoods(prodId, amount)) {
                    JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                    loadData(); 
                } else JOptionPane.showMessageDialog(this, "Lỗi khi nhập kho!");
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên hợp lệ!"); }
        }
    }

    private JPanel buildCard(String title, JLabel lblValue, JLabel lblPercent, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true), 
                BorderFactory.createEmptyBorder(10, 15, 10, 15) 
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.GRAY);
        
        lblPercent.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblPercent, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createLabel(String text, int size, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", size == 24 ? Font.BOLD : (size == 12 ? Font.ITALIC : Font.PLAIN), size));
        lbl.setForeground(color);
        return lbl;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(230, 230, 230)); table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50)); header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}