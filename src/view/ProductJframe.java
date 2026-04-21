/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.CategoryDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import khongphaigiaodien.CategoryApi;
import khongphaigiaodien.ProductApi;

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
        styleButton(btnUploadImage, new Color(102, 51, 153)); // Màu tím

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnUploadImage.addActionListener(e -> uploadImage());

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        topPanel.add(Box.createHorizontalStrut(20)); // Khoảng cách
        topPanel.add(btnUploadImage);

        add(topPanel, BorderLayout.NORTH);

        // --- BODY ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        // Đã thêm các cột: Giá nhập, Tồn kho, Thông số
        model.setColumnIdentifiers(new String[]{ "ID", "Tên", "SKU", "Giá bán", "Giá nhập", "Tồn kho", "Bảo hành", "Thông số", "Ghi chú", "Trạng thái" });

        table = new JTable(model);
        styleTable(table);
        
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
        json = json.substring(1, json.length() - 1);
        String[] items = json.split("\\},\\{");
        
        for (String item : items) {
            item = item.replace("{", "").replace("}", "");
            String[] fields = item.split(",");
            
            int id = 0, warranty = 0, status = 0, stockQuantity = 0;
            String name = "", sku = "", description = "", specifications = "";
            double price = 0, importPrice = 0;
            
            for (String field : fields) {
                String[] pair = field.split(":", 2);
                if (pair.length < 2) continue;
                String key = pair[0].replace("\"", "").trim();
                String value = pair[1].replace("\"", "").trim();
                
                // Chống lỗi parse chuỗi "null" hoặc rỗng
                if(value.equals("null")) value = ""; 

                switch (key) {
                    case "id" -> id = Integer.parseInt(value);
                    case "name" -> name = value;
                    case "sku" -> sku = value;
                    case "price" -> price = value.isEmpty() ? 0 : Double.parseDouble(value);
                    case "import_price" -> importPrice = value.isEmpty() ? 0 : Double.parseDouble(value);
                    case "stock_quantity" -> stockQuantity = value.isEmpty() ? 0 : Integer.parseInt(value);
                    case "warranty_month" -> warranty = value.isEmpty() ? 0 : Integer.parseInt(value);
                    case "status" -> status = value.isEmpty() ? 0 : Integer.parseInt(value);
                    case "description" -> description = value;
                    case "specifications" -> specifications = value;
                }
            }
            model.addRow(new Object[]{ id, name, sku, price, importPrice, stockQuantity, warranty, specifications, description, status == 1 ? "Đang bán" : "Ẩn" });
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
            "Tên:", txtName, 
            "SKU:", txtSku, 
            "Giá bán:", txtPrice, 
            "Giá nhập:", txtImportPrice, 
            "Tồn kho:", txtStock,
            "Bảo hành (tháng):", txtWarranty, 
            "Thông số kỹ thuật:", txtSpecs, 
            "Ghi chú:", txtDesc, 
            "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Thêm sản phẩm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            
            // Chống lỗi khi người dùng để trống
            double price = txtPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText());
            double importPrice = txtImportPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtImportPrice.getText());
            int stock = txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText());
            int warranty = txtWarranty.getText().isEmpty() ? 0 : Integer.parseInt(txtWarranty.getText());
            
            boolean ok = ProductApi.createProduct(
                txtName.getText(), txtSku.getText(), price, importPrice, stock, 
                warranty, cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            if (ok) loadProducts();
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để sửa!"); return; }
        
        int id = (int) model.getValueAt(row, 0);
        
        // Lấy dữ liệu từ bảng đổ lên Form (Cẩn thận thứ tự index cột)
        JTextField txtName = new JTextField(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
        JTextField txtSku = new JTextField(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
        JTextField txtPrice = new JTextField(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "0");
        JTextField txtImportPrice = new JTextField(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "0");
        JTextField txtStock = new JTextField(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "0");
        JTextField txtWarranty = new JTextField(model.getValueAt(row, 6) != null ? model.getValueAt(row, 6).toString() : "0");
        JTextField txtSpecs = new JTextField(model.getValueAt(row, 7) != null ? model.getValueAt(row, 7).toString() : "");
        JTextField txtDesc = new JTextField(model.getValueAt(row, 8) != null ? model.getValueAt(row, 8).toString() : "");
        
        JComboBox<CategoryDTO> cbCategory = new JComboBox<>();
        for (CategoryDTO c : CategoryApi.getAllCategories()) cbCategory.addItem(c);
        
        Object[] form = { 
            "Tên:", txtName, 
            "SKU:", txtSku, 
            "Giá bán:", txtPrice, 
            "Giá nhập:", txtImportPrice, 
            "Tồn kho:", txtStock,
            "Bảo hành (tháng):", txtWarranty, 
            "Thông số kỹ thuật:", txtSpecs, 
            "Ghi chú:", txtDesc, 
            "Danh mục:", cbCategory 
        };
        
        if (JOptionPane.showConfirmDialog(this, form, "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            CategoryDTO cat = (CategoryDTO) cbCategory.getSelectedItem();
            
            double price = txtPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText());
            double importPrice = txtImportPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtImportPrice.getText());
            int stock = txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText());
            int warranty = txtWarranty.getText().isEmpty() ? 0 : Integer.parseInt(txtWarranty.getText());
            
            boolean ok = ProductApi.updateProduct(
                id, txtName.getText(), txtSku.getText(), price, importPrice, stock, 
                warranty, cat.getId(), txtDesc.getText(), txtSpecs.getText(), 1, 1
            );
            if (ok) loadProducts();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để xóa!"); return; }
        
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (ProductApi.deleteProduct(id)) {
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

    private void uploadImage() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn sản phẩm trước"); return; }
        int productId = (int) model.getValueAt(row, 0);
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean ok = ProductApi.uploadImage(productId, file);
            JOptionPane.showMessageDialog(this, ok ? "Upload ảnh thành công" : "Upload thất bại");
        }
    }

    // --- STYLE HELPER ---
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
