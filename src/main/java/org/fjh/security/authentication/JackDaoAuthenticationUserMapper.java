package org.fjh.security.authentication;

import entity.Role;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JackDaoAuthenticationUserMapper  {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public User findUserByUname(String uname){

        String sql ="select *from tbl_user where username=?";

        List<User> list=jdbcTemplate.query(sql, new Object[]{uname}, new RowMapper<User>() {

            @Override
            public User mapRow(ResultSet rs, int i) throws SQLException {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setUcode(rs.getString("ucode"));
                user.setPassword(rs.getString("password"));
                return user;            }
        });

        return  list!=null && list.size()>0? list.get(0):null;
    }


    public List findRolesByUcode(String code) {
        String sql="SELECT " +
                "tbl_role.* " +
                "FROM tbl_user,tbl_user_role,tbl_role " +
                "WHERE tbl_user.`id`=tbl_user_role.`uid` " +
                "AND tbl_role.`id`=tbl_user_role.`rid` " +
                "AND tbl_user.`ucode`=?";

        List<Role> roles=jdbcTemplate.query(sql,
                new Object[]{code},
                new RowMapper<Role>() {
                    @Override
                    public Role mapRow(ResultSet rs, int i) throws SQLException {
                        Role role = new Role();
                        role.setId(rs.getString("id"));
                        role.setRname(rs.getString("rname"));
                        role.setRtag(rs.getString("rtag"));
                        role.setRmemo(rs.getString("rmemo"));
                        return role;
                    }
                }
        );

        return roles;
    }

}
