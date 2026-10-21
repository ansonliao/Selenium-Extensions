package io.github.ansonliao.selenium.internal;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OptionalConsumerTest {

    @Test
    public void ifPresentRunsConsumerWhenValuePresent() {
        List<String> seen = new ArrayList<>();
        OptionalConsumer.of(Optional.of("x")).ifPresent(seen::add);
        assertEquals(seen, List.of("x"));
    }

    @Test
    public void ifPresentSkipsConsumerWhenEmpty() {
        List<String> seen = new ArrayList<>();
        OptionalConsumer.of(Optional.<String>empty()).ifPresent(seen::add);
        assertTrue(seen.isEmpty());
    }

    @Test
    public void orElseRunsWhenEmpty() {
        final boolean[] ran = {false};
        OptionalConsumer.of(Optional.<String>empty()).orElse(() -> ran[0] = true);
        assertTrue(ran[0]);
    }

    @Test
    public void orElseSkipsWhenValuePresent() {
        final boolean[] ran = {false};
        OptionalConsumer.of(Optional.of("x")).orElse(() -> ran[0] = true);
        assertFalse(ran[0]);
    }

    @Test
    public void ifPresentCanBeChainedWithOrElse() {
        List<String> seen = new ArrayList<>();
        final boolean[] ran = {false};
        OptionalConsumer.of(Optional.<String>empty())
                .ifPresent(seen::add)
                .orElse(() -> ran[0] = true);
        assertTrue(seen.isEmpty());
        assertTrue(ran[0]);
    }
}
