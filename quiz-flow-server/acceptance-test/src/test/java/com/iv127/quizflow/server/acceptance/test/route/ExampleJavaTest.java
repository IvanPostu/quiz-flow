package com.iv127.quizflow.server.acceptance.test.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleJavaTest {
    @Test
    public void testEmptyTextBlock() {
        String abc = /*whitespaces ignored*/"""            
                """;
        assertThat(abc).isEqualTo("");
    }

    @Test
    public void testUnderscoreSyntaxStuff() {
        var f = 1____3.0_0f;
        assertThat(f)
                .isEqualTo(13);
    }

    @Test
    public void testStringRepeatMethod() {
        assertThat("abc".repeat(2))
                .isEqualTo("abcabc");
    }

    @Test
    public void testStringTransformMethod() {
        assertThat((Integer) "abc".transform(value -> value.length()))
                .isEqualTo(3);
    }

    @Test
    public void testNestedClassStaticField() {
        assertThat(InnerClassStaticField.QQQ)
                .isEqualTo(99);
        assertThat(InnerClassStaticField.QQQ1)
                .isEqualTo(991);
    }

    @Test
    public void dummyTest() {
        assertThat(1 + 1)
                .isEqualTo(2);
    }

    class InnerClassStaticField {
        private static final int QQQ = 99;
        private static int QQQ1 = 991;
    }
}
