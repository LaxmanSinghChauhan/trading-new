package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "universe")
@Data
public class UniverseProperties {

    private BigDecimal minPrice = BigDecimal.valueOf(50.0);
    private String refreshCron = "0 0 8 * * MON-FRI";
    private List<String> blacklist = new ArrayList<>();
    private String instrumentMasterResource = "universe/instrument_bootstrap.csv";
    private String nifty500Resource = "universe/nifty500_symbols.txt";
    private String bankniftyResource = "universe/banknifty_symbols.txt";
}
