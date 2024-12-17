package com.xiaohang.xiaohangapiclientsdk;

import com.xiaohang.xiaohangapiclientsdk.Client.XiaohangApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * XiaohangApi client config
 *
 * @author xiaohang
 */
@Configuration
@ConfigurationProperties("xiaohangapi.client")
@Data
@ComponentScan
public class XiaohangApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public XiaohangApiClient xiaohangApiClient() {
        return new XiaohangApiClient(accessKey, secretKey);
    }
}
