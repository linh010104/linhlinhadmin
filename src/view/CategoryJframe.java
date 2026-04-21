    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

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
        setBackground(Color.WHITE); // Nền trắng

        // --- 1. HEADER (CÁC NÚT BẤM) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton btnAdd = new JButton("Thêm Mới");
        styleButton(btnAdd, new Color(0, 153, 76)); // Xanh lá

        JButton btnEdit = new JButton("Sửa Đổi");
        styleButton(btnEdit, new Color(255, 153, 51)); // Cam

        JButton btnDelete = new JButton("Xóa");
        styleButton(btnDelete, new Color(220, 53, 69)); // Đỏ

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{ "ID", "Tên danh mục", "Mô tả" });

        table = new JTable(model);
        styleTable(table); // Áp dụng giao diện đẹp cho bảng

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(scrollPane, BorderLayout.CENTER);

        loadCategories();
    }

    // --- CÁC HÀM LOGIC (GIỮ NGUYÊN) ---
    private void loadCategories() {
        String json = CategoryApi.getAllRaw();
        if (json == null || json.length() < 2) return;
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject c = arr.getJSONObject(i);
            model.addRow(new Object[]{ c.getInt("id"), c.getString("name"), c.getString("description") });
        }
    }

    private void showAddDialog() {
        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        Object[] form = { "Tên danh mục:", txtName, "Mô tả:", txtDesc };
        if (JOptionPane.showConfirmDialog(this, form, "Thêm danh mục", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (CategoryApi.create(txtName.getText(), txtDesc.getText())) {
                JOptionPane.showMessageDialog(this, "Thêm thành công");
                loadCategories();
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn danh mục cần sửa"); return; }
        int id = (int) model.getValueAt(row, 0);
        JTextField txtName = new JTextField(model.getValueAt(row, 1).toString());
        JTextField txtDesc = new JTextField(model.getValueAt(row, 2).toString());
        Object[] form = { "Tên danh mục:", txtName, "Mô tả:", txtDesc };
        if (JOptionPane.showConfirmDialog(this, form, "Sửa danh mục", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (CategoryApi.update(id, txtName.getText(), txtDesc.getText())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                loadCategories();
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn danh mục cần xóa"); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa danh mục này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (CategoryApi.delete(id)) {
                JOptionPane.showMessageDialog(this, "Đã xóa");
                loadCategories();
            }
        }
    }

    // --- HÀM TRANG TRÍ (STYLE) ---
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
