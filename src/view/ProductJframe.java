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

    public ProductJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Thêm sản phẩm");
        styleButton(btnAdd, new Color(0, 153, 76));

        JButton btnEdit = new JButton("Sửa");
        styleButton(btnEdit, new Color(255, 153, 51));

        JButton btnDelete = new JButton("Xóa");
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

        // --- BODY ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Thêm cột "CatID" ở cuối (index 10) để lưu trữ ID danh mục ẩn
        model.setColumnIdentifiers(new String[]{ 
            "ID", "Tên", "SKU", "Giá bán", "Giá nhập", "Tồn kho", "Bảo hành", "Thông số", "Ghi chú", "Trạng thái", "CatID" 
        });

        table = new JTable(model);
        styleTable(table);
        
        // Ẩn cột CatID (index 10) để người dùng không nhìn thấy, nhưng máy vẫn dùng được
        table.getColumnModel().getColumn(10).setMinWidth(0);
        table.getColumnModel().getColumn(10).setMaxWidth(0);
        table.getColumnModel().getColumn(10).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        loadProducts();
    }

    private void loadProducts() {
        String json = ProductApi.getAllProducts();
        if (json == null || json.length() < 5) return;
        model.setRowCount(0);
        
        // Cách xử lý chuỗi JSON thủ công của Linh
        JSONArray arr = new JSONArray(json); 
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            model.addRow(new Object[]{ 
                p.getInt("id"), 
                p.optString("name"), 
                p.optString("sku"), 
                p.optDouble("price", 0), 
                p.optDouble("import_price", 0), 
                p.optInt("stock_quantity", 0), 
                p.optInt("warranty_month", 0), 
                p.optString("specifications", ""), 
                p.optString("description", ""), 
                p.optInt("status") == 1 ? "Đang bán" : "Ẩn",
                p.optInt("category_id", -1) // Cất ID danh mục vào đây
            });
        }
    }

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
        
        // --- LOGIC QUAN TRỌNG: Tự động chọn đúng danh mục hiện tại ---
        int currentCatId = (int) model.getValueAt(row, 10); // Lấy từ cột ẩn
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getId() == currentCatId) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
        
        Object[] form = { 
            "Tên:", txtName, "SKU:", txtSku, "Giá bán:", txtPrice, 
            "Giá nhập:", txtImportPrice, "Tồn kho:", txtStock,
            "Bảo hành (tháng):", txtWarranty, "Thông số:", txtSpecs, 
            "Ghi chú:", txtDesc, "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
            "Tên:", txtName, "SKU:", txtSku, "Giá bán:", txtPrice, 
            "Giá nhập:", txtImportPrice, "Tồn kho:", txtStock,
            "Bảo hành (tháng):", txtWarranty, "Thông số:", txtSpecs, 
            "Ghi chú:", txtDesc, "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Thêm sản phẩm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
        if (JOptionPane.showConfirmDialog(this, "Xóa sản phẩm?") == JOptionPane.YES_OPTION) {
            if (ProductApi.deleteProduct(id)) loadProducts();
        }
    }

    private void uploadImage() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int productId = (int) model.getValueAt(row, 0);
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean ok = ProductApi.uploadImage(productId, chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, ok ? "Thành công" : "Thất bại");
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
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50)); header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
