package entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class User {
    private String id;
    private String ucode;
    private String username;
    private String password;

    private List<Role> rlist;

    public User() {
    }

    public User(String id, String ucode, String username, String password) {
        this.id = id;
        this.ucode = ucode;
        this.username = username;
        this.password = password;
    }
}
