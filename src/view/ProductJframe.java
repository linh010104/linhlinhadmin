/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.CategoryDTO;
import java.util.List;
import java.util.ArrayList; 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
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
    
    private JTable varTable; 
    private DefaultTableModel varModel;
    
    private JLabel lblVariantTitle;
    private int currentSelectedProductId = -1; // Lưu ID sản phẩm đang chọn

    public ProductJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Thêm SP mới");
        styleButton(btnAdd, new Color(0, 153, 76));

        JButton btnEdit = new JButton("Sửa SP");
        styleButton(btnEdit, new Color(255, 153, 51));

        JButton btnDelete = new JButton("Xóa SP");
        styleButton(btnDelete, new Color(220, 53, 69));

        JButton btnUploadImage = new JButton("Thêm ảnh");
        styleButton(btnUploadImage, new Color(102, 51, 153));

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnUploadImage.addActionListener(e -> uploadImage());

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnUploadImage);

        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        model.setColumnIdentifiers(new String[]{ 
            "ID", "Tên", "SKU", "Giá bán", "Giá nhập", "Tổng Tồn Kho", "Bảo hành", "Thông số", "Ghi chú", "Trạng thái", "CatID" 
        });

        table = new JTable(model);
        styleTable(table);
        
        table.getColumnModel().getColumn(10).setMinWidth(0);
        table.getColumnModel().getColumn(10).setMaxWidth(0);
        table.getColumnModel().getColumn(10).setPreferredWidth(0);

        JScrollPane masterScrollPane = new JScrollPane(table);
        masterScrollPane.getViewport().setBackground(Color.WHITE);
        masterScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết Phiên bản"));

        // Thanh công cụ của bảng Phiên bản
        JPanel variantToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        variantToolbar.setBackground(Color.WHITE);
        
        lblVariantTitle = new JLabel("Vui lòng chọn 1 sản phẩm ở trên để xem phiên bản");
        lblVariantTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVariantTitle.setForeground(new Color(0, 102, 204));
        
        JButton btnAddVar = new JButton("+ Thêm Phiên Bản");
        styleButton(btnAddVar, new Color(0, 102, 204));
        
        JButton btnDelVar = new JButton("- Xóa Phiên Bản");
        styleButton(btnDelVar, new Color(220, 53, 69));

        variantToolbar.add(lblVariantTitle);
        variantToolbar.add(Box.createHorizontalStrut(20));
        variantToolbar.add(btnAddVar);
        variantToolbar.add(btnDelVar);
        detailPanel.add(variantToolbar, BorderLayout.NORTH);

        // Bảng dữ liệu Phiên bản
        varModel = new DefaultTableModel(new String[]{"ID", "Nhóm (Màu/Dung lượng)", "Tên Phiên Bản", "Giá cộng thêm", "Tồn kho"}, 0){
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        varTable = new JTable(varModel);
        styleTable(varTable);
        JScrollPane detailScrollPane = new JScrollPane(varTable);
        detailScrollPane.getViewport().setBackground(Color.WHITE);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        // ==========================================
        // 4. CHIA ĐÔI MÀN HÌNH (JSPLITPANE)
        // ==========================================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, masterScrollPane, detailPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.6); 
        add(splitPane, BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    currentSelectedProductId = (int) model.getValueAt(row, 0);
                    String pName = model.getValueAt(row, 1).toString();
                    lblVariantTitle.setText("Đang xem phiên bản của: " + pName);
                    loadVariants(currentSelectedProductId);
                } else {
                    currentSelectedProductId = -1;
                    lblVariantTitle.setText("Vui lòng chọn 1 sản phẩm ở trên để xem phiên bản");
                    varModel.setRowCount(0);
                }
            }
        });
        btnAddVar.addActionListener(e -> showAddVariantDialog());
        btnDelVar.addActionListener(e -> deleteSelectedVariant());
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
                p.optDouble("price", 0), 
                p.optDouble("import_price", 0), 
                p.optInt("stock_quantity", 0), // LƯU Ý: Ở CSDL cái này nên là tự cộng tổng
                p.optInt("warranty_month", 0), 
                p.optString("specifications", ""), 
                p.optString("description", ""), 
                p.optInt("status") == 1 ? "Đang bán" : "Ẩn",
                p.optInt("category_id", -1) 
            });
        }
        varModel.setRowCount(0); // Clear bảng dưới khi reload bảng trên
    }

    private void loadVariants(int productId) {
        varModel.setRowCount(0);
        String jsonDetail = ProductApi.getProductDetail(productId);
        if(jsonDetail != null) {
            try {
                JSONObject obj = new JSONObject(jsonDetail);
                if(obj.has("variants")) {
                    JSONArray vars = obj.getJSONArray("variants");
                    for(int i=0; i<vars.length(); i++) {
                        JSONObject v = vars.getJSONObject(i);
                        varModel.addRow(new Object[]{
                            v.getInt("id"), v.optString("variant_group"), v.optString("variant_name"),
                            v.optDouble("additional_price", 0), v.optInt("stock_quantity", 0)
                        });
                    }
                }
            } catch(Exception ex) { ex.printStackTrace(); }
        }
    }

    private void showAddVariantDialog() {
        if (currentSelectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm ở bảng trên trước khi thêm phiên bản!");
            return;
        }

        JComboBox<String> cbGroup = new JComboBox<>(new String[]{"Màu sắc", "Dung lượng", "Loại Switch", "Kích thước"});
        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField("0");
        JTextField txtStock = new JTextField("0");

        Object[] form = {
            "Nhóm phân loại:", cbGroup,
            "Tên phiên bản (VD: Đỏ, 256GB):", txtName,
            "Giá cộng thêm (VNĐ):", txtPrice,
            "Số lượng nhập kho:", txtStock
        };

        if (JOptionPane.showConfirmDialog(this, form, "Thêm Phiên Bản Mới", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if(txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên phiên bản không được để trống!"); 
                return;
            }
            boolean ok = ProductApi.addVariant(currentSelectedProductId, cbGroup.getSelectedItem().toString(), txtName.getText(), 
                                               Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()));
            if(ok) {
                loadVariants(currentSelectedProductId); // Tự động load lại bảng dưới
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        }
    }

    private void deleteSelectedVariant() {
        int vRow = varTable.getSelectedRow();
        if(vRow == -1) { 
            JOptionPane.showMessageDialog(this, "Hãy chọn 1 dòng ở bảng Phiên bản bên dưới để xóa!"); 
            return; 
        }
        int varId = (int) varModel.getValueAt(vRow, 0);
        if(JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phiên bản này?") == JOptionPane.YES_OPTION) {
            if(ProductApi.deleteVariant(varId)) {
                loadVariants(currentSelectedProductId);
            }
        }
    }

    // Các hàm Add, Edit, Delete SP chính giữ nguyên logic
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để sửa!"); return; }
        
        int id = (int) model.getValueAt(row, 0);
        
        JTextField txtName = new JTextField(model.getValueAt(row, 1).toString());
        JTextField txtSku = new JTextField(model.getValueAt(row, 2).toString());
        JTextField txtPrice = new JTextField(model.getValueAt(row, 3).toString());
        JTextField txtImportPrice = new JTextField(model.getValueAt(row, 4).toString());
        JTextField txtStock = new JTextField(model.getValueAt(row, 5).toString());
        JTextField txtWarranty = new JTextField(model.getValueAt(row, 6).toString());
        JTextField txtSpecs = new JTextField(model.getValueAt(row, 7).toString());
        JTextField txtDesc = new JTextField(model.getValueAt(row, 8).toString());
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        List<CategoryDTO> allCats = CategoryApi.getAllCategories();
        for (CategoryDTO c : allCats) cbCategory.addItem(c);
        
        int currentCatId = (int) model.getValueAt(row, 10); 
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getId() == currentCatId) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
        
        Object[] form = { 
            "Tên:", txtName, "SKU:", txtSku, "Giá bán (Gốc):", txtPrice, 
            "Giá nhập:", txtImportPrice, "Tồn kho tổng:", txtStock,
            "Bảo hành (tháng):", txtWarranty, "Thông số kỹ thuật:", txtSpecs, 
            "Ghi chú:", txtDesc, "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Sửa sản phẩm chính", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            
            boolean ok = ProductApi.updateProduct(
                id, txtName.getText(), txtSku.getText(), 
                Double.parseDouble(txtPrice.getText()), 
                Double.parseDouble(txtImportPrice.getText()), 
                Integer.parseInt(txtStock.getText()), 
                Integer.parseInt(txtWarranty.getText()), 
                cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            if (ok) loadProducts();
        }
    }

    private void showAddDialog() {
        JTextField txtName = new JTextField();
        JTextField txtSku = new JTextField();
        JTextField txtPrice = new JTextField("0");
        JTextField txtImportPrice = new JTextField("0");
        JTextField txtStock = new JTextField("0");
        JTextField txtWarranty = new JTextField("0");
        JTextField txtSpecs = new JTextField();
        JTextField txtDesc = new JTextField();
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        for (CategoryDTO c : CategoryApi.getAllCategories()) cbCategory.addItem(c);
        
        Object[] form = { 
            "Tên:", txtName, "SKU:", txtSku, "Giá bán (Gốc):", txtPrice, 
            "Giá nhập:", txtImportPrice, "Tồn kho tổng:", txtStock,
            "Bảo hành (tháng):", txtWarranty, "Thông số kỹ thuật:", txtSpecs, 
            "Ghi chú:", txtDesc, "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Thêm sản phẩm chính mới", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            boolean ok = ProductApi.createProduct(
                txtName.getText(), txtSku.getText(), 
                Double.parseDouble(txtPrice.getText()), 
                Double.parseDouble(txtImportPrice.getText()), 
                Integer.parseInt(txtStock.getText()), 
                Integer.parseInt(txtWarranty.getText()), 
                cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            if (ok) loadProducts();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này sẽ xóa CẢ các phiên bản. Tiếp tục?") == JOptionPane.YES_OPTION) {
            if (ProductApi.deleteProduct(id)) loadProducts();
        }
    }

    private void uploadImage() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm ở bảng trên để thêm ảnh!");
            return;
        }
        
        int productId = (int) model.getValueAt(row, 0);
        JFileChooser chooser = new JFileChooser();
        
        // BẬT CHẾ ĐỘ CHỌN NHIỀU FILE CÙNG LÚC
        chooser.setMultiSelectionEnabled(true);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Lấy danh sách các file đã chọn
            File[] files = chooser.getSelectedFiles();
            
            if (files.length == 0) return;
            
            // Gọi hàm API mới (nhận mảng files)
            boolean ok = ProductApi.uploadImages(productId, files);
            
            if (ok) {
                JOptionPane.showMessageDialog(this, "Tuyệt vời! Đã upload thành công " + files.length + " ảnh.");
            } else {
                JOptionPane.showMessageDialog(this, "Upload thất bại. Vui lòng check lại kết nối hoặc dung lượng ảnh!");
            }
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(40, 40, 40)); 
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
