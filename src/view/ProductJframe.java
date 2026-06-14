/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.CategoryDTO;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import khongphaigiaodien.CategoryApi;
import khongphaigiaodien.ProductApi;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author AlinV
 */
public class ProductJframe extends JPanel{
    private JTable table; 
    private DefaultTableModel model;

    public ProductJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // --- THANH CÔNG CỤ TRÊN CÙNG ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("+ Thêm SP mới");
        styleButton(btnAdd, new Color(0, 153, 76));
        btnAdd.addActionListener(e -> showAddDialog());
        topPanel.add(btnAdd);

        add(topPanel, BorderLayout.NORTH);

        // --- BẢNG SẢN PHẨM RÚT GỌN ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        // Thêm cột Hành động vào cuối
        model.setColumnIdentifiers(new String[]{ 
            "ID", "Tên Sản Phẩm", "SKU", "Giá Bán", "Tồn Kho", "Trạng Thái", "HÀNH ĐỘNG" 
        });

        table = new JTable(model);
        styleTable(table);
        
        // Xử lý sự kiện click vào cột "HÀNH ĐỘNG"
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                // Cột Hành động là cột số 6 (Index = 6)
                if (col == 6 && row >= 0) {
                    int productId = (int) model.getValueAt(row, 0);
                    showProductDetailDialog(productId);
                }
            }
        });

        JScrollPane masterScrollPane = new JScrollPane(table);
        masterScrollPane.getViewport().setBackground(Color.WHITE);
        masterScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(masterScrollPane, BorderLayout.CENTER);
        
        loadProducts();
    }

    private void loadProducts() {
        String json = ProductApi.getAllProducts();
        if (json == null || json.length() < 5) return;
        model.setRowCount(0);
        
        JSONArray arr = new JSONArray(json); 
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            model.addRow(new Object[]{ 
                p.getInt("id"), 
                p.optString("name"), 
                p.optString("sku"), 
                String.format("%,.0f đ", p.optDouble("price", 0)), 
                p.optInt("stock_quantity", 0),
                p.optInt("status") == 1 ? "Đang bán" : "Ẩn",
                "✏️ Chi tiết / Sửa" // Chữ hiển thị ở cột Hành động
            });
        }
    }

    private void showAddDialog() {
       JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "✨ Thêm Sản Phẩm Mới", true);
        dialog.setSize(850, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // --- KHUNG TRÊN: Các ô nhập ngắn (GridLayout 2 cột) ---
        JPanel topForm = new JPanel(new GridLayout(0, 2, 10, 15)); 
        
        JTextField txtName = new JTextField();
        JTextField txtSku = new JTextField();
        JTextField txtPrice = new JTextField("0");
        JTextField txtImportPrice = new JTextField("0");
        JTextField txtStock = new JTextField("0");
        JTextField txtWarranty = new JTextField("0");
        
        JTextField[] textFields = {txtName, txtSku, txtPrice, txtImportPrice, txtStock, txtWarranty};
        for (JTextField tf : textFields) {
            tf.setPreferredSize(new Dimension(tf.getWidth(), 35));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new Dimension(cbCategory.getWidth(), 35));
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (CategoryDTO c : CategoryApi.getAllCategories()) {
            cbCategory.addItem(c);
        }

        topForm.add(new JLabel("Tên sản phẩm * :")); topForm.add(txtName);
        topForm.add(new JLabel("Mã SKU:")); topForm.add(txtSku);
        topForm.add(new JLabel("Giá bán (VNĐ) * :")); topForm.add(txtPrice);
        topForm.add(new JLabel("Giá nhập (VNĐ):")); topForm.add(txtImportPrice);
        topForm.add(new JLabel("Tồn kho tổng:")); topForm.add(txtStock);
        topForm.add(new JLabel("Bảo hành (Tháng):")); topForm.add(txtWarranty);
        topForm.add(new JLabel("Danh mục:")); topForm.add(cbCategory);

        // --- KHUNG DƯỚI: Các ô nhập văn bản dài (BoxLayout) ---
        JPanel bottomForm = new JPanel();
        bottomForm.setLayout(new BoxLayout(bottomForm, BoxLayout.Y_AXIS));
        bottomForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblSpecs = new JLabel("Thông số kỹ thuật:");
        lblSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Dùng JTextArea tự động xuống dòng cực xịn
        JTextArea txtSpecs = new JTextArea();
        txtSpecs.setLineWrap(true);
        txtSpecs.setWrapStyleWord(true);
        txtSpecs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollSpecs = new JScrollPane(txtSpecs);
        scrollSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollSpecs.setPreferredSize(new Dimension(400, 100));

        JLabel lblDesc = new JLabel("Mô tả / Ghi chú:");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JTextArea txtDesc = new JTextArea();
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDesc.setPreferredSize(new Dimension(400, 100));

        bottomForm.add(lblSpecs);
        bottomForm.add(Box.createVerticalStrut(5));
        bottomForm.add(scrollSpecs);
        bottomForm.add(lblDesc);
        bottomForm.add(Box.createVerticalStrut(5));
        bottomForm.add(scrollDesc);

        // Gộp Khung trên và Khung dưới
        JPanel mainFormPanel = new JPanel(new BorderLayout());
        mainFormPanel.add(topForm, BorderLayout.NORTH);
        mainFormPanel.add(bottomForm, BorderLayout.CENTER);

        // --- KHUNG NÚT BẤM (Hủy / Lưu) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnCancel = new JButton("❌ Hủy Bỏ");
        styleButton(btnCancel, new Color(108, 117, 125));
        btnCancel.addActionListener(e -> dialog.dispose()); // Đóng form
        
        JButton btnSave = new JButton("💾 Thêm Mới Sản Phẩm");
        styleButton(btnSave, new Color(0, 153, 76));
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
                boolean ok = ProductApi.createProduct(
                    txtName.getText(), txtSku.getText(), Double.parseDouble(txtPrice.getText()), 
                    Double.parseDouble(txtImportPrice.getText()), Integer.parseInt(txtStock.getText()), 
                    Integer.parseInt(txtWarranty.getText()), cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
                );
                
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Thêm thành công! Hãy bấm 'Chi tiết' để thêm Ảnh & Phiên bản.");
                    dialog.dispose(); // Đóng form
                    loadProducts();   // Load lại bảng
                } else {
                    JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra khi thêm sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đúng định dạng số cho Giá, Kho và Bảo hành!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        
        panel.add(new JScrollPane(mainFormPanel), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // =========================================================================
    // 🔥 TRÙM CUỐI: DIALOG QUẢN LÝ CHI TIẾT (ALL-IN-ONE)
    // =========================================================================
    private void showProductDetailDialog(int productId) {
        String jsonDetail = ProductApi.getProductDetail(productId);
        if (jsonDetail == null) return;
        
        JSONObject product = new JSONObject(jsonDetail);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quản lý Chi Tiết Sản Phẩm: " + product.optString("name"), true);
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(this);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // --- TAB 1: THÔNG TIN CHUNG ---
        JPanel tabInfo = createInfoTab(productId, product, dialog);
        tabbedPane.addTab("📋 Thông Tin Chung", tabInfo);

        // --- TAB 2: QUẢN LÝ PHIÊN BẢN ---
        JPanel tabVariants = createVariantTab(productId, product);
        tabbedPane.addTab("📦 Quản Lý Phiên Bản", tabVariants);

        // --- TAB 3: QUẢN LÝ HÌNH ẢNH (GALLERY) ---
        JPanel tabImages = createImageTab(productId, product);
        tabbedPane.addTab("🖼️ Hình Ảnh", tabImages);

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    // Giao diện Tab 1: Form Sửa thông tin
    private JPanel createInfoTab(int productId, JSONObject product, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 1. Khung trên (GridLayout) chỉ chứa các trường ngắn
        JPanel topForm = new JPanel(new GridLayout(0, 2, 10, 15)); 
        
        JTextField txtName = new JTextField(product.optString("name"));
        JTextField txtSku = new JTextField(product.optString("sku"));
        JTextField txtPrice = new JTextField(String.valueOf(product.optDouble("price", 0)));
        JTextField txtImportPrice = new JTextField(String.valueOf(product.optDouble("import_price", 0)));
        JTextField txtStock = new JTextField(String.valueOf(product.optInt("stock_quantity", 0)));
        JTextField txtWarranty = new JTextField(String.valueOf(product.optInt("warranty_month", 0)));
        
        JTextField[] textFields = {txtName, txtSku, txtPrice, txtImportPrice, txtStock, txtWarranty};
        for (JTextField tf : textFields) {
            tf.setPreferredSize(new Dimension(tf.getWidth(), 35));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new Dimension(cbCategory.getWidth(), 35));
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        List<CategoryDTO> allCats = CategoryApi.getAllCategories();
        for (CategoryDTO c : allCats) {
            cbCategory.addItem(c);
            if (c.getId() == product.optInt("category_id")) {
                cbCategory.setSelectedItem(c);
            }
        }

        topForm.add(new JLabel("Tên sản phẩm:")); topForm.add(txtName);
        topForm.add(new JLabel("Mã SKU:")); topForm.add(txtSku);
        topForm.add(new JLabel("Giá bán (VNĐ):")); topForm.add(txtPrice);
        topForm.add(new JLabel("Giá nhập (VNĐ):")); topForm.add(txtImportPrice);
        topForm.add(new JLabel("Tổng tồn kho:")); topForm.add(txtStock);
        topForm.add(new JLabel("Bảo hành (Tháng):")); topForm.add(txtWarranty);
        topForm.add(new JLabel("Danh mục:")); topForm.add(cbCategory);

        // 2. Khung dưới (BoxLayout) dành riêng cho các trường cần gõ dài
        JPanel bottomForm = new JPanel();
        bottomForm.setLayout(new BoxLayout(bottomForm, BoxLayout.Y_AXIS));
        bottomForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblSpecs = new JLabel("Thông số kỹ thuật:");
        lblSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Dùng JTextArea thay vì JTextField, bật tính năng tự xuống dòng
        JTextArea txtSpecs = new JTextArea(product.optString("specifications"));
        txtSpecs.setLineWrap(true);
        txtSpecs.setWrapStyleWord(true);
        txtSpecs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollSpecs = new JScrollPane(txtSpecs);
        scrollSpecs.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollSpecs.setPreferredSize(new Dimension(400, 80)); // Set chiều cao 80px

        JLabel lblDesc = new JLabel("Mô tả / Ghi chú:");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JTextArea txtDesc = new JTextArea(product.optString("description"));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDesc.setPreferredSize(new Dimension(400, 80));

        bottomForm.add(lblSpecs);
        bottomForm.add(Box.createVerticalStrut(5));
        bottomForm.add(scrollSpecs);
        bottomForm.add(lblDesc);
        bottomForm.add(Box.createVerticalStrut(5));
        bottomForm.add(scrollDesc);

        // 3. Gộp cả 2 khung lại
        JPanel mainFormPanel = new JPanel(new BorderLayout());
        mainFormPanel.add(topForm, BorderLayout.NORTH);
        mainFormPanel.add(bottomForm, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(mainFormPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelete = new JButton("🗑️ Xóa Toàn Bộ Sản Phẩm");
        styleButton(btnDelete, new Color(220, 53, 69));
        btnDelete.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(panel, "Xóa sản phẩm này cùng toàn bộ ảnh và phiên bản?") == JOptionPane.YES_OPTION) {
                if (ProductApi.deleteProduct(productId)) {
                    parentDialog.dispose();
                    loadProducts();
                }
            }
        });

        JButton btnSave = new JButton("💾 Lưu Thay Đổi");
        styleButton(btnSave, new Color(0, 102, 204));
        btnSave.addActionListener(e -> {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            String safeDesc = txtDesc.getText().replace("\n", "\\n").replace("\r", "");
            String safeSpecs = txtSpecs.getText().replace("\n", "\\n").replace("\r", "");
            boolean ok = ProductApi.updateProduct(
                productId, txtName.getText(), txtSku.getText(), Double.parseDouble(txtPrice.getText()), 
                Double.parseDouble(txtImportPrice.getText()), Integer.parseInt(txtStock.getText()), 
                Integer.parseInt(txtWarranty.getText()), cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            if (ok) {
                JOptionPane.showMessageDialog(panel, "Đã lưu thông tin chung!");
                loadProducts();
            }
        });

        btnPanel.add(btnDelete);
        btnPanel.add(btnSave);
        
        panel.add(new JScrollPane(wrapper), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // Giao diện Tab 2: Quản lý Bảng Phiên bản
    private JPanel createVariantTab(int productId, JSONObject product) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel vModel = new DefaultTableModel(new String[]{"ID", "Nhóm", "Tên Phiên Bản", "Giá +", "Tồn kho"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable vTable = new JTable(vModel);
        styleTable(vTable);

        // Đổ dữ liệu
        if (product.has("variants")) {
            JSONArray vars = product.getJSONArray("variants");
            for (int i = 0; i < vars.length(); i++) {
                JSONObject v = vars.getJSONObject(i);
                vModel.addRow(new Object[]{
                    v.getInt("id"), v.optString("variant_group"), v.optString("variant_name"),
                    v.optDouble("additional_price", 0), v.optInt("stock_quantity", 0)
                });
            }
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddV = new JButton("+ Thêm Phiên Bản"); styleButton(btnAddV, new Color(0, 153, 76));
        JButton btnDelV = new JButton("- Xóa Đã Chọn"); styleButton(btnDelV, new Color(220, 53, 69));

        btnAddV.addActionListener(e -> {
            JComboBox<String> cbGroup = new JComboBox<>(new String[]{"Màu sắc", "Dung lượng", "Kích thước"});
            JTextField txtName = new JTextField(); JTextField txtPrice = new JTextField("0"); JTextField txtStock = new JTextField("0");
            Object[] form = { "Nhóm:", cbGroup, "Tên:", txtName, "Giá cộng thêm:", txtPrice, "Kho:", txtStock };
            if (JOptionPane.showConfirmDialog(panel, form, "Thêm Phiên Bản", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (ProductApi.addVariant(productId, cbGroup.getSelectedItem().toString(), txtName.getText(), Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()))) {
                    JOptionPane.showMessageDialog(panel, "Thêm thành công! Đóng mở lại cửa sổ để cập nhật danh sách."); // Cách nạp lại có thể tối ưu sau
                }
            }
        });

        btnDelV.addActionListener(e -> {
            int row = vTable.getSelectedRow();
            if (row != -1) {
                if (ProductApi.deleteVariant((int) vModel.getValueAt(row, 0))) {
                    vModel.removeRow(row);
                }
            }
        });

        topPanel.add(btnAddV); topPanel.add(btnDelV);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(vTable), BorderLayout.CENTER);
        return panel;
    }

    // Giao diện Tab 3: Thư viện Ảnh (Gallery)
    private JPanel createImageTab(int productId, JSONObject product) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Nút thêm ảnh bự chà bá ở trên cùng
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnUpload = new JButton("☁️ UP ẢNH MỚI LÊN CLOUDINARY");
        styleButton(btnUpload, new Color(102, 51, 153));
        topPanel.add(btnUpload);
        panel.add(topPanel, BorderLayout.NORTH);

        // Khu vực chứa lưới hình ảnh
        JPanel galleryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        galleryPanel.setBackground(new Color(245, 245, 245));
        
        if (product.has("images")) {
            JSONArray images = product.getJSONArray("images");
            for (int i = 0; i < images.length(); i++) {
                JSONObject img = images.getJSONObject(i);
                int imgId = img.getInt("id");
                String url = img.getString("image_url");

                // Thẻ chứa 1 hình ảnh
                JPanel card = new JPanel(new BorderLayout());
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                card.setPreferredSize(new Dimension(150, 180));

                JLabel lblPic = new JLabel("⏳ Đang tải...", SwingConstants.CENTER);
                lblPic.setPreferredSize(new Dimension(150, 140));
                
                // Dùng Luồng phụ để load ảnh từ mạng về không làm đơ phần mềm
                new SwingWorker<ImageIcon, Void>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        Image image = ImageIO.read(new URL(url));
                        return new ImageIcon(image.getScaledInstance(140, 130, Image.SCALE_SMOOTH));
                    }
                    @Override
                    protected void done() {
                        try {
                            lblPic.setIcon(get());
                            lblPic.setText(""); // Xóa chữ đang tải
                        } catch (Exception ex) { lblPic.setText("Lỗi load ảnh"); }
                    }
                }.execute();

                JButton btnDeleteImg = new JButton("❌ Xóa Ảnh Này");
                btnDeleteImg.setBackground(new Color(255, 240, 240));
                btnDeleteImg.setForeground(Color.RED);
                btnDeleteImg.setFocusPainted(false);
                btnDeleteImg.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                btnDeleteImg.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(panel, "Xóa vĩnh viễn ảnh này?") == JOptionPane.YES_OPTION) {
                        if (ProductApi.deleteImage(imgId)) {
                            galleryPanel.remove(card);
                            galleryPanel.revalidate();
                            galleryPanel.repaint();
                        } else {
                            JOptionPane.showMessageDialog(panel, "Lỗi khi xóa ảnh!");
                        }
                    }
                });

                card.add(lblPic, BorderLayout.CENTER);
                card.add(btnDeleteImg, BorderLayout.SOUTH);
                galleryPanel.add(card);
            }
        }
        
        JScrollPane scrollGallery = new JScrollPane(galleryPanel);
        scrollGallery.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollGallery, BorderLayout.CENTER);

        // Xử lý nút Up ảnh
        btnUpload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                if (files.length > 0) {
                    if (ProductApi.uploadImages(productId, files)) {
                        JOptionPane.showMessageDialog(panel, "Up thành công! Đóng mở lại Chi tiết để thấy ảnh mới.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Upload thất bại!");
                    }
                }
            }
        });

        return panel;
    }

    // Các hàm Helper dùng chung
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(40, 40, 40)); 
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
