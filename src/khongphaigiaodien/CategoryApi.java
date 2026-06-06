/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;

import api.CategoryDTO;
import adminlienketweb.AuthSession;
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
public class CategoryApi {
    private static final String BASE_URL = ApiConfig.BASE_URL +"/categories";

    public static List<CategoryDTO> getAllCategories() {
        List<CategoryDTO> list = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(res.body());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new CategoryDTO(o.getInt("id"), o.getString("name")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static String getAllRaw() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) { return "[]"; }
    }

    public static boolean create(String name, String desc) {
        return create(name, desc, null);
    }

    public static boolean create(String name, String desc, Integer parentId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("description", desc);
            obj.put("parent_id", parentId);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .POST(HttpRequest.BodyPublishers.ofString(obj.toString())).build();
            return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).statusCode() < 300;
        } catch (Exception e) { return false; }
    }

    public static boolean update(int id, String name, String desc) {
        return update(id, name, desc, null);
    }

    public static boolean update(int id, String name, String desc, Integer parentId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("description", desc);
            obj.put("parent_id", parentId);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(obj.toString())).build();
            return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }

    // 🔥 HÀM MỚI ĐÃ SỬA ĐỔI ĐỂ LẤY CÂU CHỬI TỪ NODE.JS
    public static String delete(int id) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .DELETE().build();
            
            HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            
            // Ép nó vào định dạng JSON tự chế để nhả lên Giao diện đọc
            JSONObject result = new JSONObject();
            result.put("statusCode", res.statusCode());
            
            if (res.statusCode() == 200) {
                result.put("success", true);
            } else {
                result.put("success", false);
                // Cố gắng bóc tách câu lỗi do Node.js trả về
                try {
                    JSONObject errObj = new JSONObject(res.body());
                    result.put("message", errObj.optString("message", "Lỗi không xác định từ Server!"));
                } catch(Exception parseEx) {
                    result.put("message", "Lỗi Server (Mã: " + res.statusCode() + ")");
                }
            }
            return result.toString();
        } catch (Exception e) { 
            return "{\"success\":false, \"message\":\"Lỗi mất kết nối mạng!\"}"; 
        }
    }
}