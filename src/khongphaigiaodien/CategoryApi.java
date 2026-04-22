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
    private static final String BASE_URL = "http://localhost:3000/api/categories";

    // --- HÀM QUAN TRỌNG: Để file ProductJframe.java hết báo lỗi ---
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

    // Lấy dữ liệu thô (Raw) cho bảng Danh mục
    public static String getAllRaw() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) { return "[]"; }
    }

    // Hỗ trợ tạo danh mục (Bản cũ 2 tham số - giúp các file khác ko lỗi)
    public static boolean create(String name, String desc) {
        return create(name, desc, null);
    }

    // Hỗ trợ tạo danh mục (Bản mới 3 tham số - có Parent ID)
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

    // Hỗ trợ cập nhật (Bản cũ 3 tham số)
    public static boolean update(int id, String name, String desc) {
        return update(id, name, desc, null);
    }

    // Hỗ trợ cập nhật (Bản mới 4 tham số)
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

    public static boolean delete(int id) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .DELETE().build();
            return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }
}