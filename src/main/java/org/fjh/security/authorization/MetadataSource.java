package org.fjh.security.authorization;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 授权时所要采用的数据源
 */
@Component
public class MetadataSource implements
        FilterInvocationSecurityMetadataSource {

    // key 是url ， value 是对应url资源的角色列表
    private static Map<RequestMatcher, Collection<ConfigAttribute>> permissionMap;

    static {
        permissionMap = new LinkedHashMap<>();

        RequestMatcher rm1 = new AntPathRequestMatcher("/user/add") ;
        RequestMatcher rm2 = new AntPathRequestMatcher("/user/add") ;
        RequestMatcher rm3 = new AntPathRequestMatcher("/user/add") ;
        RequestMatcher rm4 = new AntPathRequestMatcher("/user/add") ;

        Collection<ConfigAttribute> c1= new ArrayList<>();
        Collection<ConfigAttribute> c2= new ArrayList<>();
        Collection<ConfigAttribute> c3= new ArrayList<>();
        Collection<ConfigAttribute> c4= new ArrayList<>();

        c1.add(new SecurityConfig("ROLE_ADMIN"));
        c1.add(new SecurityConfig("ROLE_MANAGER"));
        c1.add(new SecurityConfig("ROLE_JACK"));

        permissionMap.put(rm1,c1);

        c2.add(new SecurityConfig("ROLE_ADMIN"));
        c2.add(new SecurityConfig("ROLE_JACK"));

        permissionMap.put(rm2,c2);
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        //取得请求对象
        HttpServletRequest request = ((FilterInvocation)object).getRequest();
        //取所请求对象所对应的角色
        for (  Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry: permissionMap.entrySet() )
        {
            if( entry.getKey().equals(request)) {
                if (entry.getValue().size() > 0)
                    return entry.getValue();
            }
        }
        return  null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return new ArrayList<>();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
