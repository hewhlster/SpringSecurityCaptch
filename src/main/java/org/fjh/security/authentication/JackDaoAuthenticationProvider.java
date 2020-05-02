package org.fjh.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 认证器
 */
@Component
public class JackDaoAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    JackUserServiceImpl jackUserService;
    @Autowired
    Md5PasswordEncoder md5PasswordEncoder;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username=authentication.getName();//得到要认证的用户名
        String credentials=authentication.getCredentials().toString();//取得凭证
        UserDetails userDetails=jackUserService.loadUserByUsername(username);

        if( userDetails==null){
            throw new UsernameNotFoundException("找不到此用户");
        }

        if( !md5PasswordEncoder.matches(userDetails.getPassword(),credentials))
            throw new BadCredentialsException("凭证无效");

        //取得用户的授权
        Collection<? extends GrantedAuthority> authorities= userDetails.getAuthorities();
        //创建认证令牌
        Authentication ret = new UsernamePasswordAuthenticationToken(userDetails,credentials,authorities);


        return ret;
    }

    @Override
    public boolean supports(Class<?> authentication) {
         return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
