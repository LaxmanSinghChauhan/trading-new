package com.kite.trading.algo.service;

import com.kite.trading.algo.config.NewsProperties;
import com.kite.trading.algo.domain.NewsLog;
import com.kite.trading.algo.repository.NewsLogRepository;
import com.kite.trading.algo.runtime.NewsClassification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsDaemon {

    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final NewsProperties newsProperties;
    private final NewsLogRepository newsLogRepository;
    private final NewsSentimentClassifier newsSentimentClassifier;
    private final PositionManager positionManager;
    private final StockUniverseService stockUniverseService;
    private final TelegramAlertService telegramAlertService;
    private final Clock tradingClock;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Set<String> processedHeadlines = new LinkedHashSet<>();

    @Scheduled(fixedDelayString = "${news.poll-interval-ms:120000}", initialDelay = 30_000L)
    public void poll() {
        if (!newsProperties.isEnabled()) {
            return;
        }
        pollNse();
        pollGoogleNews();
    }

    private void pollNse() {
        if (newsProperties.getNseUrl() == null || newsProperties.getNseUrl().isBlank()) {
            return;
        }
        fetchAndPersist(newsProperties.getNseUrl(), "NSE", null);
    }

    private void pollGoogleNews() {
        if (newsProperties.getGoogleRssTemplate() == null || newsProperties.getGoogleRssTemplate().isBlank()) {
            return;
        }
        stockUniverseService.activeUniverse().stream()
                .filter(stock -> !"INDEX".equals(stock.instrumentType()))
                .limit(newsProperties.getTopSymbolLimit())
                .forEach(stock -> fetchAndPersist(String.format(newsProperties.getGoogleRssTemplate(), stock.symbol()), "GOOGLE_NEWS", stock.symbol()));
    }

    private void fetchAndPersist(String url, String source, String symbol) {
        try {
            String xml = restTemplate.getForObject(url, String.class);
            if (xml == null || xml.isBlank()) {
                return;
            }
            extractTitles(xml).stream()
                    .filter(title -> processedHeadlines.add(source + "::" + title))
                    .forEach(title -> saveHeadline(symbol, title, source));
        } catch (Exception exception) {
            log.warn("Failed to poll news source {}", source, exception);
        }
    }

    private List<String> extractTitles(String xml) {
        java.util.ArrayList<String> titles = new java.util.ArrayList<>();
        Matcher matcher = TITLE_PATTERN.matcher(xml);
        while (matcher.find()) {
            String title = matcher.group(1)
                    .replace("&amp;", "&")
                    .replace("&#39;", "'")
                    .replace("&quot;", "\"")
                    .trim();
            if (!title.isBlank() && !title.toLowerCase().contains("google news")) {
                titles.add(title);
            }
        }
        return titles.stream().limit(5).toList();
    }

    private void saveHeadline(String symbol, String headline, String source) {
        NewsClassification classification = newsSentimentClassifier.classify(headline);
        NewsLog newsLog = new NewsLog();
        newsLog.setSymbol(symbol);
        newsLog.setHeadline(headline);
        newsLog.setSource(source);
        newsLog.setSentiment(classification.sentiment());
        newsLog.setConfidence(classification.confidence());
        newsLog.setPublishedAt(LocalDateTime.now(tradingClock));
        newsLog.setProcessedAt(LocalDateTime.now(tradingClock));
        newsLogRepository.save(newsLog);

        if ("BEARISH".equals(classification.sentiment()) && symbol != null && positionManager.hasOpenPosition(symbol)) {
            telegramAlertService.send("Bearish news for open position " + symbol + ": " + headline);
        }
    }
}
