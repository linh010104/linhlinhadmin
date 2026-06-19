package view;

import api.CategoryDTO;
import khongphaigiaodien.CategoryApi;
import khongphaigiaodien.ProductApi;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScanInvoiceDialog extends JDialog {

    private JButton btnChooseFile, btnScanAI, btnSaveDB;
    private JLabel lblFilePath;
    private JTable tableItems;
    private DefaultTableModel tableModel;
    private File selectedFile;

    private String currentSupplier = "";
    private double currentTotalAmount = 0;
    private double currentTaxPercent = 10.0;
    private double currentTaxAmount = 0;
    
    // Bản đồ lưu ID của Sản phẩm gốc để lát truyền vào Form Mẫu mã
    private Map<String, Integer> productMap = new HashMap<>();

    public ScanInvoiceDialog(JFrame parent) {
        super(parent, "Nhập Kho Bằng Trí Tuệ Nhân Tạo (AI)", true);
        initUI();
        setSize(1050, 700);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

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

        String[] columnNames = {"STT", "Tên SP Gốc", "Mẫu Mã (Variant)", "Số Lượng", "Đơn Giá Nhập", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableItems = new JTable(tableModel);
        tableItems.setRowHeight(30);
        tableItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        tableItems.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if (status.contains("SP Mới")) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("Tạo Mẫu Mã")) {
                    c.setForeground(new Color(255, 140, 0)); // Màu cam cho bắt mắt
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("Đã có")) {
                    c.setForeground(new Color(0, 153, 76));
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // 🎯 XỬ LÝ CLICK ĐÚP: PHÂN LUỒNG MỞ FORM GỐC HAY MỞ FORM MẪU MÃ
        tableItems.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int row = tableItems.getSelectedRow();
                    String status = tableItems.getValueAt(row, 5).toString();
                    String tenSp = tableItems.getValueAt(row, 1).toString();
                    String mauMa = tableItems.getValueAt(row, 2).toString();
                    
                    if (status.contains("SP Mới")) {
                        String giaNhap = tableItems.getValueAt(row, 4).toString().replace(",", "");
                        showQuickAddDialog(tenSp, giaNhap, row);
                    } 
                    else if (status.contains("Tạo Mẫu Mã")) {
                        // Lấy ID Sản phẩm gốc từ Map để gán mẫu mã vào
                        Integer prodId = productMap.get(tenSp.toLowerCase().trim());
                        if (prodId != null) {
                            showVariantAddDialog(prodId, tenSp, mauMa, row);
                        } else {
                            JOptionPane.showMessageDialog(ScanInvoiceDialog.this, "Không tìm thấy ID Sản phẩm gốc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableItems);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả bóc tách từ AI")); 
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnSaveDB = new JButton("3. Lưu Phiếu Nhập Vào Database");
        btnSaveDB.setBackground(new Color(46, 204, 113));
        btnSaveDB.setForeground(Color.WHITE);
        panelBottom.add(btnSaveDB);
        add(panelBottom, BorderLayout.SOUTH);

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

    // 🎯 FORM THÊM NHANH SẢN PHẨM GỐC
    private void showQuickAddDialog(String defaultName, String defaultImportPrice, int rowIndex) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "✨ Khởi Tạo Nhanh Sản Phẩm Gốc", true);
        dialog.setSize(850, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel topForm = new JPanel(new GridLayout(0, 2, 10, 15)); 
        JTextField txtName = new JTextField(defaultName); 
        JTextField txtSku = new JTextField("SKU-AI-" + (System.currentTimeMillis() % 10000)); 
        JTextField txtPrice = new JTextField(defaultImportPrice); 
        JTextField txtImportPrice = new JTextField(defaultImportPrice); 
        JTextField txtStock = new JTextField("0"); 
        txtStock.setEditable(false); 
        JTextField txtWarranty = new JTextField("12");
        
        JTextField[] textFields = {txtName, txtSku, txtPrice, txtImportPrice, txtStock, txtWarranty};
        for (JTextField tf : textFields) {
            tf.setPreferredSize(new Dimension(tf.getWidth(), 35));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new Dimension(cbCategory.getWidth(), 35));
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        List<CategoryDTO> listCat = CategoryApi.getAllCategories();
        if(listCat != null) {
            for (CategoryDTO c : listCat) cbCategory.addItem(c);
        }

        topForm.add(new JLabel("Tên sản phẩm gốc * :")); topForm.add(txtName);
        topForm.add(new JLabel("Mã SKU:")); topForm.add(txtSku);
        topForm.add(new JLabel("Giá bán dự kiến (VNĐ):")); topForm.add(txtPrice);
        topForm.add(new JLabel("Giá nhập (VNĐ):")); topForm.add(txtImportPrice);
        topForm.add(new JLabel("Tồn kho (Tự cộng dồn sau):")); topForm.add(txtStock);
        topForm.add(new JLabel("Bảo hành (Tháng):")); topForm.add(txtWarranty);
        topForm.add(new JLabel("Danh mục:")); topForm.add(cbCategory);

        JPanel bottomForm = new JPanel();
        bottomForm.setLayout(new BoxLayout(bottomForm, BoxLayout.Y_AXIS));
        bottomForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblSpecs = new JLabel("Thông số kỹ thuật:");
        lblSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea txtSpecs = new JTextArea();
        txtSpecs.setLineWrap(true); txtSpecs.setWrapStyleWord(true);
        txtSpecs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollSpecs = new JScrollPane(txtSpecs);
        scrollSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollSpecs.setPreferredSize(new Dimension(400, 100));

        JLabel lblDesc = new JLabel("Mô tả / Ghi chú:");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JTextArea txtDesc = new JTextArea();
        txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDesc.setPreferredSize(new Dimension(400, 100));

        bottomForm.add(lblSpecs); bottomForm.add(Box.createVerticalStrut(5)); bottomForm.add(scrollSpecs);
        bottomForm.add(lblDesc); bottomForm.add(Box.createVerticalStrut(5)); bottomForm.add(scrollDesc);

        JPanel mainFormPanel = new JPanel(new BorderLayout());
        mainFormPanel.add(topForm, BorderLayout.NORTH);
        mainFormPanel.add(bottomForm, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("❌ Hủy Bỏ");
        btnCancel.setBackground(new Color(108, 117, 125)); btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> dialog.dispose()); 
        
        JButton btnSave = new JButton("💾 Khởi Tạo Sản Phẩm Gốc");
        btnSave.setBackground(new Color(0, 153, 76)); btnSave.setForeground(Color.WHITE);
        
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
                int catId = cat != null ? cat.getId() : 1;
                
                boolean ok = ProductApi.createProduct(
                    txtName.getText(), txtSku.getText(), Double.parseDouble(txtPrice.getText()), 
                    Double.parseDouble(txtImportPrice.getText()), 0, 
                    Integer.parseInt(txtWarranty.getText()), catId, txtDesc.getText(), txtSpecs.getText(), 1, 1
                );
                
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "✅ Đã tạo SP Gốc thành công!\nHãy làm mới bảng hoặc tiếp tục tạo mẫu mã nếu cần.");
                    // Lấy lại danh sách ID mới để gán vào Map
                    refreshProductMap();
                    tableModel.setValueAt(txtName.getText(), rowIndex, 1);
                    
                    // Nếu dòng này có chứa mẫu mã -> Nhảy sang báo Cam yêu cầu xác nhận mẫu mã
                    String mauMa = tableItems.getValueAt(rowIndex, 2).toString();
                    if(!mauMa.isEmpty()){
                        tableModel.setValueAt("⚡ Click đúp Tạo Mẫu Mã", rowIndex, 5);
                    } else {
                        tableModel.setValueAt("✅ Đã có (Sẵn sàng nhập)", rowIndex, 5);
                    }
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "❌ Lỗi khi thêm sản phẩm vào Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đúng số!", "Lỗi nhập", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel); btnPanel.add(btnSave);
        panel.add(new JScrollPane(mainFormPanel), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // 🎯 FORM THÊM NHANH MẪU MÃ (VARIANT) MỚI TỪ AI
    private void showVariantAddDialog(int productId, String productName, String variantName, int rowIndex) {
        JComboBox<String> cbGroup = new JComboBox<>(new String[]{"Cấu hình", "Màu sắc", "Phân loại", "Loại Switch", "Kích thước"});
        cbGroup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTextField txtVariantName = new JTextField(variantName);
        txtVariantName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTextField txtPrice = new JTextField("0"); // Mặc định không cộng thêm tiền
        txtPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        Object[] form = {
            "<html><b>Sản phẩm:</b> " + productName + "</html>",
            " ",
            "Nhóm phân loại:", cbGroup,
            "Tên Mẫu mã / Phiên bản:", txtVariantName,
            "Giá cộng thêm khi bán ra (VNĐ):", txtPrice,
            "<html><i>(Tồn kho sẽ được tự động cộng dồn sau khi Lưu Hóa Đơn)</i></html>"
        };
        
        int result = JOptionPane.showConfirmDialog(this, form, "📦 Xác Nhận & Tạo Mẫu Mã Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Gọi API thêm Variant với stock = 0
                boolean ok = ProductApi.addVariant(
                    productId, 
                    cbGroup.getSelectedItem().toString(), 
                    txtVariantName.getText(), 
                    Double.parseDouble(txtPrice.getText()), 
                    0 
                );
                
                if (ok) {
                    JOptionPane.showMessageDialog(this, "✅ Đã khởi tạo Mẫu mã thành công!");
                    tableModel.setValueAt(txtVariantName.getText(), rowIndex, 2);
                    tableModel.setValueAt("✅ Đã có (Sẵn sàng nhập)", rowIndex, 5); // Đổi Xanh lá cây
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Lỗi khi thêm Mẫu mã vào Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ cho giá tiền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Hàm load ngầm Map để chứa cặp [Tên SP : ID]
    private void refreshProductMap() {
        productMap.clear();
        String allProductsJson = ProductApi.getAllProducts();
        if (allProductsJson != null && allProductsJson.length() > 5) {
            JSONArray allProds = new JSONArray(allProductsJson);
            for (int i = 0; i < allProds.length(); i++) {
                JSONObject p = allProds.getJSONObject(i);
                productMap.put(p.getString("name").toLowerCase().trim(), p.getInt("id"));
            }
        }
    }

    private void scanInvoiceWithAI() {
        if (selectedFile == null) return;
        
        JDialog loadingDialog = new JDialog(this, "Hệ Thống AI Đang Xử Lý", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); progressBar.setStringPainted(true);
        progressBar.setString("Google Gemini đang đọc và bóc tách hóa đơn...");
        loadingDialog.add(progressBar, BorderLayout.CENTER);
        loadingDialog.setSize(380, 70); loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    refreshProductMap(); // Cập nhật danh sách Gốc mới nhất

                    HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(60)).build();
                    String boundary = "---JavaHttpClientBoundary" + System.currentTimeMillis();
                    byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                    
                    String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"invoice_image\"; filename=\"" + selectedFile.getName() + "\"\r\nContent-Type: " + Files.probeContentType(selectedFile.toPath()) + "\r\n\r\n";
                    String tail = "\r\n--" + boundary + "--\r\n";
                    
                    byte[] headBytes = head.getBytes(StandardCharsets.UTF_8);
                    byte[] tailBytes = tail.getBytes(StandardCharsets.UTF_8);
                    byte[] body = new byte[headBytes.length + fileBytes.length + tailBytes.length];
                    System.arraycopy(headBytes, 0, body, 0, headBytes.length);
                    System.arraycopy(fileBytes, 0, body, headBytes.length, fileBytes.length);
                    System.arraycopy(tailBytes, 0, body, headBytes.length + fileBytes.length, tailBytes.length);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(khongphaigiaodien.ApiConfig.BASE_URL + "/ai/scan-invoice"))
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                            .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token) 
                            .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    JSONObject jsonRes = new JSONObject(response.body());
                    
                    if (jsonRes.getBoolean("success")) {
                        JSONObject aiData = jsonRes.getJSONObject("data");
                        JSONArray items = aiData.getJSONArray("items");
                        
                        SwingUtilities.invokeLater(() -> {
                            tableModel.setRowCount(0); 
                            currentSupplier = aiData.optString("supplier_name", "Nhà cung cấp chưa rõ");
                            double tongTien = 0;

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String tenSP = item.getString("product_name");
                                String mauMa = item.optString("variant_name", ""); 
                                int soLuong = item.getInt("quantity");
                                long donGia = item.getLong("import_price");
                                tongTien += (soLuong * donGia);
                                
                                String status = "⚠️ SP Mới (Click đúp Tạo SP Gốc)";
                                // Kiểm tra xem Sản phẩm Gốc đã có trong Database chưa?
                                if (productMap.containsKey(tenSP.toLowerCase().trim())) {
                                    if (!mauMa.isEmpty()) {
                                        // Gốc có rồi, nhưng có thêm Mẫu mã -> Bắt xác nhận Mẫu mã
                                        status = "⚡ Click đúp Tạo Mẫu Mã";
                                    } else {
                                        status = "✅ Đã có (Sẵn sàng nhập)";
                                    }
                                }
                                
                                tableModel.addRow(new Object[]{ i + 1, tenSP, mauMa, soLuong, String.format("%,d", donGia), status });
                            }
                            currentTaxAmount = tongTien * (currentTaxPercent / 100.0);
                            currentTotalAmount = tongTien + currentTaxAmount;
                            lblFilePath.setText(String.format("<html>Nhà cung cấp: <b>%s</b> | TỔNG THANH TOÁN: %,.0f VNĐ</html>", currentSupplier, currentTotalAmount));
                        });
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ScanInvoiceDialog.this, "Lỗi Server: " + ex.getMessage()));
                }
                return null;
            }
            @Override
            protected void done() { loadingDialog.dispose(); } 
        };
        worker.execute(); loadingDialog.setVisible(true); 
    }

    private void saveToDatabase() {
        if (tableModel.getRowCount() == 0) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!tableModel.getValueAt(i, 5).toString().contains("✅")) {
                JOptionPane.showMessageDialog(this, "Có lỗi ở dòng số " + (i+1) + ".\nVui lòng Click đúp để hoàn thiện dữ liệu (Tạo SP hoặc Mẫu mã) trước khi lưu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

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
                item.put("variant_name", tableModel.getValueAt(i, 2).toString()); 
                item.put("quantity", Integer.parseInt(tableModel.getValueAt(i, 3).toString()));
                item.put("import_price", Double.parseDouble(tableModel.getValueAt(i, 4).toString().replace(",", "")));
                itemsArray.put(item);
            }
            dataToSave.put("items", itemsArray);

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(khongphaigiaodien.ApiConfig.BASE_URL + "/import/create"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token) 
                    .POST(HttpRequest.BodyPublishers.ofString(dataToSave.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonRes = new JSONObject(response.body());
            
            if (jsonRes.getBoolean("success")) {
                JOptionPane.showMessageDialog(this, "✅ " + jsonRes.getString("message"));
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "❌ Lỗi: " + jsonRes.getString("message"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối Server", "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
        }
    }
}