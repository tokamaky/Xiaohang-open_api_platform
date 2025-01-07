package com.xiaohang.xiaohangapiinterface.controller;

import cn.hutool.core.util.StrUtil;
import com.xiaohang.xiaohangapiclientsdk.model.User;
import com.xiaohang.xiaohangapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Name API
 *
 * @author xiaohang
 */
@RestController
public class NameController {

    @PostMapping("/get")
    public String getNameByGet(@RequestParam String name) {
        return "GET your name is: " + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

//    @PostMapping("/user")
//    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request) {
//        String accessKey = request.getHeader("accessKey");
//        String nonce = request.getHeader("nonce");
//        String timestamp = request.getHeader("timestamp");
//        String sign = request.getHeader("sign");
//        String body = request.getHeader("body");
//        boolean hasBlank = StrUtil.hasBlank(accessKey, body, sign, nonce, timestamp);
//        // 判断是否有空
//        if (hasBlank) {
//            return "无权限";
//        }
//        // todo 实际情况应该是去数据库中查是否已分配给用户
//        if (!accessKey.equals("xiaohang")) {
//            throw new RuntimeException("无权限");
//        }
//        if (Long.parseLong(nonce) > 10000) {
//            throw new RuntimeException("无权限");
//        }
//        // todo 时间和当前时间不能超过 5 分钟
//        if (System.currentTimeMillis() - Long.parseLong(timestamp) > 5 * 60 * 1000) {
//            return "无权限";
//        }
//        // todo 实际情况中是从数据库中查出 secretKey
//        String serverSign = SignUtils.genSign(body, "abcdefgh");
//        if (!sign.equals(serverSign)) {
//          throw new RuntimeException("无权限");
//        }
//        // todo 调用次数 + 1 invokeCount
//        String result = "POST 用户名字是" + user.getUsername();
//        return result;
//    }

    @PostMapping("/api/name/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        System.out.println("Received user: " + user);
        return "POST your user name is ：" + user.getUsername();
    }
}
