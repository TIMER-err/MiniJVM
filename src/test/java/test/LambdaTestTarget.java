package test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * Target class for testing lambda expressions and method references.
 * This class will be executed inside MiniJVM.
 */
public class LambdaTestTarget {

    // ============= Lambda Expression Tests =============

    public static int testSimpleRunnable() {
        final int[] counter = {0};
        Runnable r = () -> counter[0]++;
        r.run();
        return counter[0]; // Should be 1
    }

    public static int testConsumerLambda() {
        final int[] result = {0};
        Consumer<Integer> consumer = x -> result[0] = x * 2;
        consumer.accept(5);
        return result[0]; // Should be 10
    }

    public static int testFunctionLambda() {
        Function<Integer, Integer> doubler = x -> x * 2;
        return doubler.apply(21); // Should be 42
    }

    public static int testBiFunctionLambda() {
        BiFunction<Integer, Integer, Integer> adder = (a, b) -> a + b;
        return adder.apply(15, 27); // Should be 42
    }

    public static boolean testPredicateLambda() {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        return isEven.test(42); // Should be true
    }

    public static int testSupplierLambda() {
        Supplier<Integer> supplier = () -> 99;
        return supplier.get(); // Should be 99
    }

    // ============= Capturing Variables Tests =============

    public static int testCaptureSingleVariable() {
        int x = 10;
        Function<Integer, Integer> addX = y -> x + y;
        return addX.apply(5); // Should be 15
    }

    public static int testCaptureMultipleVariables() {
        int a = 10;
        int b = 20;
        int c = 30;
        Supplier<Integer> sum = () -> a + b + c;
        return sum.get(); // Should be 60
    }

    public static int testCaptureObject() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        Supplier<Integer> getSize = () -> list.size();
        return getSize.get(); // Should be 3
    }

    // ============= Method Reference Tests =============

    public static int testStaticMethodReference() {
        Function<String, Integer> parser = Integer::parseInt;
        return parser.apply("123"); // Should be 123
    }

    public static int testInstanceMethodReference() {
        Function<String, Integer> lengthFunc = String::length;
        return lengthFunc.apply("Hello"); // Should be 5
    }

    public static int testConstructorReference() {
        Supplier<List<Integer>> listFactory = ArrayList::new;
        List<Integer> list = listFactory.get();
        list.add(1);
        list.add(2);
        return list.size(); // Should be 2
    }

    public static int testBoundMethodReference() {
        String str = "Test String";
        Supplier<Integer> lengthGetter = str::length;
        return lengthGetter.get(); // Should be 11
    }

    // ============= Complex Scenarios Tests =============

    public static int testNestedLambda() {
        Function<Integer, Function<Integer, Integer>> adder = x -> y -> x + y;
        Function<Integer, Integer> add10 = adder.apply(10);
        return add10.apply(20); // Should be 30
    }

    public static int testLambdaReturningLambda() {
        Supplier<Function<Integer, Integer>> multiplierFactory = () -> x -> x * 3;
        Function<Integer, Integer> tripler = multiplierFactory.get();
        return tripler.apply(14); // Should be 42
    }

    public static int testLambdaInCollection() {
        List<Function<Integer, Integer>> operations = new ArrayList<>();
        operations.add(x -> x + 1);
        operations.add(x -> x * 2);
        operations.add(x -> x - 1);

        int result = 2;
        for (Function<Integer, Integer> op : operations) {
            result = op.apply(result);
        }
        return result; // (2 + 1) * 2 - 1 = 5
    }

    public static int testChainedLambdaOperations() {
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> multiplyTwo = x -> x * 2;
        Function<Integer, Integer> chained = addOne.andThen(multiplyTwo);
        return chained.apply(5); // (5 + 1) * 2 = 12
    }

    // ============= Helper Method for Method Reference =============

    public static int helperStaticMethod() {
        return 42;
    }
}
