package com.kite.trading.algo.service;

import com.kite.trading.algo.config.TickQueueProperties;
import com.kite.trading.algo.runtime.Tick;
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
public class TickPipeline {

    private final TickQueueProperties properties;
    private final TickProcessor tickProcessor;

    private ArrayBlockingQueue<Tick> queue;
    private ExecutorService consumerExecutor;

    @PostConstruct
    void start() {
        queue = new ArrayBlockingQueue<>(properties.getCapacity());
        consumerExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "tick-pipeline-consumer");
            thread.setDaemon(true);
            return thread;
        });
        consumerExecutor.submit(this::consumeLoop);
    }

    public boolean offer(Tick tick) {
        boolean offered = queue.offer(tick);
        if (!offered) {
            log.warn("Tick queue saturated. Dropping tick for {}", tick.symbol());
        }
        return offered;
    }

    public int depth() {
        return queue.size();
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Tick tick = queue.take();
                tickProcessor.process(tick);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                log.error("Unhandled exception while processing tick pipeline", exception);
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
