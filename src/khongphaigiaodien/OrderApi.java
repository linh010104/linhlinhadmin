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
                    .uri(URI.create(ApiConfig.BASE_URL + "/orders/admin/all")) // Gọi đúng API của admin
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
                    .uri(URI.create(ApiConfig.BASE_URL + "/orders/" + orderId + "/admin-status"))
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

    public static boolean processReturn(int orderId, String action, String adminNote, String condition) {
        try {
            JSONObject body = new JSONObject();
            body.put("action", action);
            body.put("adminNote", adminNote);
            body.put("condition", condition);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/orders/" + orderId + "/process-return"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getOrderReturnReason(int orderId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/orders/" + orderId))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                JSONObject obj = new JSONObject(res.body());
                return obj.optString("return_reason", "Không có lý do từ khách");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Không tải được lý do";
    }
}
