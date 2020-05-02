package org.fjh.security.authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class Md5PasswordEncoder implements PasswordEncoder {
    private String key;//秘钥
    @Override
    public String encode(CharSequence rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
       String password= DigestUtils.md5DigestAsHex(encodedPassword.getBytes());
        return rawPassword.equals(password);
    }
}
