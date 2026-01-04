package com.iv127.quizflow.server.acceptance.test.route;

import com.iv127.quizflow.server.acceptance.test.route.test.InterfaceWithPackagePrivateStaticMethod;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class ExampleJavaTest {


    @Test
    public void testJavaLabelBlockWithBreak() {
        int i = 10;
        aaa:
        {
            i++;
            if (i == 11) {
                break aaa; // continue can not be used
            }
            i++;
        }
        assertThat(i)
                .isEqualTo(11);
    }

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
    public void testAmbiguousIncrementExpression() {
        int x = 5;
        x += x + (x + 3) + ++x;
        assertThat(x).isEqualTo(24);
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

    @Test
    public void testLabelBlock() {
        int one = -1;
        loopX:
        {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (i == 2 && j == 2) {
                        one = 1;
                        break loopX;
                    }
                }
            }
        }
        assertThat(one)
                .isOne();
    }

    @Test
    public void testSubList() {
        List<String> vowels = new ArrayList<String>();
        vowels.add("a");
        vowels.add("e");
        vowels.add("i");
        vowels.add("o");
        vowels.add("u");

        Function<List<String>, List<String>> f = list -> list.subList(2, 4);
        f.apply(vowels);

        assertThat(vowels)
                .isEqualTo(List.of(
                        "a",
                        "e",
                        "i",
                        "o",
                        "u"));
        assertThat(f.apply(vowels))
                .isEqualTo(List.of(
                        "i",
                        "o"));
    }

    @Test
    public void testOrOperatorForCharacters() {
        // char is promoted to int before OR operation
        int result = 'a' | 'b';
        /*97 = 0110 0001
        98 = 0110 0010
                ---------------- OR
        0110 0011 = 99*/
        assertThat(result)
                .isEqualTo(99);
    }

    @Test
    public void testTrickyLoop1() {
        var counter = 0;
        outer:
        for (var i = 0; i < 3; i++) {
            middle:
            for (var j = 0; j < 3; j++) {
                inner:
                for (var k = 0; k < 3; k++) {
                    if (k - j > 0) {
                        break middle;
                    }
                    counter++;
                }
            }
        }
        assertThat(counter).isEqualTo(3);
    }

    private static final int EXAMPLE1 = 90;

    @Test
    public void testLocalANdStaticVariableDoesNotCOnflict() {
        int EXAMPLE1 = (EXAMPLE1 = 3) * 4;
        assertThat(EXAMPLE1).isEqualTo(12);
        assertThat(this.EXAMPLE1).isEqualTo(90);
    }

    @Test
    public void testArraysEquals() {
        int[] arr1 = {1, 2, 999};
        int[] arr2 = {1, 2, 99, 99};
        assertThat(Arrays.compare(arr1, arr2))
                .isEqualTo(1);

        int[] arr11 = {1, 2, 999};
        int[] arr12 = {1, 2, 999, 1, 1, 1};
        assertThat(Arrays.compare(arr11, arr12))
                .isEqualTo(-3);

        String[] s3 = {"Camel"};
        String[] s4 = {"Camel", null, null, null};
        assertThat(Arrays.compare(s3, s4))
                .isEqualTo(-3);
    }

    @Test
    public void testLeapYear() {
        LocalDate leapYear = LocalDate.of(2028, 2, 29);
        assertThat(leapYear).isNotNull();
        assertThrowsExactly(DateTimeException.class, () -> LocalDate.of(2028 + 1, 2, 29));
    }

    @Test
    public void testMathFunctions() {
        assertThat(Math.floor(6.6))
                .isEqualTo(6);
        assertThat(Math.round(5.5))
                .isEqualTo(6);
        assertThat(Math.round(5.499))
                .isEqualTo(5);
    }

    @Test
    public void testFinalVar() {
        final var q = 1;
        final var w = q + 1;
        assertThat(q).isOne();
        assertThat(w).isEqualTo(2);
    }

    @Test
    public void testIncrementAndAssign() {
        var q = 1;
        q = q++;
        assertThat(q).isOne();
    }

    @Test
    public void testGetOne() {
        assertThat(InterfaceWithPackagePrivateStaticMethod.getOne())
                .isEqualTo(1);
    }

    @Test
    public void testWhitespaceFromOpeningTextblockDelimiterIsIgnored() {
        List<? super Float> a = new ArrayList<Object>();
        List<? super Float> a1 = new ArrayList<Float>();
        List<? super Float> a2 = new ArrayList();
        List<? extends Number> q1 = new ArrayList<Number>();
        List<? extends Number> q2 = new ArrayList<Float>();
        List<? extends Number> q3 = new ArrayList();

        Stream.of(a, a1, a2, q1, q1, q2, q3).forEach(value -> assertThat(value).isEmpty());

        var s = """     
                abc q               """;
        assertThat(s).isEqualTo("abc q");

        var s1 = """
                    hello java \
                guru
                """;
        assertThat(s1).isEqualTo("    hello java guru\n");
    }

    @Test
    public void testNullCastToType() {
        var huey = (String) null;
        assertThat(huey).isNull();
    }

    class InnerClassStaticField {
        private static final int QQQ = 99;
        private static int QQQ1 = 991;
    }

    interface House {
        public default String getAddress() {
            return "101 Main Str";
        }
    }

    interface Office {
        public static String getAddress() {
            return "101 Smart Str";
        }
    }

    class HomeOffice implements House, Office {
        public String getAddress() {
            return "R No 1, Home";
        }
    }

    // by default final and static
    record TestRecord(int value) {
    }

    private static int getTestRecordValue() {
        return new TestRecord(0).value();
    }


}
