package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for javax.crypto classes.
 * Provides stub implementations for encryption/decryption operations.
 */
public class CryptoNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // MessageDigest.engineUpdate - update digest with data
        manager.registerMethodExecutor("java/security/MessageDigest.engineUpdate([BII)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // No-op: digest updates are ignored in stub implementation
            return ExecutionResult.voidResult();
        });

        // MessageDigest.engineReset - reset digest state
        manager.registerMethodExecutor("java/security/MessageDigest.engineReset()V", (context, currentClass, currentMethod, instance, arguments) -> {
            // No-op: reset is ignored in stub implementation
            return ExecutionResult.voidResult();
        });

        // MessageDigest.engineDigest - compute final digest
        manager.registerMethodExecutor("java/security/MessageDigest.engineDigest()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a dummy digest (20 bytes for SHA-1, common size)
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject digestArray = context.getExecutionManager().instantiateArray(context, byteArrayClass, 20);

            // Fill with dummy values
            ArrayObject digest = (ArrayObject) digestArray;
            for (int i = 0; i < 20; i++) {
                digest.getElements()[i] = new StackInt(i);
            }

            return returnValue(new StackObject(digestArray));
        });

        // Cipher.engineUpdate - update cipher with data
        manager.registerMethodExecutor("javax/crypto/Cipher.engineUpdate([BII[BI)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Simple pass-through: copy input to output
            StackObject inputArray = (StackObject) arguments[0];
            StackInt inputOffset = (StackInt) arguments[1];
            StackInt inputLen = (StackInt) arguments[2];
            StackObject outputArray = (StackObject) arguments[3];
            StackInt outputOffset = (StackInt) arguments[4];

            if (!inputArray.isNull() && !outputArray.isNull()) {
                ArrayObject input = (ArrayObject) inputArray.value();
                ArrayObject output = (ArrayObject) outputArray.value();
                
                // Copy bytes from input to output
                for (int i = 0; i < inputLen.value(); i++) {
                    output.getElements()[outputOffset.value() + i] = input.getElements()[inputOffset.value() + i];
                }
            }

            return returnValue(arguments[2]); // Return input length
        });

        // Cipher.engineDoFinal - finalize cipher operation
        manager.registerMethodExecutor("javax/crypto/Cipher.engineDoFinal([BII)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Simple pass-through: return input as output
            StackObject inputArray = (StackObject) arguments[0];
            StackInt inputOffset = (StackInt) arguments[1];
            StackInt inputLen = (StackInt) arguments[2];

            if (inputArray.isNull()) {
                // Return empty array
                ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
                ExecutorObject emptyArray = context.getExecutionManager().instantiateArray(context, byteArrayClass, 0);
                return returnValue(new StackObject(emptyArray));
            }

            ArrayObject input = (ArrayObject) inputArray.value();
            
            // Create output array with same length
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject outputArray = context.getExecutionManager().instantiateArray(context, byteArrayClass, inputLen.value());
            ArrayObject output = (ArrayObject) outputArray;

            // Copy bytes
            for (int i = 0; i < inputLen.value(); i++) {
                output.getElements()[i] = input.getElements()[inputOffset.value() + i];
            }

            return returnValue(new StackObject(outputArray));
        });

        // CipherSpi methods (base class for all cipher implementations)
        manager.registerMethodExecutor("javax/crypto/CipherSpi.engineUpdate([BII[BI)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Pass-through implementation
            return returnValue(arguments[2]); // Return input length
        });

        manager.registerMethodExecutor("javax/crypto/CipherSpi.engineDoFinal([BII)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            StackInt inputLen = (StackInt) arguments[2];
            ExecutorClass byteArrayClass = context.getExecutionManager().loadClass(context, Type.getType("[B"));
            ExecutorObject outputArray = context.getExecutionManager().instantiateArray(context, byteArrayClass, inputLen.value());
            return returnValue(new StackObject(outputArray));
        });
    }
}
