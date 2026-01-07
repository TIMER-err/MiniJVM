package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ReflectionNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/reflect/Reflection.getCallerClass()Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutionContext.StackFrame[] stackFrames = executionContext.getStackFrames();
            ExecutionContext.StackFrame firstFrame = stackFrames[stackFrames.length - 1]; //TODO: What to do if no class was found?
            for (int i = stackFrames.length - 1; i >= 0; i--) {
                ExecutionContext.StackFrame stackFrame = stackFrames[i];
                //TODO: What are the actual checks?
                if (stackFrame.isNativeMethod()) continue;
                if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/reflect/")) continue;
                if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/invoke/")) continue;
                firstFrame = stackFrame;
                break;
            }
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateClass(executionContext, firstFrame.getExecutorClass())));
        });

        // Class.getDeclaredFields0(boolean) - returns declared fields
        manager.registerMethodExecutor("java/lang/Class.getDeclaredFields0(Z)[Ljava/lang/reflect/Field;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // For simplicity, return an empty Field array
            // In a full implementation, this would return actual fields of the class
            net.lenni0451.minijvm.object.ExecutorClass arrayClass =
                executionContext.getExecutionManager().loadClass(executionContext, org.objectweb.asm.Type.getType("[Ljava/lang/reflect/Field;"));
            net.lenni0451.minijvm.object.types.ArrayObject emptyArray =
                new net.lenni0451.minijvm.object.types.ArrayObject(executionContext, arrayClass, new net.lenni0451.minijvm.stack.StackElement[0]);
            return returnValue(new StackObject(emptyArray));
        });

        // Class.getDeclaredMethods0(boolean) - returns declared methods
        manager.registerMethodExecutor("java/lang/Class.getDeclaredMethods0(Z)[Ljava/lang/reflect/Method;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return an empty Method array
            net.lenni0451.minijvm.object.ExecutorClass arrayClass =
                executionContext.getExecutionManager().loadClass(executionContext, org.objectweb.asm.Type.getType("[Ljava/lang/reflect/Method;"));
            net.lenni0451.minijvm.object.types.ArrayObject emptyArray =
                new net.lenni0451.minijvm.object.types.ArrayObject(executionContext, arrayClass, new net.lenni0451.minijvm.stack.StackElement[0]);
            return returnValue(new StackObject(emptyArray));
        });

        // Class.getDeclaredConstructors0(boolean) - returns declared constructors
        manager.registerMethodExecutor("java/lang/Class.getDeclaredConstructors0(Z)[Ljava/lang/reflect/Constructor;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return an empty Constructor array
            net.lenni0451.minijvm.object.ExecutorClass arrayClass =
                executionContext.getExecutionManager().loadClass(executionContext, org.objectweb.asm.Type.getType("[Ljava/lang/reflect/Constructor;"));
            net.lenni0451.minijvm.object.types.ArrayObject emptyArray =
                new net.lenni0451.minijvm.object.types.ArrayObject(executionContext, arrayClass, new net.lenni0451.minijvm.stack.StackElement[0]);
            return returnValue(new StackObject(emptyArray));
        });

        // Class.getDeclaredField0(String) - returns a declared field by name
        manager.registerMethodExecutor("java/lang/Class.getDeclaredField0(Ljava/lang/String;)Ljava/lang/reflect/Field;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // For now, throw NoSuchFieldException to match expected behavior when field not found
            // In a full implementation, this would return the actual Field object
            return net.lenni0451.minijvm.utils.ExceptionUtils.newException(executionContext,
                org.objectweb.asm.Type.getObjectType("java/lang/NoSuchFieldException"),
                "Field not found (stub implementation)");
        });
    }

}
