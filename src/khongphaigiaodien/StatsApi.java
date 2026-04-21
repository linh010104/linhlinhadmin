/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class StatsApi {
    // Hàm gọi API lấy doanh thu từ Node.js
    public static JSONObject getRevenue(String startDate, String endDate) {
        try {
            // URL API (Lưu ý: startDate và endDate phải có định dạng yyyy-mm-dd)
            String urlString = "http://localhost:3000/api/stats/revenue?startDate=" + startDate + "&endDate=" + endDate;
            URL url = new URL(urlString);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Nếu lỗi kết nối hoặc server lỗi
            if (conn.getResponseCode() != 200) {
                return null;
            }

            // Đọc dữ liệu trả về
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // Chuyển chuỗi JSON thành đối tượng JSONObject
            return new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
