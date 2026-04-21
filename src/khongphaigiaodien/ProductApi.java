/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;
import adminlienketweb.AuthSession;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
/**
 *
 * @author AlinV
 */
public class ProductApi {
     public static String getAllProducts() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/products"))
                    .header("Authorization", "Bearer " + AuthSession.token)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
     public static boolean createProduct(
        String name, String sku, double price, double importPrice, int stockQuantity,
        int warranty, int categoryId, String description, String specifications, int brandId, int status
    ) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = """
            {
              "name": "%s",
              "sku": "%s",
              "price": %.2f,
              "import_price": %.2f,
              "stock_quantity": %d,
              "warranty_month": %d,
              "category_id": %d,
              "description": "%s",
              "specifications": "%s",
              "brand_id": %d,
              "status": %d
            }
            """.formatted(name, sku, price, importPrice, stockQuantity, warranty, categoryId, description, specifications, brandId, status);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/products"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateProduct(
            int id, String name, String sku, double price, double importPrice, int stockQuantity, 
            int warranty, int categoryId, String description, String specifications, int brandId, int status) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = """
            {
              "name": "%s",
              "sku": "%s",
              "price": %.2f,
              "import_price": %.2f,
              "stock_quantity": %d,
              "warranty_month": %d,
              "category_id": %d,
              "description": "%s",
              "specifications": "%s",
              "brand_id": %d,
              "status": %d
            }
            """.formatted(name, sku, price, importPrice, stockQuantity, warranty, categoryId, description, specifications, brandId, status);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/products/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminlienketweb.AuthSession.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

public static boolean deleteProduct(int id) {
    try {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/api/products/" + id))
                .header("Authorization", "Bearer " + AuthSession.token)
                .DELETE()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.statusCode() == 200;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
public static boolean uploadImage(int productId, File file) {
    try {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        URL url = new URL("http://localhost:3000/api/product-images/" + productId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream out = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"")
              .append(file.getName()).append("\"\r\n");
        writer.append("Content-Type: image/jpeg\r\n\r\n");
        writer.flush();

        Files.copy(file.toPath(), out);
        out.flush();

        writer.append("\r\n").flush();
        writer.append("--").append(boundary).append("--\r\n");
        writer.close();

        return conn.getResponseCode() == 200;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


}
