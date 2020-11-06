package com.github.ikhoury.rstreamer.e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.Collections;
import java.util.List;

import static com.github.ikhoury.rstreamer.util.Container.REDIS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/**
 * This E2E test verifies that a simple application implemented using the library
 * functions properly and outputs the predicted results in single threaded and multi threaded scenarios.
 */
public class MultiplierWorkerApplicationE2ETest {

    private static final int NUMBERS_TO_SEND = 5000;

    @Rule
    public final GenericContainer redis = new GenericContainer<>(REDIS)
            .withExposedPorts(6379);

    private MultiplierApplicationDriver applicationDriver;

    private static final int MULTIPLIER_VALUE = 5;

    @Before
    public void setUp() {
        applicationDriver = new MultiplierApplicationDriver(
                redis.getContainerIpAddress(),
                redis.getFirstMappedPort(),
                MULTIPLIER_VALUE
        );

        applicationDriver.start();
    }

    @After
    public void tearDown() {
        applicationDriver.stop();
    }

    @Test
    public void multipliesListOfNumbersSequential() {
        for (int i = 0; i < NUMBERS_TO_SEND; i++) {
            applicationDriver.sendNumber(i);
            assertThat(applicationDriver.removeFirstOutputNumber(), equalTo(i * MULTIPLIER_VALUE));
        }
    }

    @Test
    public void multiplesListOfNumbersParallel() throws InterruptedException {
        for (int i = 0; i < NUMBERS_TO_SEND; i++) {
            applicationDriver.sendNumber(i);
        }

        applicationDriver.waitForInputToBeProcessed();

        List<Integer> processedNumbers = applicationDriver.currentOutputNumbers();
        Collections.sort(processedNumbers);

        assertThat(applicationDriver.currentOutputNumbers(), hasSize(NUMBERS_TO_SEND));
        assertThat(applicationDriver.currentOutputNumbers(), not(equalTo(processedNumbers)));
    }


}
