package com.careerflow.common.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(CorrelationIdFilter.class)
public class CareerflowObservabilityAutoConfiguration {
}
