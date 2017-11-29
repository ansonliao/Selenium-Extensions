package com.github.ansonliao.selenium.internal;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalEx {
    private boolean isPresent;

    private OptionalEx(boolean isPresent) {
        this.isPresent = isPresent;
    }

    public void orElse(Runnable runner) {
        if (!isPresent) {
            runner.run();
        }
    }

    public static <T> OptionalEx ifPresent(Optional<T> opt, Consumer<? super T> consumer) {
        if (opt.isPresent()) {
            consumer.accept(opt.get());
            return new OptionalEx(true);
        }
        return new OptionalEx(false);
    }
}
