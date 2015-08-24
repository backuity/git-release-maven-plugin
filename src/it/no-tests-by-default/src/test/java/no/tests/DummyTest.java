package no.tests;

import org.junit.Test;

import java.lang.IllegalStateException;

public class DummyTest {
    @Test
    public void test() {
        throw new IllegalStateException("We shouldn't run the tests!");
    }
}