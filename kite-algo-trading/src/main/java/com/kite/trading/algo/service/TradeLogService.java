package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.BrokerMode;
import com.kite.trading.algo.domain.LegType;
import com.kite.trading.algo.domain.TradeLifecycleStatus;
import com.kite.trading.algo.domain.TradeLegLog;
import com.kite.trading.algo.domain.TradeLog;
import com.kite.trading.algo.repository.TradeLegLogRepository;
import com.kite.trading.algo.repository.TradeLogRepository;
import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.TradeIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeLogService {

    private final TradeLogRepository tradeLogRepository;
    private final TradeLegLogRepository tradeLegLogRepository;
    private final ChargesEstimator chargesEstimator;

    @Transactional
    public TradeLog openTrade(TradeIntent intent, BrokerOrderResult result, BrokerMode brokerMode) {
        TradeLog tradeLog = new TradeLog();
        tradeLog.setSymbol(intent.symbol());
        tradeLog.setInstrumentToken(intent.instrumentToken());
        tradeLog.setBrokerMode(brokerMode);
        tradeLog.setSignalStrength(intent.signalStrength().name());
        tradeLog.setEntryPriceVwap(result.filledPrice());
        tradeLog.setTotalQuantity(result.quantity());
        tradeLog.setRemainingQuantity(result.quantity());
        tradeLog.setEntryTime(result.executedAt());
        tradeLog.setGrossPnl(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        tradeLog.setCharges(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        tradeLog.setNetPnl(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        tradeLog.setStatus(TradeLifecycleStatus.OPEN);
        tradeLogRepository.save(tradeLog);

        TradeLegLog entryLeg = new TradeLegLog();
        entryLeg.setTrade(tradeLog);
        entryLeg.setLegType(LegType.ENTRY);
        entryLeg.setReason("BUY");
        entryLeg.setOrderId(result.orderId());
        entryLeg.setQuantity(result.quantity());
        entryLeg.setPrice(result.filledPrice());
        entryLeg.setCharges(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        entryLeg.setBrokerMode(brokerMode);
        entryLeg.setExecutedAt(result.executedAt());
        tradeLegLogRepository.save(entryLeg);
        return tradeLog;
    }

    @Transactional
    public TradeLog recordExit(Long tradeId, BrokerOrderResult result, String reason) {
        TradeLog tradeLog = tradeLogRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

        BigDecimal legNotional = result.filledPrice().multiply(BigDecimal.valueOf(result.quantity()));
        BigDecimal legCharges = chargesEstimator.estimate(legNotional);

        TradeLegLog leg = new TradeLegLog();
        leg.setTrade(tradeLog);
        leg.setLegType(LegType.EXIT);
        leg.setReason(reason);
        leg.setOrderId(result.orderId());
        leg.setQuantity(result.quantity());
        leg.setPrice(result.filledPrice());
        leg.setCharges(legCharges);
        leg.setBrokerMode(tradeLog.getBrokerMode());
        leg.setExecutedAt(result.executedAt());
        tradeLegLogRepository.save(leg);

        BigDecimal grossIncrement = result.filledPrice()
                .subtract(tradeLog.getEntryPriceVwap())
                .multiply(BigDecimal.valueOf(result.quantity()))
                .setScale(4, RoundingMode.HALF_UP);
        tradeLog.setGrossPnl(tradeLog.getGrossPnl().add(grossIncrement));
        tradeLog.setCharges(tradeLog.getCharges().add(legCharges));
        tradeLog.setNetPnl(tradeLog.getGrossPnl().subtract(tradeLog.getCharges()));
        tradeLog.setRemainingQuantity(Math.max(tradeLog.getRemainingQuantity() - result.quantity(), 0));

        List<TradeLegLog> legs = tradeLegLogRepository.findByTradeIdOrderByExecutedAtAsc(tradeId);
        int exitQty = legs.stream()
                .filter(existing -> existing.getLegType() == LegType.EXIT)
                .mapToInt(TradeLegLog::getQuantity)
                .sum();
        BigDecimal exitNotional = legs.stream()
                .filter(existing -> existing.getLegType() == LegType.EXIT)
                .map(existing -> existing.getPrice().multiply(BigDecimal.valueOf(existing.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (exitQty > 0) {
            tradeLog.setExitPriceVwap(exitNotional.divide(BigDecimal.valueOf(exitQty), 4, RoundingMode.HALF_UP));
        }
        if (tradeLog.getRemainingQuantity() == 0) {
            tradeLog.setStatus(TradeLifecycleStatus.CLOSED);
            tradeLog.setExitTime(result.executedAt());
            tradeLog.setFinalExitReason(reason);
        }
        return tradeLogRepository.save(tradeLog);
    }
}
