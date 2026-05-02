package com.kite.trading.controller;

import com.kite.trading.service.KiteAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallbackController {

    private final KiteAuthService kiteAuthService;

    /**
     * Handle Kite Connect callback at /callback
     * This is the standard callback URL for Kite Connect
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(@RequestParam("request_token") String requestToken,
                                       @RequestParam(value = "action", required = false) String action,
                                       @RequestParam(value = "type", required = false) String type,
                                       @RequestParam(value = "status", required = false) String status) {
        try {
            log.info("Received callback at /callback with request token: {}", requestToken);
            log.info("Additional params - action: {}, type: {}, status: {}", action, type, status);

            // Process the request token
            kiteAuthService.processRequestToken(requestToken);

            // Redirect to dashboard on success
            return new RedirectView("/index.html?auth=success");
        } catch (Exception e) {
            log.error("Error processing callback at /callback", e);
            return new RedirectView("/index.html?auth=error&message=" + e.getMessage());
        }
    }
}