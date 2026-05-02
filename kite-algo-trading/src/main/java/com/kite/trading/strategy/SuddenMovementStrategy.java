package com.kite.trading.strategy;

import com.kite.trading.model.PriceMovement;
import com.kite.trading.repository.PriceMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuddenMovementStrategy {

    private final PriceMovementRepository priceMovementRepository;

    @Value("${strategy.sudden-movement.threshold:2.0}")
    private double movementThreshold;

    @Value("${strategy.sudden-movement.timeframe:5}")
    private int timeframeMinutes;

    @Value("${strategy.sudden-movement.volume-multiplier:1.5}")
    private double volumeMultiplier;

    public List<PriceMovement> detectSuddenMovements(String symbol, List<BigDecimal> prices, List<Long> volumes) {
        List<PriceMovement> movements = new ArrayList<>();

        if (prices.size() < 2) {
            return movements;
        }

        for (int i = 1; i < prices.size(); i++) {
            BigDecimal currentPrice = prices.get(i);
            BigDecimal previousPrice = prices.get(i - 1);

            BigDecimal percentageChange = calculatePercentageChange(previousPrice, currentPrice);

            if (Math.abs(percentageChange.doubleValue()) >= movementThreshold) {
                PriceMovement movement = createPriceMovement(
                        symbol,
                        previousPrice,
                        currentPrice,
                        percentageChange,
                        volumes.get(i),
                        determineMovementType(percentageChange)
                );
                movements.add(movement);
            }
        }

        return movements;
    }

    public PriceMovement detectSuddenMovement(String symbol, BigDecimal startPrice, BigDecimal endPrice, Long volume) {
        BigDecimal percentageChange = calculatePercentageChange(startPrice, endPrice);

        if (Math.abs(percentageChange.doubleValue()) >= movementThreshold) {
            return createPriceMovement(
                    symbol,
                    startPrice,
                    endPrice,
                    percentageChange,
                    volume,
                    determineMovementType(percentageChange)
            );
        }

        return null;
    }

    public boolean isVolumeSpike(Long currentVolume, Long averageVolume) {
        if (averageVolume == null || averageVolume == 0) {
            return false;
        }
        return currentVolume.doubleValue() > (averageVolume.doubleValue() * volumeMultiplier);
    }

    public BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.valueOf(50);
        }

        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }

        BigDecimal avgGain = calculateAverage(gains.subList(gains.size() - period, gains.size()));
        BigDecimal avgLoss = calculateAverage(losses.subList(losses.size() - period, losses.size()));

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(
                        BigDecimal.ONE.add(rs),
                        2,
                        RoundingMode.HALF_UP
                )
        );

        return rsi;
    }

    public MACDResult calculateMACD(List<BigDecimal> prices, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (prices.size() < slowPeriod + signalPeriod) {
            return new MACDResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<BigDecimal> fastEMA = calculateEMA(prices, fastPeriod);
        List<BigDecimal> slowEMA = calculateEMA(prices, slowPeriod);

        List<BigDecimal> macdLine = new ArrayList<>();
        for (int i = 0; i < fastEMA.size(); i++) {
            macdLine.add(fastEMA.get(i).subtract(slowEMA.get(i)));
        }

        List<BigDecimal> signalLine = calculateEMA(macdLine, signalPeriod);
        BigDecimal currentMACD = macdLine.get(macdLine.size() - 1);
        BigDecimal currentSignal = signalLine.get(signalLine.size() - 1);
        BigDecimal histogram = currentMACD.subtract(currentSignal);

        return new MACDResult(currentMACD, currentSignal, histogram);
    }

    public TradingSignal generateTradingSignal(PriceMovement movement, BigDecimal rsi, MACDResult macd) {
        TradingSignal signal = new TradingSignal();
        signal.setSymbol(movement.getSymbol());
        signal.setMovementType(movement.getMovementType());
        signal.setTimestamp(LocalDateTime.now());

        if (movement.getMovementType() == PriceMovement.MovementType.SUDDEN_RISE) {
            if (rsi.compareTo(BigDecimal.valueOf(70)) < 0 && macd.getHistogram().compareTo(BigDecimal.ZERO) > 0) {
                signal.setSignalType(SignalType.BUY);
                signal.setConfidence(calculateConfidence(movement, rsi, macd, true));
            } else {
                signal.setSignalType(SignalType.HOLD);
                signal.setConfidence(BigDecimal.ZERO);
            }
        } else if (movement.getMovementType() == PriceMovement.MovementType.SUDDEN_FALL) {
            if (rsi.compareTo(BigDecimal.valueOf(30)) > 0 && macd.getHistogram().compareTo(BigDecimal.ZERO) < 0) {
                signal.setSignalType(SignalType.SELL);
                signal.setConfidence(calculateConfidence(movement, rsi, macd, false));
            } else {
                signal.setSignalType(SignalType.HOLD);
                signal.setConfidence(BigDecimal.ZERO);
            }
        } else {
            signal.setSignalType(SignalType.HOLD);
            signal.setConfidence(BigDecimal.ZERO);
        }

        return signal;
    }

    private BigDecimal calculatePercentageChange(BigDecimal startPrice, BigDecimal endPrice) {
        if (startPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return endPrice.subtract(startPrice)
                .divide(startPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private PriceMovement.MovementType determineMovementType(BigDecimal percentageChange) {
        if (percentageChange.compareTo(BigDecimal.ZERO) > 0) {
            return PriceMovement.MovementType.SUDDEN_RISE;
        } else {
            return PriceMovement.MovementType.SUDDEN_FALL;
        }
    }

    private PriceMovement createPriceMovement(String symbol, BigDecimal startPrice, BigDecimal endPrice,
                                               BigDecimal percentageChange, Long volume,
                                               PriceMovement.MovementType movementType) {
        PriceMovement movement = new PriceMovement();
        movement.setSymbol(symbol);
        movement.setExchange("NSE");
        movement.setTimestamp(LocalDateTime.now());
        movement.setStartPrice(startPrice);
        movement.setEndPrice(endPrice);
        movement.setPercentageChange(percentageChange);
        movement.setVolume(volume);
        movement.setTimeframeMinutes(timeframeMinutes);
        movement.setMovementType(movementType);
        movement.setSignalGenerated(false);
        return movement;
    }

    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private List<BigDecimal> calculateEMA(List<BigDecimal> prices, int period) {
        List<BigDecimal> ema = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(period + 1), 4, RoundingMode.HALF_UP);

        BigDecimal firstEMA = calculateAverage(prices.subList(0, period));
        ema.add(firstEMA);

        for (int i = period; i < prices.size(); i++) {
            BigDecimal currentEMA = prices.get(i)
                    .subtract(ema.get(ema.size() - 1))
                    .multiply(multiplier)
                    .add(ema.get(ema.size() - 1));
            ema.add(currentEMA);
        }

        return ema;
    }

    private BigDecimal calculateConfidence(PriceMovement movement, BigDecimal rsi, MACDResult macd, boolean isBuy) {
        BigDecimal confidence = BigDecimal.ZERO;

        BigDecimal movementStrength = movement.getPercentageChange().abs()
                .divide(BigDecimal.valueOf(movementThreshold), 2, RoundingMode.HALF_UP);
        confidence = confidence.add(movementStrength.multiply(BigDecimal.valueOf(0.4)));

        BigDecimal rsiScore;
        if (isBuy) {
            rsiScore = BigDecimal.valueOf(70).subtract(rsi).divide(BigDecimal.valueOf(40), 2, RoundingMode.HALF_UP);
        } else {
            rsiScore = rsi.subtract(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(40), 2, RoundingMode.HALF_UP);
        }
        confidence = confidence.add(rsiScore.multiply(BigDecimal.valueOf(0.3)));

        BigDecimal macdScore = macd.getHistogram().abs()
                .divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
        confidence = confidence.add(macdScore.multiply(BigDecimal.valueOf(0.3)));

        return confidence.min(BigDecimal.valueOf(1)).max(BigDecimal.valueOf(0));
    }

    public static class TradingSignal {
        private String symbol;
        private PriceMovement.MovementType movementType;
        private SignalType signalType;
        private BigDecimal confidence;
        private LocalDateTime timestamp;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public PriceMovement.MovementType getMovementType() { return movementType; }
        public void setMovementType(PriceMovement.MovementType movementType) { this.movementType = movementType; }
        public SignalType getSignalType() { return signalType; }
        public void setSignalType(SignalType signalType) { this.signalType = signalType; }
        public BigDecimal getConfidence() { return confidence; }
        public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class MACDResult {
        private BigDecimal macd;
        private BigDecimal signal;
        private BigDecimal histogram;

        public MACDResult(BigDecimal macd, BigDecimal signal, BigDecimal histogram) {
            this.macd = macd;
            this.signal = signal;
            this.histogram = histogram;
        }

        public BigDecimal getMacd() { return macd; }
        public BigDecimal getSignal() { return signal; }
        public BigDecimal getHistogram() { return histogram; }
    }

    public enum SignalType {
        BUY, SELL, HOLD
    }
}
