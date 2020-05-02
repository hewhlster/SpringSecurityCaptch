package org.fjh.security.authentication;

import entity.Role;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class JackUserServiceImpl implements UserDetailsService {

    @Autowired
    JackDaoAuthenticationUserMapper jackDaoAuthenticationUserMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User ret= jackDaoAuthenticationUserMapper.findUserByUname(username);

        if( ret==null)
            throw new UsernameNotFoundException("找不到此用户");
        //构建用户角色信息
       /* Collection< GrantedAuthority> authorities= new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));*/

       List<Role> roles=jackDaoAuthenticationUserMapper.findRolesByUcode(ret.getUcode());
        Collection< GrantedAuthority> authorities= new ArrayList<>();
       for (Role role:roles){
           authorities.add(new SimpleGrantedAuthority(role.getRtag()));
       }
        //创建一个UserDetails对象
        JackUserDetails jackUserDetails = new JackUserDetails(ret.getUsername(),ret.getPassword(),authorities);
        return jackUserDetails;
    }
}
