package com.kite.trading.algo.service;

import com.kite.trading.algo.config.SignalQueueProperties;
import com.kite.trading.algo.runtime.Signal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalBus {

    private final SignalQueueProperties properties;
    private final RiskEngine riskEngine;

    private ArrayBlockingQueue<Signal> queue;
    private ExecutorService consumerExecutor;

    @PostConstruct
    void start() {
        queue = new ArrayBlockingQueue<>(properties.getCapacity());
        consumerExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "signal-bus-consumer");
            thread.setDaemon(true);
            return thread;
        });
        consumerExecutor.submit(this::consumeLoop);
    }

    public boolean offer(Signal signal) {
        boolean offered = queue.offer(signal);
        if (!offered) {
            log.warn("Signal bus is saturated. Dropping signal for {}", signal.symbol());
        }
        return offered;
    }

    public int depth() {
        return queue.size();
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Signal signal = queue.take();
                riskEngine.evaluate(signal);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                log.error("Unhandled exception while processing signal bus", exception);
            }
        }
    }

    @PreDestroy
    void stop() {
        if (consumerExecutor != null) {
            consumerExecutor.shutdownNow();
        }
    }
}
