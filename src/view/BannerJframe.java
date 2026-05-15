/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import api.BannerDTO;
import khongphaigiaodien.BannerApi;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
/**
 *
 * @author AlinV
 */
public class BannerJframe extends JPanel{
   private JTextField txtTitle, txtLink;
    private JComboBox<String> cboType;
    private JLabel lblPreview;
    private JTable tblBanner;
    private DefaultTableModel tableModel;
    private File selectedImageFile = null;
    
    // Bản đồ ánh xạ giữa Chữ hiển thị và Mã lưu DB
    private HashMap<String, String> bannerTypeMap = new HashMap<>();
    
    // Màu sắc chủ đạo theo giao diện của ông
    private final Color MAIN_COLOR = new Color(41, 128, 185); // Xanh dương
    private final Color BACKGROUND_COLOR = new Color(245, 246, 250);
    private final String IMAGE_BASE_URL = "http://localhost:3000";

    public BannerJframe() {
        setSize(1100, 700);
        initComponents();
        loadTable(); // Tải bảng sau khi đã load xong Combobox
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));

        // --- 1. Tiêu đề Trang ---
        JLabel lblHeader = new JLabel("QUẢN LÝ BANNER QUẢNG CÁO");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(new Color(44, 62, 80));
        add(lblHeader, BorderLayout.NORTH);

        // --- 2. Panel bên trái: Nhập liệu & Preview ---
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1), 
            new EmptyBorder(15, 15, 15, 15)));
        pnlLeft.setPreferredSize(new Dimension(350, 0));

        // Preview Area
        lblPreview = new JLabel("Chưa chọn ảnh/Preview", SwingConstants.CENTER);
        lblPreview.setBorder(new LineBorder(new Color(230, 230, 230), 2));
        lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPreview.setMaximumSize(new Dimension(320, 180));
        lblPreview.setPreferredSize(new Dimension(320, 180));

        JButton btnChoose = createStyledButton("Chọn ảnh từ máy", new Color(52, 152, 219));
        btnChoose.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form Fields
        txtTitle = createStyledTextField();
        txtLink = createStyledTextField();
        
        // Khởi tạo Combobox và nạp dữ liệu động vào
        cboType = new JComboBox<>();
        cboType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadComboTypes(); 

        pnlLeft.add(new JLabel("Xem trước Banner:"));
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlLeft.add(lblPreview);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlLeft.add(btnChoose);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlLeft.add(new JLabel("Tiêu đề quảng cáo:"));
        pnlLeft.add(txtTitle);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlLeft.add(new JLabel("Đường dẫn (URL):"));
        pnlLeft.add(txtLink);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlLeft.add(new JLabel("Vị trí hiển thị:"));
        pnlLeft.add(cboType);
        pnlLeft.add(Box.createVerticalGlue());

        // Nút hành động
        JPanel pnlActions = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlActions.setBackground(Color.WHITE);
        JButton btnAdd = createStyledButton("THÊM MỚI", new Color(46, 204, 113));
        JButton btnDelete = createStyledButton("XÓA CHỌN", new Color(231, 76, 60));
        JButton btnRefresh = createStyledButton("LÀM MỚI", new Color(149, 165, 166));
        
        pnlActions.add(btnAdd);
        pnlActions.add(btnDelete);
        pnlActions.add(btnRefresh);
        pnlLeft.add(pnlActions);

        String[] cols = {"ID", "Tiêu đề", "Vị trí", "URL Ảnh"};
        tableModel = new DefaultTableModel(cols, 0);
        tblBanner = new JTable(tableModel);
        
        JTableHeader header = tblBanner.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(MAIN_COLOR);
        header.setForeground(Color.WHITE);
        tblBanner.setRowHeight(30);
        tblBanner.setSelectionBackground(new Color(232, 244, 253));

        JScrollPane scrollPane = new JScrollPane(tblBanner);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        // Gộp vào Layout chính
        add(pnlLeft, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        // --- EVENT HANDLING ---
        btnChoose.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg", "gif");
            fc.setFileFilter(filter);
            
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fc.getSelectedFile();
                displayImage(selectedImageFile.getAbsolutePath(), false);
            }
        });

        btnAdd.addActionListener(e -> {
            if (selectedImageFile == null) {
                JOptionPane.showMessageDialog(this, "Hãy chọn ảnh trước!");
                return;
            }
            
            // Lấy chữ trên Combobox và dò ra mã ngầm định (Ví dụ: "Banner Dọc - Laptop" -> "VERTICAL_CATEGORY_1")
            String selectedDisplay = cboType.getSelectedItem().toString();
            String dbBannerType = bannerTypeMap.get(selectedDisplay);
            
            // Lấy tiêu đề, nếu để trống thì lấy luôn chữ của Combobox làm tiêu đề cho đỡ trống Database
            String title = txtTitle.getText().isEmpty() ? selectedDisplay : txtTitle.getText();

            if (BannerApi.uploadBanner(selectedImageFile, title, txtLink.getText(), dbBannerType)) {
                JOptionPane.showMessageDialog(this, "Đã thêm banner thành công!");
                txtTitle.setText("");
                txtLink.setText("");
                selectedImageFile = null;
                lblPreview.setIcon(null);
                lblPreview.setText("Chưa chọn ảnh/Preview");
                loadTable();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm banner!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = tblBanner.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa banner này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && BannerApi.deleteBanner(id)) {
                    loadTable();
                    lblPreview.setIcon(null);
                    lblPreview.setText("Chưa chọn ảnh/Preview");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một banner trên bảng để xóa!");
            }
        });

        tblBanner.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBanner.getSelectedRow() != -1) {
                String path = tableModel.getValueAt(tblBanner.getSelectedRow(), 3).toString();
                displayImage(IMAGE_BASE_URL + path, true);
            }
        });
        
        btnRefresh.addActionListener(e -> {
            loadComboTypes(); // Tải lại phòng khi admin vừa thêm Danh mục mới
            loadTable();
        });
    }

    // ==========================================================
    // HÀM MỚI: TẢI DANH MỤC TỪ API VÀ ĐỔ VÀO COMBOBOX & HASHMAP
    // ==========================================================
    private void loadComboTypes() {
        cboType.removeAllItems();
        bannerTypeMap.clear();

        // 1. Thêm 2 banner tĩnh gốc
        String textBig = "Slider Lớn (Trang chủ)";
        cboType.addItem(textBig);
        bannerTypeMap.put(textBig, "BIG_SLIDER");

        String textSub = "Banner Phụ (Ngang)";
        cboType.addItem(textSub);
        bannerTypeMap.put(textSub, "SUB_BANNER");

        // 2. Gọi API lấy danh mục để làm Banner Dọc tự động
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(IMAGE_BASE_URL + "/api/categories"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(res.body());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                // Lọc chỉ lấy danh mục cha (parent_id là null)
                if (cat.isNull("parent_id")) {
                    int id = cat.getInt("id");
                    String name = cat.getString("name");

                    String displayText = "Banner Dọc - " + name;
                    String dbValue = "VERTICAL_CATEGORY_" + id;

                    cboType.addItem(displayText);
                    bannerTypeMap.put(displayText, dbValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi tải danh mục cho Combobox Banner");
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    // ==========================================================
    // HÀM SỬA LẠI: DUYỆT QUA HASHMAP ĐỂ TẢI HẾT TẤT CẢ CÁC LOẠI BANNER
    // ==========================================================
    private void loadTable() {
        tableModel.setRowCount(0);
        
        // Lấy danh sách các mã (BIG_SLIDER, SUB_BANNER, VERTICAL_CATEGORY_1...) từ bản đồ
        Set<String> uniqueTypes = new HashSet<>(bannerTypeMap.values());
        
        for (String dbType : uniqueTypes) {
            List<BannerDTO> banners = BannerApi.getBanners(dbType);
            for (BannerDTO b : banners) {
                // Dịch mã DB ngược lại thành tiếng Việt để hiện lên bảng
                String friendlyName = dbType;
                for (String key : bannerTypeMap.keySet()) {
                    if (bannerTypeMap.get(key).equals(dbType)) {
                        friendlyName = key;
                        break;
                    }
                }
                tableModel.addRow(new Object[]{b.getId(), b.getTitle(), friendlyName, b.getImage_url()});
            }
        }
    }

    private void displayImage(String path, boolean isUrl) {
        try {
            if (lblPreview.getWidth() <= 0 || lblPreview.getHeight() <= 0) return;
            Image img;
            if (isUrl) {
                img = new ImageIcon(new URL(path)).getImage();
            } else {
                img = new ImageIcon(path).getImage();
            }
            Image scaled = img.getScaledInstance(lblPreview.getWidth(), lblPreview.getHeight(), Image.SCALE_SMOOTH);
            lblPreview.setIcon(new ImageIcon(scaled));
            lblPreview.setText("");
        } catch (Exception e) {
            lblPreview.setIcon(null);
            lblPreview.setText("Lỗi load ảnh");
        }
    }
}
