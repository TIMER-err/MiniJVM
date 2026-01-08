package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

/**
 * Stub implementation for java.util.Properties.
 */
public class PropertiesNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Properties constructor
        manager.registerMethodExecutor("java/util/Properties.<init>()V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Properties.getProperty(String) - returns property value
        manager.registerMethodExecutor("java/util/Properties.getProperty(Ljava/lang/String;)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0].isNull()) {
                return ExecutionResult.returnValue(StackObject.NULL);
            }
            String key = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[0]).value());
            String value = System.getProperty(key);
            if (value == null) {
                return ExecutionResult.returnValue(StackObject.NULL);
            }
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, value));
        });

        // Properties.getProperty(String, String) - returns property value with default
        manager.registerMethodExecutor("java/util/Properties.getProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0].isNull()) {
                return ExecutionResult.returnValue(arguments[1]);
            }
            String key = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[0]).value());
            String value = System.getProperty(key);
            if (value == null) {
                return ExecutionResult.returnValue(arguments[1]);
            }
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, value));
        });

        // Properties.setProperty(String, String) - sets property
        manager.registerMethodExecutor("java/util/Properties.setProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null (no old value)
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Properties.containsKey(Object) - checks if key exists
        manager.registerMethodExecutor("java/util/Properties.containsKey(Ljava/lang/Object;)Z", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackInt(0));
        });
    }
}
