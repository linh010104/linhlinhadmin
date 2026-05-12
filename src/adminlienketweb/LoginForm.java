/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adminlienketweb;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import khongphaigiaodien.AuthApi;
import com.formdev.flatlaf.FlatLightLaf; 
import view.menu;

/**
 *
 * @author AlinV
 */
public class LoginForm extends JFrame {
    JLabel lbllogin = new JLabel("Tên đăng nhập:");
    JLabel lblpass = new JLabel("Mật khẩu:");
    JTextField txtUsername = new JTextField(20);
    JPasswordField txtPassword = new JPasswordField(20);
    JButton btnLogin = new JButton("Đăng nhập");
    JButton btncancel = new JButton("Hủy");

    public LoginForm() {
        setTitle("Đăng nhập hệ thống Admin");
        this.setSize(600, 400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        flowGUI();
        btncancel.addActionListener(e -> cancel());
        this.setVisible(true);

        btnLogin.addActionListener(this::handleLogin);
    }
    
    public void flowGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Chữ to hơn xíu cho đẹp
        lblTitle.setForeground(new Color(0, 102, 204));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lbllogin, gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblpass, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnLogin.setBackground(new Color(0, 153, 76));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Font chữ đẹp cho nút
        
        btncancel.setBackground(new Color(204, 0, 0));
        btncancel.setForeground(Color.WHITE);
        btncancel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btncancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setContentPane(mainPanel);
    }
    

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        String response = AuthApi.login(username, password);

        if (response == null || !response.contains("token")) {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu");
            return;
        }

        // ===== PARSE TOKEN =====
        try {
            org.json.JSONObject obj = new org.json.JSONObject(response);
            String token = obj.getString("token");

            // LƯU TOKEN DÙNG CHUNG
            AuthSession.token = token;

            // Xóa thông báo đăng nhập thành công cho mượt (vào thẳng app)
            this.dispose();

            // MỞ FRAME QUẢN LÝ
            new menu().setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xử lý đăng nhập");
        }
    }

    private void cancel(){
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn thoát không?",
            "Xác nhận thoát",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }
    
    // ========================================================
    // ĐÂY LÀ CHỖ KÍCH HOẠT FLATLAF CHO TOÀN BỘ APP
    // ========================================================
    public static void main(String[] args) {
        // Cài đặt giao diện FlatLightLaf trước khi khởi động Form
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Làm cho các nút bo tròn nhẹ đẹp hơn
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
