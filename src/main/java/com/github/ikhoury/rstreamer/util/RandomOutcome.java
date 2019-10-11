package com.github.ikhoury.rstreamer.util;

import java.util.Random;

/**
 * Tries to make it harder to get a random true boolean.
 */
public class RandomOutcome {

    private static final Random RANDOM = new Random();
    private static final int MAGIC_NUMBER = 42;

    public static boolean randomBooleanOutcome() {
        return RANDOM.nextBoolean() && RANDOM.nextInt(MAGIC_NUMBER + 1) == MAGIC_NUMBER;
    }
}
