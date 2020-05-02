package entity;

import lombok.Data;

import java.util.List;

@Data
public class Permission {
    String id;
    String pname;
    String purl;
    String pmemo;
    List<Role> rlist;

}
