package com.iv127.quizflow.server.acceptance.test.route;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    public void testExample() {
        List<? extends Float> l1 = new ArrayList<Float>();
        List<? super Float> l2 = new ArrayList<Float>();
        assertThat(l1)
                .isEqualTo(l2)
                .isEmpty();
    }

    @Test
    public void testSortStringsUsingNaturalOrderComparator() {
        var list = new ArrayList<String>();
        list.add("-100");
        list.add("-200");
        list.add("100");
        list.add("200");
        list.add("Aaaa");
        list.add("aaaa");

        list.sort(Comparator.naturalOrder());

        assertThat(list)
                .isEqualTo(List.of(
                        "-100",
                        "-200",
                        "100",
                        "200",
                        "Aaaa",
                        "aaaa"
                ));
    }

    class InnerClassStaticField {
        private static final int QQQ = 99;
        private static int QQQ1 = 991;
    }

}
