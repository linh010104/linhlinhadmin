/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import adminlienketweb.AuthSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
/**
 *
 * @author AlinV
 */
public class BrandApi {
    private static final String BASE_URL = "https://linhlinhstore.onrender.com/api/brands";

    public static String getAllRaw() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
            return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) { return "[]"; }
    }

    public static boolean create(String name, String country) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("country", country);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token) // Dùng token y như CategoryApi
                    .POST(HttpRequest.BodyPublishers.ofString(obj.toString())).build();
            return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).statusCode() < 300;
        } catch (Exception e) { return false; }
    }

    public static boolean update(int id, String name, String country) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("country", country);

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
