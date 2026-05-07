/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author AlinV
 */
public class CategoryBrandJframe extends JPanel{
    private JTabbedPane tabbedPane;

    public CategoryBrandJframe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Khởi tạo JTabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // 2. Thêm Tab Danh mục (Dùng lại file cũ của ông)
        // Lưu ý: CategoryJframe của ông đang là JPanel nên add vào đây rất mượt
        CategoryJframe categoryTab = new CategoryJframe();
        tabbedPane.addTab(" Danh mục ", new ImageIcon(), categoryTab);

        // 3. Thêm Tab Thương hiệu (File tui vừa viết cho ông)
        BrandJframe brandTab = new BrandJframe();
        tabbedPane.addTab(" Thương hiệu ", new ImageIcon(), brandTab);

        BannerJframe bannerTab = new BannerJframe();
        tabbedPane.addTab(" Banner ", new ImageIcon(), bannerTab);
        // 4. Style cho JTabbedPane (Tùy chỉnh để tiệp màu với UI của ông)
        tabbedPane.setBackground(Color.WHITE);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
}
