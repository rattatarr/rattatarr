package com.rattatarr.rattatarr.controllers;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

class BaseControllerTest {

    @Test
    void testLoggerIsInitialized() {
        // Given
        TestController controller = new TestController();

        // Then
        assertNotNull(controller.logger);
    }

    @Test
    void testLoggerHasCorrectName() {
        // Given
        TestController controller = new TestController();

        // Then
        assertEquals(TestController.class.getName(), controller.logger.getName());
    }

    @Test
    void testLoggerIsAccessibleToSubclass() {
        // Given
        TestController controller = new TestController();

        // When/Then
        assertDoesNotThrow(controller::testLoggerAccess);
    }

    @Test
    void testMultipleInstancesHaveIndependentLoggers() {
        // Given
        TestController controller1 = new TestController();
        AnotherTestController controller2 = new AnotherTestController();

        // Then
        assertNotEquals(controller1.logger.getName(), controller2.logger.getName());
        assertEquals(TestController.class.getName(), controller1.logger.getName());
        assertEquals(AnotherTestController.class.getName(), controller2.logger.getName());
    }

    // Test implementations
    private static class TestController extends BaseController {
        public void testLoggerAccess() {
            logger.debug("Test log message");
        }

        public Logger getLogger() {
            return logger;
        }
    }

    private static class AnotherTestController extends BaseController {
    }
}
