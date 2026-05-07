/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khongphaigiaodien;

import api.BannerDTO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
public class BannerApi {
    private static final String BASE_URL = "http://localhost:3000/api/banners";

    // 1. LẤY DANH SÁCH BANNER (Chuẩn style HttpClient & org.json của ông)
    public static List<BannerDTO> getBanners(String type) {
        List<BannerDTO> list = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + type))
                    .GET()
                    .build();
            
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(res.body());
            
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                BannerDTO b = new BannerDTO();
                b.setId(o.getInt("id"));
                b.setTitle(o.optString("title", ""));
                b.setImage_url(o.optString("image_url", ""));
                b.setLink_url(o.optString("link_url", ""));
                b.setBanner_type(o.optString("banner_type", ""));
                // status và sort_order nếu cần thì lấy thêm: o.optInt("status", 1);
                list.add(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. UPLOAD BANNER MỚI (Vẫn giữ HttpURLConnection vì gửi file Multipart tốn ít code hơn)
    public static boolean uploadBanner(File imageFile, String title, String linkUrl, String bannerType) {
        String boundary = "===" + System.currentTimeMillis() + "===";
        String LINE_FEED = "\r\n";
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = httpConn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {
                
                // Gửi thông tin Text
                addFormField(writer, boundary, "title", title);
                addFormField(writer, boundary, "link_url", linkUrl);
                addFormField(writer, boundary, "banner_type", bannerType);
                
                // Gửi File Ảnh
                writer.append("--").append(boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(imageFile.getName()).append("\"").append(LINE_FEED);
                writer.append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append(LINE_FEED);
                writer.append(LINE_FEED).flush();
                
                Files.copy(imageFile.toPath(), outputStream);
                outputStream.flush();
                
                writer.append(LINE_FEED).flush();
                writer.append("--").append(boundary).append("--").append(LINE_FEED).flush();
            }

            int status = httpConn.getResponseCode();
            return status == 200 || status == 201;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void addFormField(PrintWriter writer, String boundary, String name, String value) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
        writer.append("\r\n");
        writer.append(value != null ? value : "").append("\r\n");
        writer.flush();
    }
    public static boolean deleteBanner(int id) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
