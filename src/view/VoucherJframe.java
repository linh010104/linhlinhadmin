package view;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import api.CategoryDTO;
import khongphaigiaodien.CategoryApi;
import khongphaigiaodien.ProductApi;
import khongphaigiaodien.VoucherApi;

public class VoucherJframe extends JPanel {

    // Khai báo biến toàn cục
    private DefaultTableModel voucherModel;
    private DefaultTableModel productModel;
    
    private JTextField txtPrefix;
    private JTextField txtDiscount;
    private JTextField txtMinOrder;
    private JTextField txtPercent;
    private JComboBox<String> cbCategory;
    
    // TÍNH NĂNG MỚI: Cache dữ liệu và nút Chọn tất cả
    private JCheckBox chkSelectAll;
    private JSONArray allProductsCache;
    private List<CategoryDTO> listCategoryCache;

    public VoucherJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel pnlVoucher = taoPanelVoucher();
        JPanel pnlSanPham = taoPanelGiamGiaSanPham();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlVoucher, pnlSanPham);
        splitPane.setResizeWeight(0.5); 
        splitPane.setOneTouchExpandable(true); 
        splitPane.setDividerLocation(350); 

        add(splitPane, BorderLayout.CENTER);
        
        // Gọi API nạp dữ liệu
        loadDanhSachDanhMuc();
        loadDanhSachVoucher();
        loadDanhSachSanPham();
    }

    // =========================================================================
    // KHU VỰC 1: HỆ THỐNG MÃ VOUCHER
    // =========================================================================
    private JPanel taoPanelVoucher() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("1. QUẢN LÝ MÃ VOUCHER SỰ KIỆN"));

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        pnlTop.add(new JLabel("Tiền tố:"));
        txtPrefix = new JTextField(8); 
        pnlTop.add(txtPrefix);
        
        pnlTop.add(new JLabel("Tiền giảm:"));
        txtDiscount = new JTextField(8);
        pnlTop.add(txtDiscount);
        
        pnlTop.add(new JLabel("Đơn tối thiểu:"));
        txtMinOrder = new JTextField(8);
        pnlTop.add(txtMinOrder);
        
        JButton btnAdd = new JButton("Tạo Mã");
        pnlTop.add(btnAdd);

        panel.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Mã Voucher", "Tiền giảm", "Đơn tối thiểu", "Trạng thái"};
        voucherModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(voucherModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> taoMaVoucher());
        return panel;
    }

    // =========================================================================
    // KHU VỰC 2: HỆ THỐNG GIẢM GIÁ SẢN PHẨM
    // =========================================================================
    private JPanel taoPanelGiamGiaSanPham() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("2. CẬP NHẬT GIẢM GIÁ TRỰC TIẾP SẢN PHẨM"));

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        pnlTop.add(new JLabel("Danh mục:"));
        cbCategory = new JComboBox<>(); 
        pnlTop.add(cbCategory);
        
        pnlTop.add(new JLabel("Mức giảm (%):"));
        txtPercent = new JTextField(5);
        pnlTop.add(txtPercent);
        
        // 🔥 THÊM NÚT CHỌN TẤT CẢ VÀO ĐÂY 🔥
        chkSelectAll = new JCheckBox("Chọn tất cả SP");
        pnlTop.add(chkSelectAll);
        
        JButton btnApply = new JButton("Áp dụng cho SP đã chọn");
        btnApply.setBackground(new Color(40, 167, 69));
        btnApply.setForeground(Color.WHITE);
        pnlTop.add(btnApply);

        panel.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"Chọn", "Mã SP", "Tên Sản Phẩm", "Giá Gốc", "% Giảm", "Giá Sau Giảm"};
        productModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        
        JTable table = new JTable(productModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= SỰ KIỆN MỚI =================
        
        // 1. Click vào "Chọn tất cả" sẽ tick toàn bộ Checkbox
        chkSelectAll.addActionListener(e -> {
            boolean isChecked = chkSelectAll.isSelected();
            for (int i = 0; i < productModel.getRowCount(); i++) {
                productModel.setValueAt(isChecked, i, 0);
            }
        });
        
        // 2. Chuyển đổi Danh mục (ComboBox) sẽ lọc lại bảng
        cbCategory.addActionListener(e -> {
            if (cbCategory.getSelectedIndex() == -1 || allProductsCache == null) return;
            
            int selectedIndex = cbCategory.getSelectedIndex();
            if (selectedIndex == 0) {
                hienThiSanPhamLenBang(-1); // Truyền -1 là hiện Tất cả
            } else {
                // Index 0 là "Tất cả", nên List Danh mục bị lệch đi 1 so với ComboBox
                CategoryDTO selectedCat = listCategoryCache.get(selectedIndex - 1);
                hienThiSanPhamLenBang(selectedCat.getId());
            }
        });

        // 3. Sự kiện Áp dụng giảm giá
        btnApply.addActionListener(e -> apDungGiamGiaSP());

        return panel;
    }

    // =========================================================================
    // KHU VỰC 3: LOGIC VÀ KẾT NỐI API
    // =========================================================================
    
    private void taoMaVoucher() {
        String prefix = txtPrefix.getText().trim();
        String discountStr = txtDiscount.getText().trim();
        String minOrderStr = txtMinOrder.getText().trim();

        if (prefix.isEmpty() || discountStr.isEmpty() || minOrderStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        try {
            int discount = Integer.parseInt(discountStr);
            int minOrder = Integer.parseInt(minOrderStr);

            boolean isSuccess = VoucherApi.generateVoucher(prefix, discount, minOrder);

            if (isSuccess) {
                JOptionPane.showMessageDialog(this, "Tạo mã thành công!");
                txtPrefix.setText("");
                txtDiscount.setText("");
                txtMinOrder.setText("");
                loadDanhSachVoucher(); 
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi Server khi tạo mã!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tiền giảm và Đơn tối thiểu phải là số!");
        }
    }

    private void apDungGiamGiaSP() {
        String percentStr = txtPercent.getText().trim();
        if (percentStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập % giảm giá!");
            return;
        }

        try {
            int percent = Integer.parseInt(percentStr);
            int count = 0;

            for (int i = 0; i < productModel.getRowCount(); i++) {
                Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                if (isChecked != null && isChecked) {
                    String maSP = (String) productModel.getValueAt(i, 1);
                    
                    boolean success = ProductApi.updateDiscount(maSP, percent);
                    if (success) count++;
                }
            }

            if (count > 0) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật giảm giá cho " + count + " sản phẩm!");
                // Bỏ tick ô Chọn Tất cả và Tải lại bảng để xem giá mới
                chkSelectAll.setSelected(false);
                loadDanhSachSanPham(); 
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng tích chọn ít nhất 1 sản phẩm!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Phần trăm phải là số nguyên!");
        }
    }

    // --- HÀM LOAD DỮ LIỆU ---

    private void loadDanhSachDanhMuc() {
        cbCategory.removeAllItems();
        cbCategory.addItem("Tất cả"); 
        try {
            listCategoryCache = CategoryApi.getAllCategories(); 
            if (listCategoryCache != null) {
                for (CategoryDTO dm : listCategoryCache) {
                    cbCategory.addItem(dm.getName()); 
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi load Danh mục: " + e.getMessage());
        }
    }

    private void loadDanhSachVoucher() {
        voucherModel.setRowCount(0); 
        try {
            String rawJson = VoucherApi.getAllVouchers();
            if (rawJson != null && !rawJson.trim().isEmpty() && rawJson.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(rawJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject v = jsonArray.getJSONObject(i);
                    String id = v.has("id") ? String.valueOf(v.getInt("id")) : "N/A";
                    String code = v.has("code") ? v.getString("code") : "LỖI MÃ";
                    double discount = v.has("discount_amount") ? v.getDouble("discount_amount") : 0;
                    double minOrder = v.has("min_order_value") ? v.getDouble("min_order_value") : 0;
                    
                    String status = "Khả dụng";
                    if (v.has("is_used") && v.getInt("is_used") == 1) status = "Đã sử dụng";
                    
                    voucherModel.addRow(new Object[]{ id, code, String.format("%,.0f", discount), String.format("%,.0f", minOrder), status });
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi load Voucher: " + e.getMessage());
        }
    }

    // Nạp toàn bộ SP từ Database về bộ nhớ tạm (Cache) 1 lần duy nhất để lọc cho mượt
    private void loadDanhSachSanPham() {
        try {
            String rawJson = ProductApi.getAllProducts(); 
            if (rawJson != null && !rawJson.trim().isEmpty() && rawJson.trim().startsWith("[")) {
                allProductsCache = new JSONArray(rawJson);
                
                // Mặc định gọi hàm hiển thị với filter là -1 (Tất cả)
                hienThiSanPhamLenBang(-1);
            }
        } catch (Exception e) {
            System.out.println("Lỗi load Sản phẩm: " + e.getMessage());
        }
    }

    // Hàm dùng chung để đổ dữ liệu từ Cache lên Bảng, có kết hợp Lọc theo Category ID
    private void hienThiSanPhamLenBang(int filterCategoryId) {
        productModel.setRowCount(0); 
        chkSelectAll.setSelected(false); // Reset lại cái nút Chọn tất cả
        
        try {
            if (allProductsCache == null) return;
            
            for (int i = 0; i < allProductsCache.length(); i++) {
                JSONObject sp = allProductsCache.getJSONObject(i);
                
                // NẾU CÓ BỘ LỌC (Khác -1) VÀ SP NÀY KHÔNG THUỘC DANH MỤC ĐÓ -> BỎ QUA
                if (filterCategoryId != -1) {
                    if (!sp.has("category_id") || sp.getInt("category_id") != filterCategoryId) {
                        continue; 
                    }
                }
                
                String maSP = sp.has("id") ? String.valueOf(sp.getInt("id")) : "N/A"; 
                String tenSP = sp.has("name") ? sp.getString("name") : "Không tên"; 
                double giaGoc = sp.has("price") ? sp.getDouble("price") : 0;
                
                int phanTram = 0; 
                if (sp.has("discount_percent") && !sp.isNull("discount_percent")) {
                    phanTram = sp.getInt("discount_percent");
                }
                
                double giaMoi = giaGoc - (giaGoc * phanTram / 100);

                productModel.addRow(new Object[]{ 
                    false, maSP, tenSP, String.format("%,.0f", giaGoc), phanTram, String.format("%,.0f", giaMoi) 
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi render Bảng Sản phẩm: " + e.getMessage());
        }
    }
}