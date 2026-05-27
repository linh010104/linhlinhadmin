/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
/**
 *
 * @author AlinV
 */
public class VoucherApi {
    public static boolean generateVoucher(String prefix, int discountAmount, int minOrderValue) {
        try {
            String json = String.format(
                "{\"prefix\": \"%s\", \"discount_amount\": %d, \"min_order_value\": %d}", 
                prefix, discountAmount, minOrderValue
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/vouchers/generate"))
                    .header("Content-Type", "application/json") // Đảm bảo Node.js hiểu đây là JSON
                    // .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token) // Mở comment nếu Node.js bắt buộc có token
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Tạo Voucher Response: " + response.body()); // In ra console để sếp dễ debug
            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. LẤY DANH SÁCH
    public static String getAllVouchers() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/vouchers"))
                    // .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Load Voucher Response: " + response.body()); // In ra để xem Node.js trả về mảng hay trả về lỗi HTML
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
