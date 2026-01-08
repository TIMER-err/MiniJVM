package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;

import java.util.function.Consumer;

public class FileInputStreamNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/io/FileInputStream.initIDs()V", MethodExecutor.NOOP_VOID);

        // open0(String) - open file
        manager.registerMethodExecutor("java/io/FileInputStream.open0(Ljava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Just return - don't actually open the file
            return ExecutionResult.voidResult();
        });

        // read0() - read single byte
        manager.registerMethodExecutor("java/io/FileInputStream.read0()I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return -1 (EOF)
            return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackInt(-1));
        });

        // readBytes([BII) - read bytes into array
        manager.registerMethodExecutor("java/io/FileInputStream.readBytes([BII)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return -1 (EOF)
            return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackInt(-1));
        });

        // close0() - close file
        manager.registerMethodExecutor("java/io/FileInputStream.close0()V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });
    }

}
