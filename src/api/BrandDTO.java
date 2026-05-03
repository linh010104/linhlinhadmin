/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author AlinV
 */
public class BrandDTO {
    private int id;
    private String name;
    private String country;

    public BrandDTO(int id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }

    @Override
    public String toString() { return name; }
}
