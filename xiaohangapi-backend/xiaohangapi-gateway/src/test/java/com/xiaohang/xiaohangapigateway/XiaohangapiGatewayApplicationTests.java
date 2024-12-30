package com.xiaohang.xiaohangapigateway;

import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.xiaohang.project.provider.DemoService;

@SpringBootTest
class XiaohangapiGatewayApplicationTests {
    @DubboReference
    private DemoService DemoService;
    @Test
    void contextLoads() {
    }
    @Test
    void testRpc() {
        System.out.println(DemoService.sayHello("world"));
    }
}
