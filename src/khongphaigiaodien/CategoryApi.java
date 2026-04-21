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
    public static List<CategoryDTO> getAllCategories() {
    List<CategoryDTO> list = new ArrayList<>();

    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:3000/api/categories"))
            .GET()
            .build();

        HttpResponse<String> res =
            client.send(req, HttpResponse.BodyHandlers.ofString());

        JSONArray arr = new JSONArray(res.body());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            list.add(new CategoryDTO(
                o.getInt("id"),
                o.getString("name")
            ));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}
    public static String getAllRaw() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/categories"))
                    .GET()
                    .build();

            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());

            return res.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean create(String name, String desc) {
        try {
            String json = """
            {
              "name": "%s",
              "description": "%s"
            }
            """.formatted(name, desc);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/categories"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());

            return res.statusCode() == 200 || res.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(int id, String name, String desc) {
        try {
            String json = """
            {
              "name": "%s",
              "description": "%s"
            }
            """.formatted(name, desc);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/categories/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());

            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/categories/" + id))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .DELETE()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());

            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
