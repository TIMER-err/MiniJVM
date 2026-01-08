package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

/**
 * Native method implementations for jdk.internal.reflect.ConstantPool.
 */
public class ConstantPoolNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // ConstantPool.getSize() - returns the size of the constant pool
        manager.registerMethodExecutor("jdk/internal/reflect/ConstantPool.getSize()I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return 0 - empty constant pool
            return ExecutionResult.returnValue(new StackInt(0));
        });

        // ConstantPool.getClassAt(int) - returns class at index
        manager.registerMethodExecutor("jdk/internal/reflect/ConstantPool.getClassAt(I)Ljava/lang/Class;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no classes in pool
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // ConstantPool.getClassAtIfLoaded(int) - returns class at index if loaded
        manager.registerMethodExecutor("jdk/internal/reflect/ConstantPool.getClassAtIfLoaded(I)Ljava/lang/Class;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no classes in pool
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // ConstantPool.getUTF8At(int) - returns UTF8 string at index
        manager.registerMethodExecutor("jdk/internal/reflect/ConstantPool.getUTF8At(I)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no strings in pool
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // ConstantPool.getIntAt(int) - returns int at index
        manager.registerMethodExecutor("jdk/internal/reflect/ConstantPool.getIntAt(I)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return 0
            return ExecutionResult.returnValue(new StackInt(0));
        });
    }
}
