package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simplified stub implementation for java.lang.ThreadLocal.
 * Since MiniJVM is single-threaded, we use a simple Map to store values.
 */
public class ThreadLocalNatives implements Consumer<ExecutionManager> {

    private static final Map<Object, Object> THREAD_LOCAL_STORAGE = new HashMap<>();

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass ThreadLocal's static initializer
        manager.registerMethodExecutor("java/lang/ThreadLocal.<clinit>()V", MethodExecutor.NOOP_VOID);

        // ThreadLocal() constructor
        manager.registerMethodExecutor("java/lang/ThreadLocal.<init>()V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // get() - returns the value in the current thread's copy
        manager.registerMethodExecutor("java/lang/ThreadLocal.get()Ljava/lang/Object;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorObject threadLocal = (net.lenni0451.minijvm.object.ExecutorObject) instance;
            Object value = THREAD_LOCAL_STORAGE.get(threadLocal);

            if (value == null) {
                // Call initialValue() if value is not set
                net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod initialValue =
                    threadLocal.getClazz().findMethod(context, "initialValue", "()Ljava/lang/Object;");
                if (initialValue != null) {
                    ExecutionResult result = net.lenni0451.minijvm.execution.Executor.execute(
                        context, initialValue.owner(), initialValue.method(), threadLocal);
                    if (result.hasReturnValue()) {
                        value = result.getReturnValue();
                        THREAD_LOCAL_STORAGE.put(threadLocal, value);
                        return ExecutionResult.returnValue((net.lenni0451.minijvm.stack.StackElement) value);
                    }
                }
                return ExecutionResult.returnValue(StackObject.NULL);
            }

            return ExecutionResult.returnValue((net.lenni0451.minijvm.stack.StackElement) value);
        });

        // set(T) - sets the value in the current thread's copy
        manager.registerMethodExecutor("java/lang/ThreadLocal.set(Ljava/lang/Object;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorObject threadLocal = (net.lenni0451.minijvm.object.ExecutorObject) instance;
            net.lenni0451.minijvm.stack.StackElement value = arguments[0];
            THREAD_LOCAL_STORAGE.put(threadLocal, value);
            return ExecutionResult.voidResult();
        });

        // remove() - removes the value from the current thread's copy
        manager.registerMethodExecutor("java/lang/ThreadLocal.remove()V", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorObject threadLocal = (net.lenni0451.minijvm.object.ExecutorObject) instance;
            THREAD_LOCAL_STORAGE.remove(threadLocal);
            return ExecutionResult.voidResult();
        });

        // initialValue() - default implementation returns null
        manager.registerMethodExecutor("java/lang/ThreadLocal.initialValue()Ljava/lang/Object;", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // nextHashCode() - returns next hash code for ThreadLocal
        manager.registerMethodExecutor("java/lang/ThreadLocal.nextHashCode()I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a simple incrementing value
            return ExecutionResult.returnValue(new StackInt(THREAD_LOCAL_STORAGE.size()));
        });

        // setInitialValue() - internal method
        manager.registerMethodExecutor("java/lang/ThreadLocal.setInitialValue()Ljava/lang/Object;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Call initialValue() and store it
            net.lenni0451.minijvm.object.ExecutorObject threadLocal = (net.lenni0451.minijvm.object.ExecutorObject) instance;
            net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod initialValue =
                threadLocal.getClazz().findMethod(context, "initialValue", "()Ljava/lang/Object;");
            if (initialValue != null) {
                ExecutionResult result = net.lenni0451.minijvm.execution.Executor.execute(
                    context, initialValue.owner(), initialValue.method(), threadLocal);
                if (result.hasReturnValue()) {
                    net.lenni0451.minijvm.stack.StackElement value = result.getReturnValue();
                    THREAD_LOCAL_STORAGE.put(threadLocal, value);
                    return ExecutionResult.returnValue(value);
                }
            }
            return ExecutionResult.returnValue(StackObject.NULL);
        });
    }
}
