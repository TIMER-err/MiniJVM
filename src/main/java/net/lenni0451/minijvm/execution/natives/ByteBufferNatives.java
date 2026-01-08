package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;

import java.util.function.Consumer;

/**
 * Native method implementations for java.nio.ByteBuffer.
 */
public class ByteBufferNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass ByteBuffer static initializer - it's complex and involves Unsafe operations
        manager.registerMethodExecutor("java/nio/ByteBuffer.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass related NIO classes
        manager.registerMethodExecutor("java/nio/Buffer.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/Bits.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/HeapByteBuffer.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/DirectByteBuffer.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/MappedByteBuffer.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/charset/Charset.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/charset/CharsetEncoder.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/nio/charset/CharsetDecoder.<clinit>()V", MethodExecutor.NOOP_VOID);
    }
}
