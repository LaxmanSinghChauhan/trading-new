package com.kite.trading.algo.service;

import com.kite.trading.algo.config.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAlertService {

    private final TelegramProperties telegramProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void send(String message) {
        if (!telegramProperties.isEnabled()
                || telegramProperties.getBotToken() == null
                || telegramProperties.getBotToken().isBlank()
                || telegramProperties.getChatId() == null
                || telegramProperties.getChatId().isBlank()) {
            log.info("Telegram alert: {}", message);
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + telegramProperties.getBotToken() + "/sendMessage";
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            payload.add("chat_id", telegramProperties.getChatId());
            payload.add("text", message);
            restTemplate.postForEntity(url, payload, String.class);
        } catch (Exception exception) {
            log.warn("Failed to send Telegram alert", exception);
        }
    }
}
