/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import khongphaigiaodien.VendorApi;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class VendorJFrame extends JPanel{
    private JTable table;
    private DefaultTableModel model;

    public VendorJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- THANH CÔNG CỤ ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Thêm Mới");
        styleButton(btnAdd, new Color(0, 153, 76));
        btnAdd.addActionListener(e -> showVendorDialog(null));

        JButton btnEdit = new JButton("Sửa");
        styleButton(btnEdit, new Color(255, 140, 0));
        btnEdit.addActionListener(e -> handleEdit());

        JButton btnDelete = new JButton("Xóa");
        styleButton(btnDelete, new Color(220, 53, 69));
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnRefresh = new JButton("Làm mới");
        styleButton(btnRefresh, new Color(0, 102, 204));
        btnRefresh.addActionListener(e -> loadData());

        topPanel.add(btnAdd); topPanel.add(btnEdit); 
        topPanel.add(btnDelete); topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);

        // --- BẢNG HIỂN THỊ ---
        model = new DefaultTableModel(new String[]{"ID", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Địa Chỉ"}, 0);
        table = new JTable(model);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp để sửa!");
            return;
        }
        // Lấy dữ liệu từ dòng đang chọn
        JSONObject vendor = new JSONObject();
        vendor.put("id", model.getValueAt(row, 0));
        vendor.put("name", model.getValueAt(row, 1));
        vendor.put("phone", model.getValueAt(row, 2));
        vendor.put("address", model.getValueAt(row, 3));
        
        showVendorDialog(vendor);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa NCC này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (VendorApi.delete(id)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa!");
            }
        }
    }

    private void loadData() {
        JSONArray data = VendorApi.getAll();
        if (data == null) return;
        model.setRowCount(0);
        for (int i = 0; i < data.length(); i++) {
            JSONObject v = data.getJSONObject(i);
            model.addRow(new Object[]{
                v.getInt("id"), v.getString("name"), v.optString("phone", "---"), v.optString("address", "---")
            });
        }
    }

    private void showVendorDialog(JSONObject vendor) {
        JTextField txtName = new JTextField(vendor != null ? vendor.getString("name") : "", 20);
        JTextField txtPhone = new JTextField(vendor != null ? vendor.optString("phone", "") : "", 20);
        JTextField txtAddress = new JTextField(vendor != null ? vendor.optString("address", "") : "", 20);

        Object[] fields = {"Tên NCC:", txtName, "Số ĐT:", txtPhone, "Địa chỉ:", txtAddress};
        int result = JOptionPane.showConfirmDialog(this, fields, "Thông tin NCC", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            JSONObject body = new JSONObject();
            if (vendor != null) body.put("id", vendor.getInt("id"));
            body.put("name", txtName.getText());
            body.put("phone", txtPhone.getText());
            body.put("address", txtAddress.getText());

            if (VendorApi.save(body, vendor != null)) {
                loadData();
            }
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}