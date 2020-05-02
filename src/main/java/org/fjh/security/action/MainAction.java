package org.fjh.security.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("main")
public class MainAction {

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("/ok")
    public String ok(){
        return "authenticationOk";
    }
}
