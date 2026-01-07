package test;

import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import org.objectweb.asm.Type;

/**
 * Test runner for InvokeDynamic support.
 * Executes lambda test methods inside MiniJVM and validates the results.
 */
public class InvokeDynamicTestRunner {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== InvokeDynamic Test Suite ===\n");

        ExecutionManager manager = new ExecutionManager(new LoaderClassProvider());
        ExecutionContext context = manager.newContext();

        // Lambda Expression Tests
        runTest(manager, context, "Simple Runnable Lambda", "testSimpleRunnable", "()I", 1);
        runTest(manager, context, "Consumer Lambda", "testConsumerLambda", "()I", 10);
        runTest(manager, context, "Function Lambda", "testFunctionLambda", "()I", 42);
        runTest(manager, context, "BiFunction Lambda", "testBiFunctionLambda", "()I", 42);
        runBooleanTest(manager, context, "Predicate Lambda", "testPredicateLambda", "()Z", true);
        runTest(manager, context, "Supplier Lambda", "testSupplierLambda", "()I", 99);

        // Capturing Variables Tests
        runTest(manager, context, "Lambda Capturing Single Variable", "testCaptureSingleVariable", "()I", 15);
        runTest(manager, context, "Lambda Capturing Multiple Variables", "testCaptureMultipleVariables", "()I", 60);
        runTest(manager, context, "Lambda Capturing Object", "testCaptureObject", "()I", 3);

        // Method Reference Tests
        runTest(manager, context, "Static Method Reference", "testStaticMethodReference", "()I", 123);
        runTest(manager, context, "Instance Method Reference", "testInstanceMethodReference", "()I", 5);
        runTest(manager, context, "Constructor Reference", "testConstructorReference", "()I", 2);
        runTest(manager, context, "Bound Method Reference", "testBoundMethodReference", "()I", 11);

        // Complex Scenarios
        runTest(manager, context, "Nested Lambda", "testNestedLambda", "()I", 30);
        runTest(manager, context, "Lambda Returning Lambda", "testLambdaReturningLambda", "()I", 42);
        runTest(manager, context, "Lambda in Collection", "testLambdaInCollection", "()I", 5);
        runTest(manager, context, "Chained Lambda Operations", "testChainedLambdaOperations", "()I", 12);

        // Print summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total:  " + (testsPassed + testsFailed));
        if (testsPassed + testsFailed > 0) {
            System.out.println("Success Rate: " + (testsPassed * 100 / (testsPassed + testsFailed)) + "%");
        }

        // Exit with appropriate code
        System.exit(testsFailed == 0 ? 0 : 1);
    }

    private static void runTest(ExecutionManager manager, ExecutionContext context,
                                String testName, String methodName, String methodDesc, int expectedValue) {
        System.out.print("[TEST] " + testName + "... ");
        try {
            ExecutorClass testClass = manager.loadClass(context, Type.getType(LambdaTestTarget.class));
            ExecutorClass.ResolvedMethod method = testClass.findMethod(context, methodName, methodDesc);

            if (method == null) {
                System.out.println("FAILED - Method not found");
                testsFailed++;
                return;
            }

            ExecutionResult result = Executor.execute(context, testClass, method.method(), null);

            if (result.hasException()) {
                System.out.println("FAILED");
                System.err.println("  Exception: " + result.getException());
                if (result.getException() != null) {
                    System.err.println("  Class: " + result.getException().getClazz().getClassNode().name);
                }
                testsFailed++;
                return;
            }

            if (!result.hasReturnValue()) {
                System.out.println("FAILED - No return value");
                testsFailed++;
                return;
            }

            StackElement returnValue = result.getReturnValue();
            int actualValue = ((StackInt) returnValue).value();

            if (actualValue == expectedValue) {
                System.out.println("PASSED (value: " + actualValue + ")");
                testsPassed++;
            } else {
                System.out.println("FAILED");
                System.err.println("  Expected: " + expectedValue + ", Actual: " + actualValue);
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("FAILED");
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void runBooleanTest(ExecutionManager manager, ExecutionContext context,
                                       String testName, String methodName, String methodDesc, boolean expectedValue) {
        System.out.print("[TEST] " + testName + "... ");
        try {
            ExecutorClass testClass = manager.loadClass(context, Type.getType(LambdaTestTarget.class));
            ExecutorClass.ResolvedMethod method = testClass.findMethod(context, methodName, methodDesc);

            if (method == null) {
                System.out.println("FAILED - Method not found");
                testsFailed++;
                return;
            }

            ExecutionResult result = Executor.execute(context, testClass, method.method(), null);

            if (result.hasException()) {
                System.out.println("FAILED");
                System.err.println("  Exception: " + result.getException());
                if (result.getException() != null) {
                    System.err.println("  Class: " + result.getException().getClazz().getClassNode().name);
                }
                testsFailed++;
                return;
            }

            if (!result.hasReturnValue()) {
                System.out.println("FAILED - No return value");
                testsFailed++;
                return;
            }

            StackElement returnValue = result.getReturnValue();
            boolean actualValue = ((StackInt) returnValue).value() != 0;

            if (actualValue == expectedValue) {
                System.out.println("PASSED (value: " + actualValue + ")");
                testsPassed++;
            } else {
                System.out.println("FAILED");
                System.err.println("  Expected: " + expectedValue + ", Actual: " + actualValue);
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("FAILED");
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }
}
