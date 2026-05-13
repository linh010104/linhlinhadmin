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

public class InventoryJFrame extends JPanel{
    private JTable table;
    private DefaultTableModel model;
    private boolean showLowStockOnly = false; // Biến cờ kiểm tra lọc

    public InventoryJFrame() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton btnImport = new JButton("Nhập Hàng Vào Kho");
        styleButton(btnImport, new Color(0, 153, 76)); 
        btnImport.addActionListener(e -> showImportChoiceDialog());

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

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                
                // Tô màu đỏ nếu tồn kho < 10
                int qty = Integer.parseInt(value.toString());
                if(qty < 10) c.setForeground(new Color(220, 53, 69)); 
                else c.setForeground(new Color(0, 102, 204));
                
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    // === THÊM MỚI: HÀM KHỞI TẠO TỪ DASHBOARD ===
    public InventoryJFrame(boolean showLowStockOnly) {
        this(); 
        this.showLowStockOnly = showLowStockOnly;
        if (showLowStockOnly) {
            loadData(); // Tải lại bảng với chế độ lọc
        }
    }

    private void loadData() {
        String json = InventoryApi.getAll();
        if (json == null) return;
        
        model.setRowCount(0);
        JSONArray arr = new JSONArray(json);
        
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            int qty = o.getInt("quantity");
            
            // Nếu cờ lọc bật và số lượng >= 10 thì bỏ qua không vẽ lên bảng
            if (showLowStockOnly && qty >= 10) continue; 
            
            String date = o.optString("updated_at", "Chưa cập nhật");
            if(date.contains("T")) date = date.replace("T", " ").substring(0, 16);
            
            model.addRow(new Object[]{ o.getInt("id"), o.getString("name"), o.optString("sku", "---"), qty, date });
        }
    }

    private void showImportChoiceDialog() {
        Object[] options = {"Nhập Thủ Công", "Quét Hóa Đơn AI"};
        int choice = JOptionPane.showOptionDialog(this, "Bạn muốn nhập kho bằng phương thức nào?", "Chọn phương thức nhập kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if (choice == 0) showManualImportDialog();
        else if (choice == 1) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ScanInvoiceDialog dialog = new ScanInvoiceDialog(parentFrame);
            dialog.setVisible(true);
            loadData();
        }
    }

    private void showManualImportDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 sản phẩm trên bảng!"); return; }

        String prodName = model.getValueAt(row, 1).toString();
        int prodId = (int) model.getValueAt(row, 0);
        String input = JOptionPane.showInputDialog(this, "Nhập số lượng thêm cho:\n" + prodName, "Nhập kho thủ công", JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.isEmpty()) {
            try {
                int amount = Integer.parseInt(input);
                if (amount <= 0) { JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!"); return; }
                if (InventoryApi.importGoods(prodId, amount)) {
                    JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                    loadData();
                } else JOptionPane.showMessageDialog(this, "Lỗi khi nhập kho!");
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên!"); }
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(230, 230, 230)); table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50)); header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }
}
