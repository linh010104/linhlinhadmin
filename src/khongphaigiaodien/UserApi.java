/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import adminlienketweb.AuthSession;
import api.UserDTO;
import java.net.http.*;
import java.net.URI;
import java.util.*;
import org.json.*;
/**
 *
 * @author AlinV
 */
public class UserApi {
    private static final String BASE_URL = ApiConfig.BASE_URL +"/users";

    public static List<UserDTO> getAll() {
        List<UserDTO> list = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + AuthSession.token)
                .GET()
                .build();

            HttpResponse<String> res =
                client.send(req, HttpResponse.BodyHandlers.ofString());

            JSONArray arr = new JSONArray(res.body());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                
                // --- ĐOẠN ĐÃ SỬA ---
                // Dùng optString thay cho getString để tránh lỗi khi dữ liệu là NULL
                // Nếu null, nó sẽ lấy giá trị mặc định là "" (chuỗi rỗng) hoặc "Chưa cập nhật"
                
                list.add(new UserDTO(
                    o.getInt("id"),
                    o.getString("username"),
                    o.optString("full_name", "Không tên"), // Tránh lỗi nếu full_name null
                    o.optString("email", ""),              // Tránh lỗi nếu email null
                    o.optString("phone", ""),              // Tránh lỗi nếu phone null
                    o.getString("role_name"),
                    o.getInt("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean changeStatus(int id, int status) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JSONObject body = new JSONObject();
            body.put("status", status);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/status"))
                .header("Authorization", "Bearer " + AuthSession.token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            client.send(req, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
