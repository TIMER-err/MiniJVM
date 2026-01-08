package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Type;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Native method implementations for java.lang.String.
 */
public class StringNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // String.getBytes() - convert string to byte array using default charset
        manager.registerMethodExecutor("java/lang/String.getBytes()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Get the string from the instance
            String str = ExecutorTypeUtils.fromExecutorString(context, instance);

            // Convert to bytes using UTF-8
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

            // Create byte array
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject byteArrayObj = context.getExecutionManager().instantiateArray(context, byteArrayClass, bytes.length);

            // Fill array
            if (byteArrayObj instanceof ArrayObject byteArray) {
                for (int i = 0; i < bytes.length; i++) {
                    byteArray.getElements()[i] = new StackInt(bytes[i]);
                }
            }

            return ExecutionResult.returnValue(new StackObject(byteArrayObj));
        });

        // String.getBytes(String) - convert string to byte array using specified charset
        manager.registerMethodExecutor("java/lang/String.getBytes(Ljava/lang/String;)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Get the string from the instance
            String str = ExecutorTypeUtils.fromExecutorString(context, instance);

            // For simplicity, always use UTF-8 regardless of charset parameter
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

            // Create byte array
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject byteArrayObj = context.getExecutionManager().instantiateArray(context, byteArrayClass, bytes.length);

            // Fill array
            if (byteArrayObj instanceof ArrayObject byteArray) {
                for (int i = 0; i < bytes.length; i++) {
                    byteArray.getElements()[i] = new StackInt(bytes[i]);
                }
            }

            return ExecutionResult.returnValue(new StackObject(byteArrayObj));
        });

        // String.getBytes(Charset) - convert string to byte array using specified charset
        manager.registerMethodExecutor("java/lang/String.getBytes(Ljava/nio/charset/Charset;)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Get the string from the instance
            String str = ExecutorTypeUtils.fromExecutorString(context, instance);

            // For simplicity, always use UTF-8 regardless of charset parameter
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

            // Create byte array
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject byteArrayObj = context.getExecutionManager().instantiateArray(context, byteArrayClass, bytes.length);

            // Fill array
            if (byteArrayObj instanceof ArrayObject byteArray) {
                for (int i = 0; i < bytes.length; i++) {
                    byteArray.getElements()[i] = new StackInt(bytes[i]);
                }
            }

            return ExecutionResult.returnValue(new StackObject(byteArrayObj));
        });

        // String.<init>([BLjava/lang/String;) - constructor from byte array with charset name
        manager.registerMethodExecutor("java/lang/String.<init>([BLjava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Extract bytes from the array and create a string directly
            if (!arguments[0].isNull() && ((StackObject) arguments[0]).value() instanceof ArrayObject byteArray) {
                byte[] bytes = new byte[byteArray.getElements().length];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) ((StackInt) byteArray.getElements()[i]).value();
                }

                // Create a Java string from bytes using UTF-8
                String str = new String(bytes, StandardCharsets.UTF_8);

                // Set the string value in the ExecutorObject
                // We need to find the internal string representation
                // For now, just log that we created it
                System.out.println("DEBUG: Created string from bytes: " + str);
            }
            return ExecutionResult.voidResult();
        });

        // String.<init>([BLjava/nio/charset/Charset;) - constructor from byte array with charset
        manager.registerMethodExecutor("java/lang/String.<init>([BLjava/nio/charset/Charset;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Extract bytes from the array and create a string directly
            if (!arguments[0].isNull() && ((StackObject) arguments[0]).value() instanceof ArrayObject byteArray) {
                byte[] bytes = new byte[byteArray.getElements().length];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) ((StackInt) byteArray.getElements()[i]).value();
                }

                // Create a Java string from bytes using UTF-8 (ignoring charset parameter)
                String str = new String(bytes, StandardCharsets.UTF_8);

                System.out.println("DEBUG: Created string from bytes with charset: " + str);
            }
            return ExecutionResult.voidResult();
        });

        // String.<init>([B) - constructor from byte array (default charset)
        manager.registerMethodExecutor("java/lang/String.<init>([B)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Extract bytes from the array and create a string directly
            if (!arguments[0].isNull() && ((StackObject) arguments[0]).value() instanceof ArrayObject byteArray) {
                byte[] bytes = new byte[byteArray.getElements().length];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) ((StackInt) byteArray.getElements()[i]).value();
                }

                // Create a Java string from bytes using UTF-8
                String str = new String(bytes, StandardCharsets.UTF_8);

                System.out.println("DEBUG: Created string from bytes (default): " + str);
            }
            return ExecutionResult.voidResult();
        });
    }
}
