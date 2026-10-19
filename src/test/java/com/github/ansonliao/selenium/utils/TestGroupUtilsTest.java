package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.testfixtures.ClassGroupFixture;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class TestGroupUtilsTest {

    @Test
    public void getClassTestGroupsReadsClassLevelGroups() {
        List<String> groups = TestGroupUtils.getClassTestGroups(ClassGroupFixture.class);
        assertEquals(groups, List.of("smoke"));
    }

    @Test
    public void getClassTestGroupsEmptyWhenNoTestAnnotation() {
        List<String> groups = TestGroupUtils.getClassTestGroups(StringUtils.class);
        assertEquals(groups, List.of());
    }

    @Test
    public void getMethodTestGroupsUnionsClassAndMethod() throws Exception {
        Method method = ClassGroupFixture.class.getDeclaredMethod("methodWithGroup");
        List<String> groups = TestGroupUtils.getMethodTestGroups(method);
        assertEquals(groups, List.of("smoke", "regression"));
    }

    @Test
    public void getMethodTestGroupsInheritsClassGroups() throws Exception {
        Method method = ClassGroupFixture.class.getDeclaredMethod("methodWithoutGroup");
        List<String> groups = TestGroupUtils.getMethodTestGroups(method);
        assertEquals(groups, List.of("smoke"));
    }
}
