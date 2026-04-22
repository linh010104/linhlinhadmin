/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author AlinV
 */
public class CategoryDTO {
    private int id;
    private String name;
    private String description;

    public CategoryDTO(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public CategoryDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }
    @Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    CategoryDTO other = (CategoryDTO) obj;
    return this.id == other.id; // So sánh dựa trên ID danh mục
}

@Override
public int hashCode() {
    return java.util.Objects.hash(id);
}

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name;
    }
}
