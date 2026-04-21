/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import khongphaigiaodien.InventoryApi;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class InventoryJFrame extends JPanel{
    private JTable table;
    private DefaultTableModel model;

    public InventoryJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton btnImport = new JButton("Nhập Hàng Vào Kho");
        styleButton(btnImport, new Color(0, 153, 76)); // Xanh lá cây
        btnImport.addActionListener(e -> showImportChoiceDialog()); // CẬP NHẬT: Gọi menu chọn

        JButton btnRefresh = new JButton("Làm mới");
        styleButton(btnRefresh, Color.GRAY);
        btnRefresh.addActionListener(e -> loadData());

        topPanel.add(btnImport);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{ "ID", "Tên Sản Phẩm", "Mã SKU", "Tồn Kho", "Cập nhật lần cuối" });

        table = new JTable(model);
        styleTable(table);

        // Tô đậm cột Số lượng tồn kho
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                c.setForeground(new Color(0, 102, 204)); // Xanh đậm
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        String json = InventoryApi.getAll();
        if (json == null) return;
        
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            
            // Xử lý cẩn thận null pointer nếu ngày tháng bị trống
            String date = o.optString("updated_at", "Chưa cập nhật");
            if(date.contains("T")) date = date.replace("T", " ").substring(0, 16);
            
            model.addRow(new Object[]{
                o.getInt("id"),
                o.getString("name"),
                o.optString("sku", "---"),
                o.getInt("quantity"), // Lấy số lượng tồn kho mới
                date
            });
        }
    }

    // --- MỚI: MENU HIỂN THỊ LỰA CHỌN ---
    private void showImportChoiceDialog() {
        Object[] options = {"Nhập Thủ Công", "Quét Hóa Đơn AI"};
        int choice = JOptionPane.showOptionDialog(this,
                "Bạn muốn nhập kho bằng phương thức nào?",
                "Chọn phương thức nhập kho",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]); // Mặc định tô sáng nút AI

        if (choice == 0) {
            // Người dùng chọn nhập thủ công
            showManualImportDialog();
        } else if (choice == 1) {
            // Người dùng chọn nhập bằng AI
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ScanInvoiceDialog dialog = new ScanInvoiceDialog(parentFrame);
            dialog.setVisible(true);
            
            // TỰ ĐỘNG CẬP NHẬT LẠI BẢNG SAU KHI TẮT FORM AI
            loadData();
        }
    }

    // --- GIỮ NGUYÊN LOGIC NHẬP THỦ CÔNG CŨ ---
    private void showManualImportDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 sản phẩm trên bảng để nhập thủ công!");
            return;
        }

        String prodName = model.getValueAt(row, 1).toString();
        int prodId = (int) model.getValueAt(row, 0);

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng muốn thêm cho:\n" + prodName, "Nhập kho thủ công", JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.isEmpty()) {
            try {
                int amount = Integer.parseInt(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!");
                    return;
                }

                if (InventoryApi.importGoods(prodId, amount)) {
                    JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi nhập kho!");
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên!");
            }
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
