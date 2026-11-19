package com.epam.training.gym.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemoryHealthIndicatorTest {

    private final MemoryMXBean memory = mock(MemoryMXBean.class);
    private final MemoryHealthIndicator indicator = new MemoryHealthIndicator(memory);

    @Test
    void health_lowUsage_isUp() {
        when(memory.getHeapMemoryUsage()).thenReturn(new MemoryUsage(0, 500, 800, 1000));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(500L, health.getDetails().get("usedBytes"));
        assertEquals(1000L, health.getDetails().get("maxBytes"));
    }

    @Test
    void health_usageAboveLimit_isDown() {
        when(memory.getHeapMemoryUsage()).thenReturn(new MemoryUsage(0, 950, 1000, 1000));

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
