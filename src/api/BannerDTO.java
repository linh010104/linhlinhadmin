/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author AlinV
 */
public class BannerDTO {
    private int id;
    private String title;
    private String image_url;
    private String link_url;
    private String banner_type;
    private int status;
    private int sort_order;
    private String created_at;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }
    
    public String getLink_url() { return link_url; }
    public void setLink_url(String link_url) { this.link_url = link_url; }
    
    public String getBanner_type() { return banner_type; }
    public void setBanner_type(String banner_type) { this.banner_type = banner_type; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public int getSort_order() { return sort_order; }
    public void setSort_order(int sort_order) { this.sort_order = sort_order; }
    
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

