package com.epam.training.gym.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/** DOWN when more than 90% of the heap is used. */
@Component
public class MemoryHealthIndicator extends AbstractHealthIndicator {

    private static final double MAX_USED_RATIO = 0.9;

    private static final Logger log = LoggerFactory.getLogger(MemoryHealthIndicator.class);

    private final MemoryMXBean memory;

    public MemoryHealthIndicator() {
        this(ManagementFactory.getMemoryMXBean());
    }

    MemoryHealthIndicator(MemoryMXBean memory) {
        super("Memory health check failed");
        this.memory = memory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        MemoryUsage heap = memory.getHeapMemoryUsage();
        // max is -1 when the heap limit is not defined: nothing to compare with
        if (heap.getMax() <= 0) {
            builder.unknown().withDetail("usedBytes", heap.getUsed());
            return;
        }
        boolean high = heap.getUsed() > heap.getMax() * MAX_USED_RATIO;
        if (high) {
            log.warn("Heap usage is high: {} of {} bytes", heap.getUsed(), heap.getMax());
        }
        builder.status(high ? Status.DOWN : Status.UP)
                .withDetail("usedBytes", heap.getUsed())
                .withDetail("maxBytes", heap.getMax());
    }
}
