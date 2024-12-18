package com.xiaohang.xiaohangapiinterface;

import com.xiaohang.xiaohangapiclientsdk.model.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import  com.xiaohang.xiaohangapiclientsdk.Client.XiaohangApiClient;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test Class
 *
 * @author Xiaohang
 */
@SpringBootTest
class XiaohangapiInterfaceApplicationTests {

    @Resource
    private XiaohangApiClient XiaohangApiClient;

    @Test
    void contextLoads() {
        String result =XiaohangApiClient.getNameByGet("Xiaohang");
        User user = new User();
        user.setUsername("xiaohang ji");
        String usernameByPost=XiaohangApiClient.getUsernameByPost(user);
        System.out.println(result);
        System.out.println(usernameByPost);
    }

}
