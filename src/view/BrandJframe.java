/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.JPanel;
import api.BrandDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import khongphaigiaodien.BrandApi;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class BrandJframe extends JPanel{
    private JTable table;
    private DefaultTableModel model;
    
    public BrandJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. HEADER (THANH CÔNG CỤ) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton btnAdd = new JButton("Thêm Mới");
        styleButton(btnAdd, new Color(0, 153, 76)); // Màu xanh lá giống CategoryJframe[cite: 4]
        btnAdd.addActionListener(e -> showDialog(null));

        JButton btnEdit = new JButton("Sửa Đổi");
        styleButton(btnEdit, new Color(255, 153, 51)); // Màu cam[cite: 4]
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một thương hiệu để sửa!");
                return;
            }
            JSONObject current = new JSONObject();
            current.put("id", model.getValueAt(row, 0));
            current.put("name", model.getValueAt(row, 1));
            current.put("country", model.getValueAt(row, 2));
            showDialog(current);
        });

        JButton btnDelete = new JButton("Xóa");
        styleButton(btnDelete, new Color(220, 53, 69)); // Màu đỏ[cite: 4]
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa thương hiệu này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (BrandApi.delete(id)) loadBrands();
            }
        });

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        // Cấu trúc bảng khớp với database brands: ID, Name, Country
        model = new DefaultTableModel(new String[]{"ID", "Tên thương hiệu", "Quốc gia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        styleTable(table); // Dùng lại hàm styleTable của ông[cite: 4]

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        loadBrands();
    }

    private void loadBrands() {
        String json = BrandApi.getAllRaw();
        if (json == null) return;
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject b = arr.getJSONObject(i);
            model.addRow(new Object[]{ 
                b.getInt("id"), 
                b.getString("name"), 
                b.optString("country", "---") 
            });
        }
    }

    private void showDialog(JSONObject data) {
        JTextField txtName = new JTextField(data != null ? data.getString("name") : "", 20);
        JTextField txtCountry = new JTextField(data != null ? data.getString("country") : "", 20);

        Object[] form = {
            "Tên thương hiệu:", txtName,
            "Quốc gia:", txtCountry
        };

        int result = JOptionPane.showConfirmDialog(this, form, 
                data == null ? "Thêm thương hiệu mới" : "Cập nhật thương hiệu", 
                JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            String country = txtCountry.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên thương hiệu không được để trống!");
                return;
            }

            boolean success;
            if (data == null) {
                success = BrandApi.create(name, country);
            } else {
                success = BrandApi.update(data.getInt("id"), name, country);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Thao tác thành công!");
                loadBrands();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra, vui lòng kiểm tra lại!");
            }
        }
    }

    // --- HÀM STYLE (Copy nguyên văn từ CategoryJframe của ông) ---[cite: 4]
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
