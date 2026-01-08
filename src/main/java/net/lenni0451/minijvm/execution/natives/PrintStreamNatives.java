package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

/**
 * Native method implementations for java.io.PrintStream.
 */
public class PrintStreamNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // PrintStream.println(String) - print string with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(Ljava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            if (!arguments[0].isNull()) {
                String message = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[0]).value());
                System.out.println(message);
            } else {
                System.out.println("null");
            }
            return ExecutionResult.voidResult();
        });

        // PrintStream.println() - print newline
        manager.registerMethodExecutor("java/io/PrintStream.println()V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println();
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(String) - print string without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(Ljava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            if (!arguments[0].isNull()) {
                String message = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[0]).value());
                System.out.print(message);
            } else {
                System.out.print("null");
            }
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(Object) - print object with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(Ljava/lang/Object;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            if (!arguments[0].isNull()) {
                // Call toString on the object
                net.lenni0451.minijvm.object.ExecutorObject obj = ((StackObject) arguments[0]).value();
                net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod toStringMethod =
                    obj.getClazz().findMethod(context, "toString", "()Ljava/lang/String;");
                if (toStringMethod != null) {
                    ExecutionResult result = net.lenni0451.minijvm.execution.Executor.execute(context,
                        toStringMethod.owner(), toStringMethod.method(), obj);
                    if (result.hasReturnValue() && !result.getReturnValue().isNull()) {
                        String message = ExecutorTypeUtils.fromExecutorString(context,
                            ((StackObject) result.getReturnValue()).value());
                        System.out.println(message);
                    } else {
                        System.out.println("null");
                    }
                } else {
                    System.out.println(obj.getClazz().getClassNode().name + "@" + System.identityHashCode(obj));
                }
            } else {
                System.out.println("null");
            }
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(Object) - print object without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(Ljava/lang/Object;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            if (!arguments[0].isNull()) {
                // Call toString on the object
                net.lenni0451.minijvm.object.ExecutorObject obj = ((StackObject) arguments[0]).value();
                net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod toStringMethod =
                    obj.getClazz().findMethod(context, "toString", "()Ljava/lang/String;");
                if (toStringMethod != null) {
                    ExecutionResult result = net.lenni0451.minijvm.execution.Executor.execute(context,
                        toStringMethod.owner(), toStringMethod.method(), obj);
                    if (result.hasReturnValue() && !result.getReturnValue().isNull()) {
                        String message = ExecutorTypeUtils.fromExecutorString(context,
                            ((StackObject) result.getReturnValue()).value());
                        System.out.print(message);
                    } else {
                        System.out.print("null");
                    }
                } else {
                    System.out.print(obj.getClazz().getClassNode().name + "@" + System.identityHashCode(obj));
                }
            } else {
                System.out.print("null");
            }
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(int) - print int with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(I)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println(((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(int) - print int without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(I)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print(((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(long) - print long with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(J)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println(((net.lenni0451.minijvm.stack.StackLong) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(long) - print long without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(J)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print(((net.lenni0451.minijvm.stack.StackLong) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(double) - print double with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(D)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println(((net.lenni0451.minijvm.stack.StackDouble) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(double) - print double without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(D)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print(((net.lenni0451.minijvm.stack.StackDouble) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(float) - print float with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(F)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println(((net.lenni0451.minijvm.stack.StackFloat) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(float) - print float without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(F)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print(((net.lenni0451.minijvm.stack.StackFloat) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(boolean) - print boolean with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(Z)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println(((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value() != 0);
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(boolean) - print boolean without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(Z)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print(((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value() != 0);
            return ExecutionResult.voidResult();
        });

        // PrintStream.println(char) - print char with newline
        manager.registerMethodExecutor("java/io/PrintStream.println(C)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.println((char) ((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.print(char) - print char without newline
        manager.registerMethodExecutor("java/io/PrintStream.print(C)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.print((char) ((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        // PrintStream.flush() - flush stream
        manager.registerMethodExecutor("java/io/PrintStream.flush()V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.flush();
            return ExecutionResult.voidResult();
        });

        // PrintStream write methods
        manager.registerMethodExecutor("java/io/PrintStream.write(I)V", (context, currentClass, currentMethod, instance, arguments) -> {
            System.out.write(((net.lenni0451.minijvm.stack.StackInt) arguments[0]).value());
            return ExecutionResult.voidResult();
        });

        manager.registerMethodExecutor("java/io/PrintStream.write([BII)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Just return - we're not actually writing
            return ExecutionResult.voidResult();
        });
    }
}
