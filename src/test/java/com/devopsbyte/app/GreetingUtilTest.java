
package com.devopsbyte.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GreetingUtilTest {

    @Test
    void defaultGreetingWhenNulls() {
        assertEquals("Hello, world!", GreetingUtil.greet(null, null));
    }

    @Test
    void customNameAndPrefix() {
        assertEquals("Namaste, Phoenix!", GreetingUtil.greet("Phoenix", "Namaste"));
    }
}
