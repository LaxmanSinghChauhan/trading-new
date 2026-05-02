package com.kite.trading.algo.service;

import com.kite.trading.algo.config.ExecutionQueueProperties;
import com.kite.trading.algo.runtime.TradeIntent;
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
public class ExecutionQueue {

    private final ExecutionQueueProperties properties;
    private final OrderExecutionService orderExecutionService;

    private ArrayBlockingQueue<TradeIntent> queue;
    private ExecutorService consumerExecutor;

    @PostConstruct
    void start() {
        queue = new ArrayBlockingQueue<>(properties.getCapacity());
        consumerExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "execution-queue-consumer");
            thread.setDaemon(true);
            return thread;
        });
        consumerExecutor.submit(this::consumeLoop);
    }

    public boolean offer(TradeIntent intent) {
        boolean offered = queue.offer(intent);
        if (!offered) {
            log.warn("Execution queue is saturated. Rejecting intent for {}", intent.symbol());
        }
        return offered;
    }

    public int depth() {
        return queue.size();
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TradeIntent intent = queue.take();
                orderExecutionService.execute(intent);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                log.error("Unhandled exception while processing execution queue", exception);
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
