package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

/**
 * Native method implementations for java.nio.charset classes.
 */
public class CharsetNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass CharsetProvider static initializer
        manager.registerMethodExecutor("java/nio/charset/spi/CharsetProvider.<clinit>()V", MethodExecutor.NOOP_VOID);

        // CharsetProvider.charsetForName(String) - returns Charset
        manager.registerMethodExecutor("java/nio/charset/spi/CharsetProvider.charsetForName(Ljava/lang/String;)Ljava/nio/charset/Charset;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no charset found
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Charset.forName(String) - returns Charset
        manager.registerMethodExecutor("java/nio/charset/Charset.forName(Ljava/lang/String;)Ljava/nio/charset/Charset;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub Charset instance (UTF-8)
            net.lenni0451.minijvm.object.ExecutorClass charsetClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/nio/charset/Charset"));
            net.lenni0451.minijvm.object.ExecutorObject charset =
                context.getExecutionManager().instantiate(context, charsetClass);
            return ExecutionResult.returnValue(new StackObject(charset));
        });

        // Charset.defaultCharset() - returns default charset
        manager.registerMethodExecutor("java/nio/charset/Charset.defaultCharset()Ljava/nio/charset/Charset;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub Charset instance
            net.lenni0451.minijvm.object.ExecutorClass charsetClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/nio/charset/Charset"));
            net.lenni0451.minijvm.object.ExecutorObject charset =
                context.getExecutionManager().instantiate(context, charsetClass);
            return ExecutionResult.returnValue(new StackObject(charset));
        });

        // Charset.encode(String) - encode string to ByteBuffer
        manager.registerMethodExecutor("java/nio/charset/Charset.encode(Ljava/lang/String;)Ljava/nio/ByteBuffer;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null for now
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Charset.decode(ByteBuffer) - decode ByteBuffer to String
        manager.registerMethodExecutor("java/nio/charset/Charset.decode(Ljava/nio/ByteBuffer;)Ljava/nio/CharBuffer;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null for now
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Charset.newEncoder() - creates a new encoder
        manager.registerMethodExecutor("java/nio/charset/Charset.newEncoder()Ljava/nio/charset/CharsetEncoder;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub CharsetEncoder instance
            net.lenni0451.minijvm.object.ExecutorClass encoderClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/nio/charset/CharsetEncoder"));
            net.lenni0451.minijvm.object.ExecutorObject encoder =
                context.getExecutionManager().instantiate(context, encoderClass);
            return ExecutionResult.returnValue(new StackObject(encoder));
        });

        // Charset.newDecoder() - creates a new decoder
        manager.registerMethodExecutor("java/nio/charset/Charset.newDecoder()Ljava/nio/charset/CharsetDecoder;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub CharsetDecoder instance
            net.lenni0451.minijvm.object.ExecutorClass decoderClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/nio/charset/CharsetDecoder"));
            net.lenni0451.minijvm.object.ExecutorObject decoder =
                context.getExecutionManager().instantiate(context, decoderClass);
            return ExecutionResult.returnValue(new StackObject(decoder));
        });
    }
}
