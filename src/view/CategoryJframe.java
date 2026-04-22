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
import khongphaigiaodien.CategoryApi;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class CategoryJframe extends JPanel{
    private JTable table;
    private DefaultTableModel model;

    public CategoryJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. HEADER (THANH CÔNG CỤ) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton btnAdd = new JButton("Thêm Mới");
        styleButton(btnAdd, new Color(0, 153, 76));
        btnAdd.addActionListener(e -> showDialog(null));

        JButton btnEdit = new JButton("Sửa Đổi");
        styleButton(btnEdit, new Color(255, 153, 51));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một danh mục để sửa!");
                return;
            }
            JSONObject current = new JSONObject();
            current.put("id", model.getValueAt(row, 0));
            current.put("name", model.getValueAt(row, 1));
            current.put("description", model.getValueAt(row, 2));
            showDialog(current);
        });

        JButton btnDelete = new JButton("Xóa");
        styleButton(btnDelete, new Color(220, 53, 69));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa danh mục này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (CategoryApi.delete(id)) loadCategories();
            }
        });

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        model = new DefaultTableModel(new String[]{"ID", "Tên danh mục", "Mô tả", "Danh mục cha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        loadCategories();
    }

    private void loadCategories() {
        String json = CategoryApi.getAllRaw();
        if (json == null) return;
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject c = arr.getJSONObject(i);
            model.addRow(new Object[]{ 
                c.getInt("id"), 
                c.getString("name"), 
                c.optString("description", ""), 
                c.optString("parent_name", "---") 
            });
        }
    }

    private void showDialog(JSONObject data) {
        JTextField txtName = new JTextField(data != null ? data.getString("name") : "", 20);
        JTextField txtDesc = new JTextField(data != null ? data.getString("description") : "", 20);
        
        JComboBox<CategoryDTO> cbParent = new JComboBox<>();
        cbParent.addItem(new CategoryDTO(-1, "-- Không có (Danh mục gốc) --"));
        
        try {
            JSONArray arr = new JSONArray(CategoryApi.getAllRaw());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                
                // CHỈ HIỆN DANH MỤC GỐC ĐỂ LÀM CHA (tránh đệ quy nhiều tầng khó quản lý)
                if (o.isNull("parent_id")) {
                    // Không cho phép chọn chính mình làm cha
                    if (data != null && data.getInt("id") == o.getInt("id")) continue;
                    
                    cbParent.addItem(new CategoryDTO(o.getInt("id"), o.getString("name")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        Object[] form = {
            "Tên danh mục:", txtName,
            "Mô tả:", txtDesc,
            "Thuộc danh mục cha:", cbParent
        };

        int result = JOptionPane.showConfirmDialog(this, form, 
                data == null ? "Thêm danh mục mới" : "Cập nhật danh mục", 
                JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được để trống!");
                return;
            }

            CategoryDTO selected = (CategoryDTO) cbParent.getSelectedItem();
            Integer parentId = (selected.getId() == -1) ? null : selected.getId();
            
            boolean success;
            if (data == null) {
                success = CategoryApi.create(name, txtDesc.getText(), parentId);
            } else {
                success = CategoryApi.update(data.getInt("id"), name, txtDesc.getText(), parentId);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Thao tác thành công!");
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra, vui lòng kiểm tra lại!");
            }
        }
    }

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
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}