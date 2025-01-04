package com.xiaohang.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "xiaohang.gateway")
@Data
public class GatewayConfig {

    private String host;

}
