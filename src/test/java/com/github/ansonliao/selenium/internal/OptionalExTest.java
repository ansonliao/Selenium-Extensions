package com.github.ansonliao.selenium.internal;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OptionalExTest {

    @Test
    public void ifPresentRunsConsumerAndOrElseIsSkipped() {
        List<String> seen = new ArrayList<>();
        final boolean[] ran = {false};
        OptionalEx.ifPresent(Optional.of("x"), seen::add).orElse(() -> ran[0] = true);
        assertEquals(seen, List.of("x"));
        assertFalse(ran[0]);
    }

    @Test
    public void orElseRunsWhenOptionalIsEmpty() {
        final boolean[] ran = {false};
        OptionalEx.ifPresent(Optional.<String>empty(), value -> {
        }).orElse(() -> ran[0] = true);
        assertTrue(ran[0]);
    }

    @Test
    public void orElseCanBeOmittedWhenPresent() {
        List<String> seen = new ArrayList<>();
        // Just verifying the fluent call compiles and runs without orElse.
        OptionalEx.ifPresent(Optional.of("value"), seen::add);
        assertEquals(seen, List.of("value"));
    }
}
