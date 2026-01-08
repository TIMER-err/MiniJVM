package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackDouble;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Simplified stub implementation for java.util.Random.
 * Uses host JVM's Random to avoid complex initialization issues.
 */
public class RandomNatives implements Consumer<ExecutionManager> {

    private static final Random HOST_RANDOM = new Random();

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass Random's static initializer - it uses Unsafe which is complex
        manager.registerMethodExecutor("java/util/Random.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Random() constructor - initialize with default seed
        manager.registerMethodExecutor("java/util/Random.<init>()V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Initialize using host random
            return ExecutionResult.voidResult();
        });

        // Random(long) constructor - initialize with given seed
        manager.registerMethodExecutor("java/util/Random.<init>(J)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Seed is ignored - use host random
            return ExecutionResult.voidResult();
        });

        // next(int) - protected method used by other methods
        manager.registerMethodExecutor("java/util/Random.next(I)I", (context, currentClass, currentMethod, instance, arguments) -> {
            int bits = ((StackInt) arguments[0]).value();
            // Generate random bits
            int value = HOST_RANDOM.nextInt();
            if (bits < 32) {
                value >>>= (32 - bits);
            }
            return ExecutionResult.returnValue(new StackInt(value));
        });

        // nextInt() - returns a random int
        manager.registerMethodExecutor("java/util/Random.nextInt()I", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackInt(HOST_RANDOM.nextInt()));
        });

        // nextInt(int) - returns a random int between 0 (inclusive) and bound (exclusive)
        manager.registerMethodExecutor("java/util/Random.nextInt(I)I", (context, currentClass, currentMethod, instance, arguments) -> {
            int bound = ((StackInt) arguments[0]).value();
            if (bound <= 0) {
                // Should throw IllegalArgumentException, but for simplicity return 0
                return ExecutionResult.returnValue(new StackInt(0));
            }
            return ExecutionResult.returnValue(new StackInt(HOST_RANDOM.nextInt(bound)));
        });

        // nextLong() - returns a random long
        manager.registerMethodExecutor("java/util/Random.nextLong()J", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackLong(HOST_RANDOM.nextLong()));
        });

        // nextDouble() - returns a random double between 0.0 and 1.0
        manager.registerMethodExecutor("java/util/Random.nextDouble()D", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackDouble(HOST_RANDOM.nextDouble()));
        });

        // nextFloat() - returns a random float between 0.0 and 1.0
        manager.registerMethodExecutor("java/util/Random.nextFloat()F", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackFloat(HOST_RANDOM.nextFloat()));
        });

        // nextBoolean() - returns a random boolean
        manager.registerMethodExecutor("java/util/Random.nextBoolean()Z", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackInt(HOST_RANDOM.nextBoolean() ? 1 : 0));
        });

        // nextBytes(byte[]) - fills the array with random bytes
        manager.registerMethodExecutor("java/util/Random.nextBytes([B)V", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorObject byteArray = ((StackObject) arguments[0]).value();
            if (byteArray instanceof net.lenni0451.minijvm.object.types.ArrayObject array) {
                // Fill with random bytes
                for (int i = 0; i < array.getElements().length; i++) {
                    array.getElements()[i] = new StackInt(HOST_RANDOM.nextInt(256) - 128); // -128 to 127
                }
            }
            return ExecutionResult.voidResult();
        });

        // nextGaussian() - returns a random Gaussian distributed double
        manager.registerMethodExecutor("java/util/Random.nextGaussian()D", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackDouble(HOST_RANDOM.nextGaussian()));
        });

        // setSeed(long) - resets the seed
        manager.registerMethodExecutor("java/util/Random.setSeed(J)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Ignore - we use host random
            return ExecutionResult.voidResult();
        });
    }
}
