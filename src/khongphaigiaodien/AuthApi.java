/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
/**
 *
 * @author AlinV
 */
public class AuthApi {
    public static String login(String username, String password) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Đã thêm "clientType": "JAVA" để báo cho Backend biết đây là app Admin
            String json = """
            {
              "username": "%s",
              "password": "%s",
              "clientType": "JAVA"
            }
            """.formatted(username, password);

            // Đã đổi URL thành /api/auth/login (xóa chữ -admin)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://linhlinhstore.onrender.com/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
