package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;

import java.util.function.Consumer;

/**
 * Native method implementations for jdk.internal.misc.VM.
 */
public class VMNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // VM.initialize() - initialize the VM
        manager.registerMethodExecutor(
            "jdk/internal/misc/VM.initialize()V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // In a real JVM, this would initialize various VM subsystems
                // For MiniJVM, we can just return void as most initialization is already done
                return ExecutionResult.voidResult();
            }
        );

        // VM.getSavedProperty(String) - get saved system property
        manager.registerMethodExecutor(
            "jdk/internal/misc/VM.getSavedProperty(Ljava/lang/String;)Ljava/lang/String;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Return null for all properties in MiniJVM
                return ExecutionResult.returnValue(net.lenni0451.minijvm.stack.StackObject.NULL);
            }
        );

        // VM.latestUserDefinedLoader() - get the latest user-defined class loader
        manager.registerMethodExecutor(
            "jdk/internal/misc/VM.latestUserDefinedLoader()Ljava/lang/ClassLoader;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Return null (system class loader)
                return ExecutionResult.returnValue(net.lenni0451.minijvm.stack.StackObject.NULL);
            }
        );

        // Bypass Blocker static initializer
        manager.registerMethodExecutor("jdk/internal/misc/Blocker.<clinit>()V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.voidResult();
            }
        );

        // Blocker.begin() - begin blocking operation
        manager.registerMethodExecutor("jdk/internal/misc/Blocker.begin()J",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackLong(0L));
            }
        );

        // Blocker.end(long) - end blocking operation
        manager.registerMethodExecutor("jdk/internal/misc/Blocker.end(J)V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.voidResult();
            }
        );
    }
}
