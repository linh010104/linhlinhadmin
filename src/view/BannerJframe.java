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
    
    // Màu sắc chủ đạo theo giao diện của ông
    private final Color MAIN_COLOR = new Color(41, 128, 185); // Xanh dương
    private final Color BACKGROUND_COLOR = new Color(245, 246, 250);
    private final String IMAGE_BASE_URL = "http://localhost:3000";

    public BannerJframe() {
      
        setSize(1100, 700);
        initComponents();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
//        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

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
        cboType = new JComboBox<>(new String[]{"BIG_SLIDER", "SUB_BANNER"});
        cboType.setFont(new Font("Segoe UI", Font.PLAIN, 14));

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

        String[] cols = {"ID", "Tiêu đề", "Loại", "URL Ảnh"};
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
            if (BannerApi.uploadBanner(selectedImageFile, txtTitle.getText(), txtLink.getText(), cboType.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "Đã thêm banner!");
                loadTable();
            }
        });

        btnDelete.addActionListener(e -> {
            int row = tblBanner.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                if (BannerApi.deleteBanner(id)) {
                    loadTable();
                    lblPreview.setIcon(null);
                }
            }
        });

        tblBanner.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBanner.getSelectedRow() != -1) {
                String path = tableModel.getValueAt(tblBanner.getSelectedRow(), 3).toString();
                displayImage(IMAGE_BASE_URL + path, true);
            }
        });
        
        btnRefresh.addActionListener(e -> loadTable());
    }

    // Hàm tạo Button đẹp
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

    // Hàm tạo TextField đẹp
    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<BannerDTO> banners = BannerApi.getBanners("BIG_SLIDER");
        banners.addAll(BannerApi.getBanners("SUB_BANNER"));
        for (BannerDTO b : banners) {
            tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getBanner_type(), b.getImage_url()});
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
