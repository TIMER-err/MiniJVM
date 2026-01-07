package test;

import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * Comprehensive test suite for InvokeDynamic support.
 * Tests lambda expressions, method references, and MethodHandle operations.
 */
public class InvokeDynamicTest {

    private static final ExecutionManager manager = new ExecutionManager(new LoaderClassProvider());
    private static final ExecutionContext context = manager.newContext();
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== InvokeDynamic Test Suite ===\n");

        // Lambda Expression Tests
        runTest("Simple Runnable Lambda", InvokeDynamicTest::testSimpleRunnable);
        runTest("Consumer Lambda", InvokeDynamicTest::testConsumerLambda);
        runTest("Function Lambda", InvokeDynamicTest::testFunctionLambda);
        runTest("BiFunction Lambda", InvokeDynamicTest::testBiFunctionLambda);
        runTest("Predicate Lambda", InvokeDynamicTest::testPredicateLambda);
        runTest("Supplier Lambda", InvokeDynamicTest::testSupplierLambda);

        // Capturing Variables Tests
        runTest("Lambda Capturing Single Variable", InvokeDynamicTest::testLambdaCapturingSingleVariable);
        runTest("Lambda Capturing Multiple Variables", InvokeDynamicTest::testLambdaCapturingMultipleVariables);
        runTest("Lambda Capturing Object", InvokeDynamicTest::testLambdaCapturingObject);

        // Method Reference Tests
        runTest("Static Method Reference", InvokeDynamicTest::testStaticMethodReference);
        runTest("Instance Method Reference", InvokeDynamicTest::testInstanceMethodReference);
        runTest("Constructor Reference", InvokeDynamicTest::testConstructorReference);
        runTest("Bound Method Reference", InvokeDynamicTest::testBoundMethodReference);

        // Complex Scenarios
        runTest("Nested Lambda", InvokeDynamicTest::testNestedLambda);
        runTest("Lambda Returning Lambda", InvokeDynamicTest::testLambdaReturningLambda);
        runTest("Lambda in Collection", InvokeDynamicTest::testLambdaInCollection);
        runTest("Chained Lambda Operations", InvokeDynamicTest::testChainedLambdaOperations);

        // MethodHandle Tests
        runTest("MethodHandle findStatic", InvokeDynamicTest::testMethodHandleFindStatic);
        runTest("MethodHandle findVirtual", InvokeDynamicTest::testMethodHandleFindVirtual);
        runTest("MethodHandle findConstructor", InvokeDynamicTest::testMethodHandleFindConstructor);

        // Print summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total:  " + (testsPassed + testsFailed));
        System.out.println("Success Rate: " + (testsPassed * 100 / (testsPassed + testsFailed)) + "%");
    }

    // ============= Test Helper Methods =============

    private static void runTest(String testName, Runnable test) {
        try {
            System.out.print("[TEST] " + testName + "... ");
            test.run();
            System.out.println("PASSED");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED");
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    // ============= Lambda Expression Tests =============

    // Test 1: Simple Runnable lambda
    public static void testSimpleRunnable() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "runnableTest", "()V");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");
    }

    public static void runnableTest() {
        Runnable r = () -> System.out.println("Lambda executed!");
        r.run();
    }

    // Test 2: Consumer lambda
    public static void testConsumerLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "consumerTest", "()V");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");
    }

    public static void consumerTest() {
        Consumer<String> consumer = s -> System.out.println("Consumed: " + s);
        consumer.accept("Hello World");
    }

    // Test 3: Function lambda
    public static void testFunctionLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "functionTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(10, ((StackInt) returnValue).value(), "Function should return correct value");
    }

    public static int functionTest() {
        Function<String, Integer> lengthFunc = s -> s.length();
        return lengthFunc.apply("HelloWorld");
    }

    // Test 4: BiFunction lambda
    public static void testBiFunctionLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "biFunctionTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(15, ((StackInt) returnValue).value(), "BiFunction should return correct value");
    }

    public static int biFunctionTest() {
        BiFunction<Integer, Integer, Integer> addFunc = (a, b) -> a + b;
        return addFunc.apply(7, 8);
    }

    // Test 5: Predicate lambda
    public static void testPredicateLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "predicateTest", "()Z");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(1, ((StackInt) returnValue).value(), "Predicate should return true");
    }

    public static boolean predicateTest() {
        Predicate<Integer> isPositive = n -> n > 0;
        return isPositive.test(42);
    }

    // Test 6: Supplier lambda
    public static void testSupplierLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "supplierTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(42, ((StackInt) returnValue).value(), "Supplier should return correct value");
    }

    public static int supplierTest() {
        Supplier<Integer> supplier = () -> 42;
        return supplier.get();
    }

    // ============= Capturing Variables Tests =============

    // Test 7: Lambda capturing single variable
    public static void testLambdaCapturingSingleVariable() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "captureSingleVariable", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(15, ((StackInt) returnValue).value(), "Should capture and use variable correctly");
    }

    public static int captureSingleVariable() {
        int x = 10;
        Function<Integer, Integer> addX = y -> x + y;
        return addX.apply(5);
    }

    // Test 8: Lambda capturing multiple variables
    public static void testLambdaCapturingMultipleVariables() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "captureMultipleVariables", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(60, ((StackInt) returnValue).value(), "Should capture multiple variables correctly");
    }

    public static int captureMultipleVariables() {
        int a = 10;
        int b = 20;
        int c = 30;
        Supplier<Integer> sum = () -> a + b + c;
        return sum.get();
    }

    // Test 9: Lambda capturing object
    public static void testLambdaCapturingObject() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "captureObject", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(5, ((StackInt) returnValue).value(), "Should capture object correctly");
    }

    public static int captureObject() {
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        Supplier<Integer> getSize = () -> list.size();
        return getSize.get();
    }

    // ============= Method Reference Tests =============

    // Test 10: Static method reference
    public static void testStaticMethodReference() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "staticMethodRefTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(100, ((StackInt) returnValue).value(), "Static method reference should work");
    }

    public static int staticMethodRefTest() {
        Function<String, Integer> parser = Integer::parseInt;
        return parser.apply("100");
    }

    // Test 11: Instance method reference
    public static void testInstanceMethodReference() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "instanceMethodRefTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(5, ((StackInt) returnValue).value(), "Instance method reference should work");
    }

    public static int instanceMethodRefTest() {
        Function<String, Integer> lengthFunc = String::length;
        return lengthFunc.apply("Hello");
    }

    // Test 12: Constructor reference
    public static void testConstructorReference() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "constructorRefTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(3, ((StackInt) returnValue).value(), "Constructor reference should work");
    }

    public static int constructorRefTest() {
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> list = listFactory.get();
        list.add("a");
        list.add("b");
        list.add("c");
        return list.size();
    }

    // Test 13: Bound method reference
    public static void testBoundMethodReference() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "boundMethodRefTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(11, ((StackInt) returnValue).value(), "Bound method reference should work");
    }

    public static int boundMethodRefTest() {
        String str = "Hello World";
        Supplier<Integer> lengthGetter = str::length;
        return lengthGetter.get();
    }

    // ============= Complex Scenarios Tests =============

    // Test 14: Nested lambda
    public static void testNestedLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "nestedLambdaTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(30, ((StackInt) returnValue).value(), "Nested lambda should work");
    }

    public static int nestedLambdaTest() {
        Function<Integer, Function<Integer, Integer>> adder = x -> y -> x + y;
        Function<Integer, Integer> add10 = adder.apply(10);
        return add10.apply(20);
    }

    // Test 15: Lambda returning lambda
    public static void testLambdaReturningLambda() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "lambdaReturningLambdaTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(50, ((StackInt) returnValue).value(), "Lambda returning lambda should work");
    }

    public static int lambdaReturningLambdaTest() {
        Supplier<Function<Integer, Integer>> multiplierFactory = () -> x -> x * 2;
        Function<Integer, Integer> doubler = multiplierFactory.get();
        return doubler.apply(25);
    }

    // Test 16: Lambda in collection
    public static void testLambdaInCollection() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "lambdaInCollectionTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(6, ((StackInt) returnValue).value(), "Lambda in collection should work");
    }

    public static int lambdaInCollectionTest() {
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

    // Test 17: Chained lambda operations
    public static void testChainedLambdaOperations() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "chainedLambdaTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(11, ((StackInt) returnValue).value(), "Chained lambda operations should work");
    }

    public static int chainedLambdaTest() {
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> multiplyTwo = x -> x * 2;
        Function<Integer, Integer> chained = addOne.andThen(multiplyTwo);
        return chained.apply(5); // (5 + 1) * 2 = 12
    }

    // ============= MethodHandle Tests =============

    // Test 18: MethodHandle findStatic
    public static void testMethodHandleFindStatic() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "methodHandleFindStaticTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(42, ((StackInt) returnValue).value(), "MethodHandle findStatic should work");
    }

    public static int methodHandleFindStaticTest() throws Throwable {
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodType mt = java.lang.invoke.MethodType.methodType(int.class);
        java.lang.invoke.MethodHandle mh = lookup.findStatic(InvokeDynamicTest.class, "staticHelper", mt);
        return (int) mh.invoke();
    }

    public static int staticHelper() {
        return 42;
    }

    // Test 19: MethodHandle findVirtual
    public static void testMethodHandleFindVirtual() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "methodHandleFindVirtualTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(5, ((StackInt) returnValue).value(), "MethodHandle findVirtual should work");
    }

    public static int methodHandleFindVirtualTest() throws Throwable {
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodType mt = java.lang.invoke.MethodType.methodType(int.class);
        java.lang.invoke.MethodHandle mh = lookup.findVirtual(String.class, "length", mt);
        return (int) mh.invoke("Hello");
    }

    // Test 20: MethodHandle findConstructor
    public static void testMethodHandleFindConstructor() {
        ExecutorClass testClass = manager.loadClass(context, Type.getType(InvokeDynamicTest.class));
        ExecutorClass.ResolvedMethod method = testClass.findMethod(context, "methodHandleFindConstructorTest", "()I");
        ExecutionResult result = Executor.execute(context, testClass, method.method(), null);
        assertTrue(!result.hasException(), "Should execute without exception");

        StackElement returnValue = result.getReturnValue();
        assertEqual(0, ((StackInt) returnValue).value(), "MethodHandle findConstructor should work");
    }

    public static int methodHandleFindConstructorTest() throws Throwable {
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodType mt = java.lang.invoke.MethodType.methodType(void.class);
        java.lang.invoke.MethodHandle mh = lookup.findConstructor(ArrayList.class, mt);
        List<?> list = (List<?>) mh.invoke();
        return list.size();
    }
}
