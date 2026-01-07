package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

/**
 * Native method implementations for java.util.concurrent.atomic classes.
 */
public class AtomicNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // AtomicLong.VMSupportsCS8() - Check if VM supports 8-byte compare-and-swap
        // Return true to indicate support
        manager.registerMethodExecutor(
            "java/util/concurrent/atomic/AtomicLong.VMSupportsCS8()Z",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Return true - MiniJVM supports 8-byte operations
                return ExecutionResult.returnValue(new StackInt(1));
            }
        );

        // AtomicInteger.getAndAddInt(Object, long, int) - native implementation
        manager.registerMethodExecutor(
            "java/util/concurrent/atomic/AtomicInteger.getAndAddInt(Ljava/lang/Object;JI)I",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // For simplicity, just return 0
                // In a real implementation, this would use Unsafe to do atomic operations
                return ExecutionResult.returnValue(new StackInt(0));
            }
        );

        // AtomicLong.getAndAddLong(Object, long, long) - native implementation
        manager.registerMethodExecutor(
            "java/util/concurrent/atomic/AtomicLong.getAndAddLong(Ljava/lang/Object;JJ)J",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // For simplicity, just return 0L
                return ExecutionResult.returnValue(new StackLong(0L));
            }
        );
    }
}
