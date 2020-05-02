package entity;

import lombok.Data;

import java.util.List;

@Data
public class Role {
    String id;
    String rname;
    String rtag;
    String rmemo;
    List<Permission> plist;

}
