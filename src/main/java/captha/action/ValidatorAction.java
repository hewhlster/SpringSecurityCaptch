package captha.action;


import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import captha.util.*;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 滑动验证码原理
 *
 * 1.服务器存有原始图片、抠图模板、抠图边框等图片
 * 2.请求获取验证码，服务器随机获取一张图片，根据抠图模板图片在原图中随机生成x, y轴的矩形感兴趣区域
 * 3.再通过抠图模板在感兴趣的区域图片中抠图，这里会产生一张小块的验证滑块图
 * 4.验证滑块图再通过抠图边框进行颜色处理，生成带有描边的新的验证滑块图
 * 5.原图再根据抠图模板做颜色处理，这里会产生一张遮罩图（缺少小块的目标图）
 * 6.到这里可以得到三张图，一张原图，一张遮罩图。将这三张图和抠图的y轴坐标通过base64加密，返回给前端，并将验证的抠图位置的x轴、y轴存放在session、db、nosql中
 * 7.前端在移动方块验证时，将移动后的x轴和y轴坐标传递到后台与原来的x坐标和y轴坐标作比较，如果在阈值内则验证通过，验证通过后可以是给提示或者显示原图
 * 8.后端可以通过token、session、redis等方式取出存放的x轴和y轴坐标数据，与用户滑动的x轴和y轴进行对比验证
 *
 *
 */
@RestController
@RequestMapping("validator")
public class ValidatorAction {
// https://blog.csdn.net/YTenderness/article/details/99969355?depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-5&utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-5
   private Logger log = LoggerFactory.getLogger(ValidatorAction.class);

    /**
     * 生成验证码
     *
     * @return
     */
    @RequestMapping(value = "/init")
    @ResponseBody
    public JSONObject init() throws IOException {
        JSONObject object = new JSONObject();

        /*redis实现:使用base64编码转化成字符串处理*/
//        List<String> imgList = JedisUtils.getList(JedisConfig.KEY_VALIDATE_IMG);
//        List<String> tpllist = JedisUtils.getList(JedisConfig.KEY_VALIDATE_TPL);
//        if (null == imgList || imgList.size() < 1 || tpllist == null || tpllist.size() < 1) {
//            imgList = new ArrayList<String>();
//            tpllist = new ArrayList<String>();
//            initValidateResources(imgList,tpllist);
//            JedisUtils.setList(JedisConfig.KEY_VALIDATE_IMG,imgList,JedisConfig.JEDIS_EXPIRE*3);
//            JedisUtils.setList(JedisConfig.KEY_VALIDATE_TPL,tpllist,JedisConfig.JEDIS_EXPIRE*3);
//        }

        /*本地缓存实现*/
        List<byte[]> imgList = ValidateCache.get(JedisConfig.KEY_VALIDATE_IMG);
        List<byte[]> tpllist = ValidateCache.get(JedisConfig.KEY_VALIDATE_TPL);
        if (null == imgList || imgList.size() < 1 || tpllist == null || tpllist.size() < 1) {
            imgList = new ArrayList<byte[]>();
            tpllist = new ArrayList<byte[]>();
            initValidateResources(imgList,tpllist);
            ValidateCache.set(JedisConfig.KEY_VALIDATE_IMG,imgList);
            ValidateCache.set(JedisConfig.KEY_VALIDATE_TPL,tpllist);
        }

        byte[] targetIS = null;//原图片
        byte[] templateIS = null;//减切原图片用的模板

        //随机选出原图和扣图模板
        Random ra = new Random();
        if (null != imgList){
            int rd = ra.nextInt(imgList.size());
            targetIS = imgList.get(rd);
        }
        if (null != tpllist){
            int rd = ra.nextInt(tpllist.size());
            templateIS = tpllist.get(rd);
        }

        Map<String, Object> pictureMap = null;
        try {
            //得到经过扣去小图的原图片和扣出来的图
            pictureMap = VerifyImageUtil.pictureTemplatesCut(templateIS,targetIS , "png", "jpg");

            //
            //扣出来的小图进行base64编码
            String newImage = Base64Utils.encodeToString((byte[]) pictureMap.get("newImage"));
            //扣去小图后的图片base64编码
            String sourceImage = Base64Utils.encodeToString((byte[]) pictureMap.get("oriCopyImage"));
            //得到扣的小图在原图上的坐标
            int X = (int) pictureMap.get("X");
            int Y = (int) pictureMap.get("Y");
            object.put("newImage", newImage);
            object.put("sourceImage", sourceImage);
            object.put("X", X);
            object.put("Y", Y);

            //取得token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            Map<String, Object> tokenObj = new HashMap<String, Object>();
            tokenObj.put("token", token);
            tokenObj.put("X", X);
            tokenObj.put("Y", Y);
            //token 保存2分钟
            JedisUtils.setObjectMap(JedisConfig.KEY_VALIDATE_TOKEN + ":" + token, tokenObj, 120000);
            object.put("token", token);
        } catch (Exception e) {
            log.error("",e);
        }
        //将扣出来的图，扣去小图的图，得到扣的小图在原图上的坐标，token返回给浏览器
        return object;
    }

    /**
     * 初始化验证图形生成资源
     * @param imgList
     * @param tpllist
     */
    private void initValidateResources(List<byte[]> imgList, List<byte[]> tpllist) throws IOException {
        /*加载验证原图*/
        System.out.println(ValidatorAction.class.getClassLoader().getResource(""));
        String target = URLDecoder.decode(ValidatorAction.class.getClassLoader().getResource("static/images/validate/targets").getPath(),"UTF-8");
        byte[] targetIS = null;//要扣的图
        byte[] templateIS = null;//扣图模板
        if (target.indexOf("!/") != -1) {//jar包
            String jarPath = "jar:" + target;
            log.debug(jarPath);
            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntrys = jarFile.entries();
            while (jarEntrys.hasMoreElements()) {
                JarEntry entry = jarEntrys.nextElement();
                String name = entry.getName();
                if (name.startsWith("static/images/validate/targets") && !name.equals("static/images/validate/targets/") && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                    log.debug("targets=" + name);
                    InputStream isTemplates = jarFile.getInputStream(entry);
                    targetIS = IOUtils.toByteArray(jarFile.getInputStream(entry));
                    imgList.add(targetIS);

                } else if (name.startsWith("static/images/validate/templates") && !name.equals("static/image/validate/templates/")  && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                    log.debug("templates=" + name);
                    InputStream isTemplates = jarFile.getInputStream(entry);
                    templateIS = IOUtils.toByteArray(jarFile.getInputStream(entry));
                    tpllist.add(templateIS);
                }
            }
        } else {
            File targetBaseFile = new File(target);
            if (null != targetBaseFile) {
                File[] fs = targetBaseFile.listFiles();
//                Random ra = new Random();
//                if (null != fs && fs.length > 0) {
//                    int random = ra.nextInt(fs.length);
//                    targetIS = IOUtils.toByteArray(new FileInputStream(fs[random]));
//                }
                for (File f : fs){
                    targetIS = IOUtils.toByteArray(new FileInputStream(f));
                    imgList.add(targetIS);
                }
            }
            /*加载切图模板*/
            String template = URLDecoder.decode(ValidatorAction.class.getClassLoader().getResource("static/images/validate/templates").getPath(),"UTF-8");
            File templateBaseFile = new File(template);
            if (null != templateBaseFile) {
                File[] fs = templateBaseFile.listFiles();
//                Random ra = new Random();
//                if (null != fs && fs.length > 0) {
//                    int random = ra.nextInt(fs.length);
//                    templateIS = IOUtils.toByteArray(new FileInputStream(fs[random]));
//                }
                for (File f : fs){
                    templateIS = IOUtils.toByteArray(new FileInputStream(f));
                    tpllist.add(templateIS);
                }
            }
        }
        log.info("initValidateResources:template size:" + tpllist.size() + "target size:" + imgList.size());
    }


    /**
     * 验证方法 (有验证码的方法提交，有时候也可以带上验证参数，做后端二次验证)
     *
     * @return
     */
    @RequestMapping(value = "check",method = RequestMethod.POST)
    @ResponseBody
    public Boolean check(String token, int X, int Y) {
        JSONObject message = new JSONObject();
        message.put("code", 2);
        message.put("massage", "验证不通过，请重试！");
        if (null == token || token.trim().length() < 1) {
            message.put("code", 0);
            message.put("massage", "请求参数错误:token为空！");
        }
        Map<String, Object> tokenObj = JedisUtils.getObjectMap(JedisConfig.KEY_VALIDATE_TOKEN + ":" + token);
        if (null == tokenObj) {
            message.put("code", -1);
            message.put("massage", "验证码超期，请重新请求！");
        } else {
            int sX = (Integer) tokenObj.get("X");
            int sY = (Integer) tokenObj.get("Y");
            if (sY != Y) {
                message.put("code", 0);
                message.put("massage", "请求参数错误:位置信息不正确！");
            } else {
                if (Math.abs(sX - X) <= 2) {
                    message.put("code", 1);
                    message.put("massage", "验证通过！");
                } else {
                    message.put("code", 2);
                    message.put("massage", "验证不通过，请重试！");
                }
            }
        }


        if (message.get("code").equals(1)) {
            return true;
        } else {
            return false;
        }
    }

}
