package com.triageagent.bedrocktriageagent.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class BedrockHealthIndicator implements HealthIndicator {
    @Override public Health health() {
        return Health.up().withDetail("backend", "mock").build();
    }
}
