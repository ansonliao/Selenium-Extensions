package io.github.ansonliao.selenium.testng;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the static discovery state of {@link TestNGFilter} for the duration
 * of a test and restores it afterwards.
 *
 * <p>{@code TestNGFilter} fills its state from a classpath scan in a static
 * initializer, so tests that assert on filtering results seed the state
 * directly instead of depending on whichever classes happen to be on the
 * test classpath.</p>
 */
final class TestNGFilterState implements AutoCloseable {

    private static final String CLASSES_FIELD = "testngClasses";
    private static final String METHOD_MAP_FIELD = "testNGClass2MethodMap";

    private final List<Class<?>> originalClasses;
    private final Multimap<Class<?>, Method> originalMethodMap;

    private TestNGFilterState(List<Class<?>> originalClasses,
                              Multimap<Class<?>, Method> originalMethodMap) {
        this.originalClasses = originalClasses;
        this.originalMethodMap = originalMethodMap;
    }

    /**
     * Captures the current state, then replaces it with the given classes and
     * an empty class-to-method map.
     */
    static TestNGFilterState seed(Class<?>... classes) {
        try {
            TestNGFilterState state = new TestNGFilterState(
                    new ArrayList<>(readClasses()), HashMultimap.create(readMethodMap()));
            write(CLASSES_FIELD, new ArrayList<>(List.of(classes)));
            write(METHOD_MAP_FIELD, HashMultimap.create());
            return state;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to seed TestNGFilter state", e);
        }
    }

    @Override
    public void close() {
        try {
            write(CLASSES_FIELD, originalClasses);
            write(METHOD_MAP_FIELD, originalMethodMap);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to restore TestNGFilter state", e);
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = TestNGFilter.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @SuppressWarnings("unchecked")
    private static List<Class<?>> readClasses() throws ReflectiveOperationException {
        return (List<Class<?>>) field(CLASSES_FIELD).get(null);
    }

    @SuppressWarnings("unchecked")
    private static Multimap<Class<?>, Method> readMethodMap() throws ReflectiveOperationException {
        return (Multimap<Class<?>, Method>) field(METHOD_MAP_FIELD).get(null);
    }

    private static void write(String name, Object value) throws ReflectiveOperationException {
        field(name).set(null, value);
    }
}
