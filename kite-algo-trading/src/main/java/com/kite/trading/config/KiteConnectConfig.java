package com.kite.trading.config;

import com.kite.trading.service.MockKiteConnect;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kite")
@Data
public class KiteConnectConfig {

    private String apiKey;
    private String apiSecret;
    private String apiUrl = "https://api.kite.trade";

    // Ngrok configuration for authentication
    private String ngrokUrl;
    private String ngrokAuthtoken;
    private String callbackUrl;
    private String postbackUrl;

    @Bean
    public MockKiteConnect mockKiteConnect() {
        return new MockKiteConnect(apiKey, apiSecret);
    }

    /**
     * Get the full callback URL for Kite Connect authentication
     * Note: Kite Connect expects /callback as the standard callback path
     */
    public String getFullCallbackUrl() {
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            return callbackUrl;
        }
        // Fallback to ngrok URL + /callback (standard Kite Connect path)
        return ngrokUrl + "/callback";
    }

    /**
     * Get the full postback URL for Kite Connect notifications
     */
    public String getFullPostbackUrl() {
        if (postbackUrl != null && !postbackUrl.isEmpty()) {
            return postbackUrl;
        }
        // Fallback to ngrok URL + postback path
        return ngrokUrl + "/api/auth/postback";
    }
}
