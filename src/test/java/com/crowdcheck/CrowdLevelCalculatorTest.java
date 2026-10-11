package com.crowdcheck;

import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.service.CrowdLevelCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CrowdLevelCalculator covering:
 * - Mathematical occupancy calculation
 * - Crowd level classifications (LOW, MODERATE, HIGH, CRITICAL)
 * - Strict threshold boundaries (39.99%, 40%, 69.99%, 70%, 89.99%, 90%)
 * - Capacity overflow (> 100%)
 * - Invalid inputs (negative crowd count, non-positive capacity)
 */
public class CrowdLevelCalculatorTest {

    @Nested
    @DisplayName("Occupancy Percentage Calculations")
    class OccupancyCalculations {

        @Test
        @DisplayName("Zero crowd gives 0.0% occupancy")
        void testZeroCrowd() {
            double occupancy = CrowdLevelCalculator.calculateOccupancyPercentage(0, 500);
            assertEquals(0.0, occupancy, 0.001);
        }

        @Test
        @DisplayName("Half capacity gives exactly 50.0% occupancy")
        void testHalfCapacity() {
            double occupancy = CrowdLevelCalculator.calculateOccupancyPercentage(250, 500);
            assertEquals(50.0, occupancy, 0.001);
        }

        @Test
        @DisplayName("Full capacity gives 100.0% occupancy")
        void testFullCapacity() {
            double occupancy = CrowdLevelCalculator.calculateOccupancyPercentage(500, 500);
            assertEquals(100.0, occupancy, 0.001);
        }

        @Test
        @DisplayName("Exceeding capacity gives > 100.0% occupancy")
        void testExceedingCapacity() {
            double occupancy = CrowdLevelCalculator.calculateOccupancyPercentage(600, 500);
            assertEquals(120.0, occupancy, 0.001);
        }
    }

    @Nested
    @DisplayName("Classification Thresholds and Boundaries")
    class ClassificationBoundaries {

        @ParameterizedTest(name = "Count {0} of Capacity {1} -> Level {2}")
        @CsvSource({
                // LOW: < 40%
                "0, 100, LOW",
                "20, 100, LOW",
                "39, 100, LOW",

                // MODERATE: 40% <= occupancy < 70%
                "40, 100, MODERATE",
                "50, 100, MODERATE",
                "69, 100, MODERATE",

                // HIGH: 70% <= occupancy < 90%
                "70, 100, HIGH",
                "80, 100, HIGH",
                "89, 100, HIGH",

                // CRITICAL: >= 90%
                "90, 100, CRITICAL",
                "95, 100, CRITICAL",
                "100, 100, CRITICAL",
                "150, 100, CRITICAL"
        })
        void testDiscreteBoundaries(int count, int capacity, CrowdLevel expectedLevel) {
            CrowdLevel actual = CrowdLevelCalculator.determineCrowdLevel(count, capacity);
            assertEquals(expectedLevel, actual);
        }

        @Test
        @DisplayName("Boundary: Just below 40% (39.99%) is LOW")
        void testJustBelow40IsLow() {
            assertEquals(CrowdLevel.LOW, CrowdLevelCalculator.classifyOccupancy(39.99));
        }

        @Test
        @DisplayName("Boundary: Exactly 40.0% is MODERATE")
        void testExactly40IsModerate() {
            assertEquals(CrowdLevel.MODERATE, CrowdLevelCalculator.classifyOccupancy(40.0));
        }

        @Test
        @DisplayName("Boundary: Just below 70% (69.99%) is MODERATE")
        void testJustBelow70IsModerate() {
            assertEquals(CrowdLevel.MODERATE, CrowdLevelCalculator.classifyOccupancy(69.99));
        }

        @Test
        @DisplayName("Boundary: Exactly 70.0% is HIGH")
        void testExactly70IsHigh() {
            assertEquals(CrowdLevel.HIGH, CrowdLevelCalculator.classifyOccupancy(70.0));
        }

        @Test
        @DisplayName("Boundary: Just below 90% (89.99%) is HIGH")
        void testJustBelow90IsHigh() {
            assertEquals(CrowdLevel.HIGH, CrowdLevelCalculator.classifyOccupancy(89.99));
        }

        @Test
        @DisplayName("Boundary: Exactly 90.0% is CRITICAL")
        void testExactly90IsCritical() {
            assertEquals(CrowdLevel.CRITICAL, CrowdLevelCalculator.classifyOccupancy(90.0));
        }

        @Test
        @DisplayName("Boundary: Overflowing capacity (e.g. 150%) is CRITICAL")
        void testOverflowIsCritical() {
            assertEquals(CrowdLevel.CRITICAL, CrowdLevelCalculator.classifyOccupancy(150.0));
        }
    }

    @Nested
    @DisplayName("Invalid Input Validation")
    class InvalidInputTests {

        @Test
        @DisplayName("Negative crowd count throws IllegalArgumentException")
        void testNegativeCrowdCount() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    CrowdLevelCalculator.calculateOccupancyPercentage(-5, 100));
            assertTrue(ex.getMessage().contains("negative"));
        }

        @Test
        @DisplayName("Zero capacity throws IllegalArgumentException")
        void testZeroCapacity() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    CrowdLevelCalculator.calculateOccupancyPercentage(50, 0));
            assertTrue(ex.getMessage().contains("strictly greater than zero"));
        }

        @Test
        @DisplayName("Negative capacity throws IllegalArgumentException")
        void testNegativeCapacity() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    CrowdLevelCalculator.calculateOccupancyPercentage(50, -100));
            assertTrue(ex.getMessage().contains("strictly greater than zero"));
        }

        @Test
        @DisplayName("determineCrowdLevel propagates exception on invalid input")
        void testDetermineCrowdLevelPropagatesException() {
            assertThrows(IllegalArgumentException.class, () ->
                    CrowdLevelCalculator.determineCrowdLevel(-1, 50));
            assertThrows(IllegalArgumentException.class, () ->
                    CrowdLevelCalculator.determineCrowdLevel(10, 0));
        }
    }

    @Nested
    @DisplayName("Progress Bar and Formatting")
    class ProgressBarTests {

        @Test
        @DisplayName("Generates non-empty visual progress bar")
        void testProgressBarGeneration() {
            String bar = CrowdLevelCalculator.getOccupancyBar(75.5, 20);
            assertNotNull(bar);
            assertTrue(bar.contains("75.5%"));
            assertTrue(bar.contains("█"));
            assertTrue(bar.contains("░"));
        }
    }
}
