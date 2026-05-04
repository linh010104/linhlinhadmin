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

        // NÚT MỚI: QUẢN LÝ PHIÊN BẢN
        JButton btnManageVariants = new JButton("Quản lý phiên bản");
        styleButton(btnManageVariants, new Color(0, 102, 204)); // Màu xanh dương

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnUploadImage.addActionListener(e -> uploadImage());
        btnManageVariants.addActionListener(e -> showVariantDialog());

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnUploadImage);
        topPanel.add(btnManageVariants);

        add(topPanel, BorderLayout.NORTH);

        // --- BODY ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        model.setColumnIdentifiers(new String[]{ 
            "ID", "Tên", "SKU", "Giá bán", "Giá nhập", "Tồn kho", "Bảo hành", "Thông số", "Ghi chú", "Trạng thái", "CatID" 
        });

        table = new JTable(model);
        styleTable(table);
        
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
                p.optInt("category_id", -1) 
            });
        }
    }

    // ==========================================
    // LOGIC QUẢN LÝ PHIÊN BẢN (MỚI)
    // ==========================================
    private void showVariantDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm để quản lý phiên bản!"); 
            return; 
        }
        
        int productId = (int) model.getValueAt(row, 0);
        String productName = model.getValueAt(row, 1).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Phiên bản: " + productName, true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // --- NỬA TRÊN: FORM THÊM MỚI ---
        JPanel panelTop = new JPanel(new GridLayout(5, 2, 10, 10));
        panelTop.setBorder(BorderFactory.createTitledBorder("Thêm Phiên Bản Mới"));

        JComboBox<String> cbGroup = new JComboBox<>(new String[]{"Dung lượng", "Màu sắc", "Cấu hình", "Loại Switch"});
        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField("0");
        JTextField txtStock = new JTextField("0");
        JButton btnSaveVariant = new JButton("Thêm Phiên Bản");
        styleButton(btnSaveVariant, new Color(0, 153, 76));

        panelTop.add(new JLabel("Nhóm (VD: Màu sắc):")); panelTop.add(cbGroup);
        panelTop.add(new JLabel("Tên (VD: Đen, 256GB):")); panelTop.add(txtName);
        panelTop.add(new JLabel("Giá cộng thêm:")); panelTop.add(txtPrice);
        panelTop.add(new JLabel("Số lượng kho:")); panelTop.add(txtStock);
        panelTop.add(new JLabel("")); panelTop.add(btnSaveVariant);
        
        dialog.add(panelTop, BorderLayout.NORTH);

        // --- NỬA DƯỚI: DANH SÁCH ---
        DefaultTableModel varModel = new DefaultTableModel(new String[]{"ID", "Nhóm", "Tên", "Giá thêm", "Tồn kho"}, 0){
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable varTable = new JTable(varModel);
        styleTable(varTable);
        dialog.add(new JScrollPane(varTable), BorderLayout.CENTER);

        // Nút Xóa nằm ở dưới cùng
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelVariant = new JButton("Xóa Phiên Bản Đang Chọn");
        styleButton(btnDelVariant, new Color(220, 53, 69));
        panelBottom.add(btnDelVariant);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        // Hàm load dữ liệu vào bảng con
        Runnable loadVariants = () -> {
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
        };

        // Bắt sự kiện nút Thêm
        btnSaveVariant.addActionListener(e -> {
            if(txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên không được để trống!"); return;
            }
            boolean ok = ProductApi.addVariant(productId, cbGroup.getSelectedItem().toString(), txtName.getText(), 
                                               Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()));
            if(ok) {
                txtName.setText(""); txtPrice.setText("0"); txtStock.setText("0");
                loadVariants.run();
                JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
            }
        });

        // Bắt sự kiện nút Xóa
        btnDelVariant.addActionListener(e -> {
            int vRow = varTable.getSelectedRow();
            if(vRow == -1) { JOptionPane.showMessageDialog(dialog, "Chọn phiên bản để xóa!"); return; }
            int varId = (int) varModel.getValueAt(vRow, 0);
            if(JOptionPane.showConfirmDialog(dialog, "Xóa phiên bản này?") == JOptionPane.YES_OPTION) {
                if(ProductApi.deleteVariant(varId)) loadVariants.run();
            }
        });

        // Load data lần đầu mở lên
        loadVariants.run();
        dialog.setVisible(true);
    }

    // ==========================================
    // CÁC HÀM CŨ GIỮ NGUYÊN (Không đổi)
    // ==========================================

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
