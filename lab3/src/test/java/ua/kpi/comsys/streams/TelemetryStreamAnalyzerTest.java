package ua.kpi.comsys.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(2)
class TelemetryStreamAnalyzerTest {

    private final TelemetryStreamAnalyzer analyzer = new TelemetryStreamAnalyzer();

    private static SensorMetric metric(String deviceId, String type, double value) {
        return new SensorMetric(deviceId, type, value, Instant.EPOCH);
    }

    private static Stream<SensorMetric> counting(String deviceId, String type) {
        return Stream.iterate(0, i -> i + 1).map(i -> metric(deviceId, type, i));
    }

    @Test
    void findFirstAnomalyOnInfiniteStream() {
        Optional<SensorMetric> result = analyzer.findFirstAnomaly(counting("d1", "temp"), "d1", 100);
        assertEquals(101.0, result.orElseThrow().value());
    }

    @Test
    void findFirstAnomalyIgnoresOtherDevices() {
        Stream<SensorMetric> stream = Stream.iterate(0, i -> i + 1)
                .map(i -> metric(i % 2 == 0 ? "d1" : "d2", "temp", i));
        SensorMetric result = analyzer.findFirstAnomaly(stream, "d2", 10).orElseThrow();
        assertEquals("d2", result.deviceId());
        assertEquals(11.0, result.value());
    }

    @Test
    void findFirstAnomalyThresholdIsStrict() {
        Stream<SensorMetric> stream = Stream.of(metric("d1", "temp", 50), metric("d1", "temp", 51));
        assertEquals(51.0, analyzer.findFirstAnomaly(stream, "d1", 50).orElseThrow().value());
    }

    @Test
    void findFirstAnomalyReturnsEmptyOnFiniteStreamWithoutMatch() {
        Stream<SensorMetric> stream = Stream.of(metric("d1", "temp", 1), metric("d2", "temp", 999));
        assertTrue(analyzer.findFirstAnomaly(stream, "d1", 10).isEmpty());
    }

    @Test
    void findFirstAnomalyRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.findFirstAnomaly(null, "d1", 1));
        assertThrows(IllegalArgumentException.class, () -> analyzer.findFirstAnomaly(Stream.empty(), null, 1));
    }

    @Test
    void streamActiveMetricsSkipsStableHeadAndLimitsInfiniteStream() {
        List<SensorMetric> result = analyzer.streamActiveMetrics(counting("d1", "temp"), 10, 3);
        assertEquals(List.of(11.0, 12.0, 13.0), result.stream().map(SensorMetric::value).toList());
    }

    @Test
    void streamActiveMetricsKeepsLaterStableValuesAfterFirstActiveOne() {
        Stream<SensorMetric> stream = Stream.of(5.0, 5.0, 20.0, 1.0, 30.0, 2.0)
                .map(v -> metric("d1", "temp", v));
        List<Double> values = analyzer.streamActiveMetrics(stream, 10, 3).stream().map(SensorMetric::value).toList();
        assertEquals(List.of(20.0, 1.0, 30.0), values);
    }

    @Test
    void streamActiveMetricsWithZeroMaxCountReturnsEmptyList() {
        assertTrue(analyzer.streamActiveMetrics(counting("d1", "temp"), 10, 0).isEmpty());
    }

    @Test
    void streamActiveMetricsReturnsEmptyWhenAllMetricsAreStable() {
        Stream<SensorMetric> stream = Stream.of(metric("d1", "temp", 1), metric("d1", "temp", 10));
        assertTrue(analyzer.streamActiveMetrics(stream, 10, 5).isEmpty());
    }

    @Test
    void streamActiveMetricsRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.streamActiveMetrics(null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> analyzer.streamActiveMetrics(Stream.empty(), 1, -1));
    }

    @Test
    void calculateMovingAveragesOnInfiniteStream() {
        List<Double> result = analyzer.calculateMovingAverages(counting("d1", "temp"), "temp", 3, 5);
        assertEquals(List.of(0.0, 0.5, 1.0, 2.0, 3.0), result);
    }

    @Test
    void calculateMovingAveragesFiltersByMetricType() {
        Stream<SensorMetric> stream = Stream.iterate(0, i -> i + 1)
                .map(i -> metric("d1", i % 2 == 0 ? "temp" : "humidity", i));
        List<Double> result = analyzer.calculateMovingAverages(stream, "temp", 2, 3);
        assertEquals(List.of(0.0, 1.0, 3.0), result);
    }

    @Test
    void calculateMovingAveragesWithWindowOfOneReturnsValues() {
        List<Double> result = analyzer.calculateMovingAverages(counting("d1", "temp"), "temp", 1, 3);
        assertEquals(List.of(0.0, 1.0, 2.0), result);
    }

    @Test
    void calculateMovingAveragesStopsOnFiniteStreamShorterThanLimit() {
        Stream<SensorMetric> stream = Stream.of(metric("d1", "temp", 2), metric("d1", "temp", 4));
        assertEquals(List.of(2.0, 3.0), analyzer.calculateMovingAverages(stream, "temp", 5, 10));
    }

    @Test
    void calculateMovingAveragesRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.calculateMovingAverages(null, "temp", 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.calculateMovingAverages(Stream.empty(), null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.calculateMovingAverages(Stream.empty(), "temp", 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.calculateMovingAverages(Stream.empty(), "temp", 1, 0));
    }
}
