/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class VendorApi {
    private static final String BASE_URL = "https://linhlinhstore.onrender.com/api/vendors";

    // Lấy toàn bộ danh sách NCC
    public static JSONArray getAll() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return new JSONArray(response);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }
    public static boolean delete(int id) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
    } catch (Exception e) { return false; }
}

    public static boolean save(JSONObject vendor, boolean isUpdate) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = isUpdate ? BASE_URL + "/" + vendor.getInt("id") : BASE_URL;
            
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            String jsonBody = vendor.toString();
            if (isUpdate) builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
            else builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }
}

