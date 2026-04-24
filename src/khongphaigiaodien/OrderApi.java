/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import adminlienketweb.AuthSession;
import api.OrderDTO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class OrderApi {
    public static List<OrderDTO> getAllOrders() {
        List<OrderDTO> list = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/orders"))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                JSONArray arr = new JSONArray(res.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    list.add(new OrderDTO(
                            o.getInt("id"),
                            o.optString("receiver_name", "N/A"),
                            o.optString("phone", "N/A"),
                            o.optString("address", "N/A"),
                            o.getDouble("total_amount"),
                            o.getString("status"),
                            o.optString("payment_method", "COD"),
                            o.optString("created_at", "")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean updateStatus(int orderId, String newStatus) {
        try {
            String json = "{ \"status\": \"" + newStatus + "\" }";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/orders/" + orderId + "/status"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Thêm hàm này vào dưới cùng của OrderApi.java
    public static boolean returnOrder(int orderId, String reason, String condition) {
        try {
            JSONObject json = new JSONObject();
            json.put("reason", reason);
            json.put("condition", condition); // "GOOD" hoặc "BAD"

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/orders/" + orderId + "/return"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
