package ua.kpi.comsys.streams;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class TelemetryStreamAnalyzer {

    public Optional<SensorMetric> findFirstAnomaly(Stream<SensorMetric> stream, String targetDeviceId,
                                                   double criticalThreshold) {
        if (stream == null || targetDeviceId == null) {
            throw new IllegalArgumentException("stream and targetDeviceId must not be null");
        }
        return stream
                .filter(metric -> targetDeviceId.equals(metric.deviceId()))
                .filter(metric -> metric.value() > criticalThreshold)
                .findFirst();
    }

    public List<SensorMetric> streamActiveMetrics(Stream<SensorMetric> stream, double normalThreshold,
                                                  int maxCount) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null");
        }
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount must not be negative");
        }
        return stream
                .dropWhile(metric -> metric.value() <= normalThreshold)
                .limit(maxCount)
                .toList();
    }

    public List<Double> calculateMovingAverages(Stream<SensorMetric> stream, String metricType, int windowSize,
                                                int outputLimit) {
        if (stream == null || metricType == null) {
            throw new IllegalArgumentException("stream and metricType must not be null");
        }
        if (windowSize <= 0 || outputLimit <= 0) {
            throw new IllegalArgumentException("windowSize and outputLimit must be greater than 0");
        }
        return stream
                .filter(metric -> metricType.equals(metric.metricType()))
                .map(SensorMetric::value)
                .gather(movingAverage(windowSize))
                .limit(outputLimit)
                .toList();
    }

    private static Gatherer<Double, ?, Double> movingAverage(int windowSize) {
        return Gatherer.ofSequential(
                ArrayDeque<Double>::new,
                (Deque<Double> window, Double value, Gatherer.Downstream<? super Double> downstream) -> {
                    window.addLast(value);
                    if (window.size() > windowSize) {
                        window.removeFirst();
                    }
                    double sum = 0;
                    for (double v : window) {
                        sum += v;
                    }
                    return downstream.push(sum / window.size());
                });
    }
}
