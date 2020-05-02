package captha.util;

import java.util.HashMap;
import java.util.Map;

public class JedisUtils {

    static Map<String,Map<String, Object>> map = new HashMap<>();
    public static Map<String, Object> getObjectMap(String key){
        return map.get(key);
    }

    public static void setObjectMap(String key,Map<String, Object> object,Integer time){
        map.put(key,object);
    }
}
