/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.CategoryDTO;
import khongphaigiaodien.CategoryApi;
import khongphaigiaodien.ProductApi;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

// --- CÁC THƯ VIỆN ĐỂ GỌI API ---
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScanInvoiceDialog extends JDialog {

    private JButton btnChooseFile;
    private JButton btnScanAI;
    private JButton btnSaveDB;
    private JLabel lblFilePath;
    private JTable tableItems;
    private DefaultTableModel tableModel;
    private File selectedFile;

    private String currentSupplier = "";
    private double currentTotalAmount = 0;
    private double currentTaxPercent = 10.0;
    private double currentTaxAmount = 0;

    public ScanInvoiceDialog(JFrame parent) {
        super(parent, "Nhập Kho Bằng Trí Tuệ Nhân Tạo (AI)", true);
        initUI();
        setSize(950, 600); // Mở rộng form ra một chút cho đẹp
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // 1. PHẦN TRÊN CÙNG
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnChooseFile = new JButton("1. Chọn Ảnh Hóa Đơn");
        lblFilePath = new JLabel("Chưa chọn file nào...");
        lblFilePath.setForeground(Color.BLUE);
        btnScanAI = new JButton("2. Bắt Đầu Quét AI");
        btnScanAI.setEnabled(false);

        panelTop.add(btnChooseFile);
        panelTop.add(btnScanAI);
        panelTop.add(lblFilePath);
        add(panelTop, BorderLayout.NORTH);

        // 2. PHẦN GIỮA: BẢNG (THÊM CỘT TRẠNG THÁI)
        String[] columnNames = {"STT", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá Nhập", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableItems = new JTable(tableModel);
        tableItems.setRowHeight(30);
        tableItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tô màu cột Trạng thái cho sinh động
        tableItems.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if (status.contains("Hàng Mới")) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("Đã có")) {
                    c.setForeground(new Color(0, 153, 76)); // Xanh lá
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // BẮT SỰ KIỆN DOUBLE-CLICK VÀO HÀNG MỚI ĐỂ TẠO SẢN PHẨM
        tableItems.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int row = tableItems.getSelectedRow();
                    String status = tableItems.getValueAt(row, 4).toString();
                    
                    if (status.contains("Hàng Mới")) {
                        String name = tableItems.getValueAt(row, 1).toString();
                        String qty = tableItems.getValueAt(row, 2).toString();
                        String price = tableItems.getValueAt(row, 3).toString().replace(",", "");
                        
                        // Gọi form tạo nhanh
                        showQuickAddDialog(name, qty, price, row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableItems);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả bóc tách từ AI (Nhấp đúp vào Hàng Mới để thêm vào DB)")); 
        add(scrollPane, BorderLayout.CENTER);

        // 3. PHẦN DƯỚI CÙNG
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnSaveDB = new JButton("3. Lưu Phiếu Nhập Vào Database");
        btnSaveDB.setBackground(new Color(46, 204, 113));
        btnSaveDB.setForeground(Color.WHITE);
        panelBottom.add(btnSaveDB);
        add(panelBottom, BorderLayout.SOUTH);

        // --- CÁC SỰ KIỆN CLICKS ---
        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);
            if (fileChooser.showOpenDialog(ScanInvoiceDialog.this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                lblFilePath.setText("Đã chọn file: " + selectedFile.getName());
                btnScanAI.setEnabled(true);
            }
        });

        btnScanAI.addActionListener(e -> scanInvoiceWithAI());
        btnSaveDB.addActionListener(e -> saveToDatabase());
    }

    // --- LOGIC GỌI AI VÀ ĐỐI CHIẾU SẢN PHẨM CŨ/MỚI ---
    private void scanInvoiceWithAI() {
        if (selectedFile == null) return;
        JOptionPane.showMessageDialog(this, "Đang gửi ảnh sang AI...\nVui lòng đợi vài giây (tùy thuộc vào mạng)...");
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 1. GỌI API LẤY TOÀN BỘ SẢN PHẨM TRONG DB ĐỂ ĐỐI CHIẾU
                    String allProductsJson = ProductApi.getAllProducts();
                    List<String> existingNames = new ArrayList<>();
                    if (allProductsJson != null && allProductsJson.length() > 5) {
                        JSONArray allProds = new JSONArray(allProductsJson);
                        for (int i = 0; i < allProds.length(); i++) {
                            existingNames.add(allProds.getJSONObject(i).getString("name").toLowerCase().trim());
                        }
                    }

                    // 2. GỬI ẢNH CHO AI
                    HttpClient client = HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(60)) // Đảm bảo Client không bỏ cuộc sớm
                            .build();
                            
                    String boundary = "---JavaHttpClientBoundary" + System.currentTimeMillis();
                    byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                    
                    String head = "--" + boundary + "\r\n" +
                                "Content-Disposition: form-data; name=\"invoice_image\"; filename=\"" + selectedFile.getName() + "\"\r\n" +
                                "Content-Type: " + Files.probeContentType(selectedFile.toPath()) + "\r\n\r\n";
                    String tail = "\r\n--" + boundary + "--\r\n";
                    
                    byte[] headBytes = head.getBytes(StandardCharsets.UTF_8);
                    byte[] tailBytes = tail.getBytes(StandardCharsets.UTF_8);
                    byte[] body = new byte[headBytes.length + fileBytes.length + tailBytes.length];
                    System.arraycopy(headBytes, 0, body, 0, headBytes.length);
                    System.arraycopy(fileBytes, 0, body, headBytes.length, fileBytes.length);
                    System.arraycopy(tailBytes, 0, body, headBytes.length + fileBytes.length, tailBytes.length);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(khongphaigiaodien.ApiConfig.BASE_URL + "/ai/scan-invoice")) // Dùng link động
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                            .timeout(java.time.Duration.ofSeconds(60)) // Ép Java kiên nhẫn chờ 60s
                            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    JSONObject jsonRes = new JSONObject(response.body());
                    
                    if (jsonRes.getBoolean("success")) {
                        JSONObject aiData = jsonRes.getJSONObject("data");
                        JSONArray items = aiData.getJSONArray("items");
                        
                        SwingUtilities.invokeLater(() -> {
                            tableModel.setRowCount(0); 
                            double tongTienHangChuaThue = 0;
                            currentSupplier = aiData.optString("supplier_name", "Nhà cung cấp chưa rõ");
                            
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String tenSP = item.getString("product_name");
                                int soLuong = item.getInt("quantity");
                                long donGia = item.getLong("import_price");
                                
                                tongTienHangChuaThue += (soLuong * donGia);
                                
                                // LOGIC THẦN THÁNH: KIỂM TRA HÀNG MỚI HAY CŨ
                                String status = "⚠️ Hàng Mới (Click để thêm)";
                                if (existingNames.contains(tenSP.toLowerCase().trim())) {
                                    status = "✅ Đã có trong DB";
                                }
                                
                                tableModel.addRow(new Object[]{ i + 1, tenSP, soLuong, String.format("%,d", donGia), status });
                            }
                            
                            currentTaxAmount = tongTienHangChuaThue * (currentTaxPercent / 100.0);
                            currentTotalAmount = tongTienHangChuaThue + currentTaxAmount;
                            
                            String baoCao = String.format("<html>Nhà cung cấp: <b>%s</b> &nbsp;|&nbsp; TỔNG THANH TOÁN: %,.0f VNĐ</html>", 
                                    currentSupplier, currentTotalAmount);
                            lblFilePath.setText(baoCao);
                            
                            JOptionPane.showMessageDialog(ScanInvoiceDialog.this, "🎉 Quét AI xong! Hãy chú ý các hàng màu đỏ nhé.");
                        });
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ScanInvoiceDialog.this, "Lỗi Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        }.execute();
    }

    // --- FORM THÊM NHANH SẢN PHẨM TỪ AI ---
    private void showQuickAddDialog(String prefillName, String prefillQty, String prefillPrice, int rowIndex) {
        JTextField txtName = new JTextField(prefillName);
        JTextField txtSku = new JTextField("SKU-AI-" + System.currentTimeMillis() % 10000); // Tạo đại mã SKU
        JTextField txtPrice = new JTextField(prefillPrice); // Giá bán tạm lấy bằng giá nhập
        JTextField txtImportPrice = new JTextField(prefillPrice);
        JTextField txtStock = new JTextField(prefillQty);
        JTextField txtWarranty = new JTextField("12");
        JTextField txtSpecs = new JTextField();
        JTextField txtDesc = new JTextField("Nhập tự động từ AI");
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        for (CategoryDTO c : CategoryApi.getAllCategories()) cbCategory.addItem(c);
        
        Object[] form = { 
            "Tên SP (AI đọc):", txtName, 
            "Mã SKU:", txtSku, 
            "Giá nhập (AI đọc):", txtImportPrice, 
            "Giá bán dự kiến:", txtPrice, 
            "Số lượng nhập (AI đọc):", txtStock,
            "Danh mục:", cbCategory,
            "Bảo hành (tháng):", txtWarranty,
            "Thông số:", txtSpecs
        };
        
        int result = JOptionPane.showConfirmDialog(this, form, "Tạo nhanh sản phẩm mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            boolean ok = ProductApi.createProduct(
                txtName.getText(), txtSku.getText(), 
                Double.parseDouble(txtPrice.getText()), Double.parseDouble(txtImportPrice.getText()), Integer.parseInt(txtStock.getText()), 
                Integer.parseInt(txtWarranty.getText()), cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm thành công!");
                // Cập nhật lại trạng thái trên JTable
                tableModel.setValueAt(txtName.getText(), rowIndex, 1); // Cập nhật tên lỡ người dùng có sửa
                tableModel.setValueAt("✅ Đã có trong DB", rowIndex, 4);
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại, vui lòng kiểm tra lại!");
            }
        }
    }

    // --- LOGIC LƯU PHIẾU NHẬP ---
    private void saveToDatabase() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Bảng trống!");
            return;
        }
        
        // KIỂM TRA XEM CÒN HÀNG MỚI CHƯA ĐƯỢC TẠO KHÔNG
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 4).toString().contains("Hàng Mới")) {
                JOptionPane.showMessageDialog(this, "Cảnh báo: Dòng số " + (i+1) + " là hàng mới.\nVui lòng nhấp đúp vào dòng đó để tạo sản phẩm trước khi lưu Phiếu nhập!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Đang gửi dữ liệu sang Node.js...");
        try {
            JSONObject dataToSave = new JSONObject();
            dataToSave.put("supplier_name", currentSupplier);
            dataToSave.put("total_amount", currentTotalAmount);
            dataToSave.put("tax_percent", currentTaxPercent);
            dataToSave.put("total_tax_amount", currentTaxAmount);

            JSONArray itemsArray = new JSONArray();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                JSONObject item = new JSONObject();
                item.put("product_name", tableModel.getValueAt(i, 1).toString());
                item.put("quantity", Integer.parseInt(tableModel.getValueAt(i, 2).toString()));
                item.put("import_price", Double.parseDouble(tableModel.getValueAt(i, 3).toString().replace(",", "")));
                itemsArray.put(item);
            }
            dataToSave.put("items", itemsArray);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(15)) // Chờ kết nối 15s
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(khongphaigiaodien.ApiConfig.BASE_URL + "/import/create")) // Dùng link động
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(30)) // Lưu DB chờ 30s
                    .POST(HttpRequest.BodyPublishers.ofString(dataToSave.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonRes = new JSONObject(response.body());
            
            if (jsonRes.getBoolean("success")) {
                JOptionPane.showMessageDialog(this, "✅ " + jsonRes.getString("message"));
                this.dispose(); // Đóng form sau khi lưu thành công
            } else {
                JOptionPane.showMessageDialog(this, "❌ Lỗi: " + jsonRes.getString("message"));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối Server", "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
        }
    }
}