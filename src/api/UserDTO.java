/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author AlinV
 */
public class UserDTO {
    public int id;
    public String username;
    public String fullName;
    public String email;
    public String phone;
    public String roleName;
    public int status;

    public UserDTO(int id, String username, String fullName,
                   String email, String phone, String roleName, int status) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleName = roleName;
        this.status = status;
    }
}
