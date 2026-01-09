package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

/**
 * Stub implementation for java.security.Provider and related security classes.
 * Bypasses complex security infrastructure initialization.
 */
public class ProviderNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Removed: Let Provider's static initializer run (Locale.ENGLISH is now properly initialized)
        // manager.registerMethodExecutor("java/security/Provider.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Provider constructor
        manager.registerMethodExecutor("java/security/Provider.<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Bypass SecureRandom static initializer
        manager.registerMethodExecutor("java/security/SecureRandom.<clinit>()V", MethodExecutor.NOOP_VOID);

        // SecureRandom.getDefaultPRNG(boolean, byte[]) - bypassed
        manager.registerMethodExecutor("java/security/SecureRandom.getDefaultPRNG(Z[B)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Do nothing - just return
            return ExecutionResult.voidResult();
        });

        // SecureRandom.<init>() - constructor
        manager.registerMethodExecutor("java/security/SecureRandom.<init>()V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // SecureRandom.nextBytes([B) - fill array with random bytes
        manager.registerMethodExecutor("java/security/SecureRandom.nextBytes([B)V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Use RandomNatives' approach
            net.lenni0451.minijvm.object.ExecutorObject byteArray = ((net.lenni0451.minijvm.stack.StackObject) arguments[0]).value();
            if (byteArray instanceof net.lenni0451.minijvm.object.types.ArrayObject array) {
                java.util.Random random = new java.util.Random();
                for (int i = 0; i < array.getElements().length; i++) {
                    array.getElements()[i] = new net.lenni0451.minijvm.stack.StackInt(random.nextInt(256) - 128);
                }
            }
            return ExecutionResult.voidResult();
        });

        // Bypass Security static initializer - it requires complex file I/O and native configuration
        manager.registerMethodExecutor("java/security/Security.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Security.getProperty(String) - returns security property
        manager.registerMethodExecutor("java/security/Security.getProperty(Ljava/lang/String;)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no special security properties
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Removed: Let ProviderList and Providers initialize properly to enable real crypto providers
        // manager.registerMethodExecutor("sun/security/jca/ProviderList.<clinit>()V", MethodExecutor.NOOP_VOID);
        // manager.registerMethodExecutor("sun/security/jca/Providers.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass JCAUtil static initializer
        manager.registerMethodExecutor("sun/security/jca/JCAUtil.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass MessageDigest static initializer
        manager.registerMethodExecutor("java/security/MessageDigest.<clinit>()V", MethodExecutor.NOOP_VOID);

        // MessageDigest stubs - these would normally call native crypto implementations
        // Using simple stub implementations since real crypto requires native code

        // MessageDigest.getInstance(String) - returns MessageDigest
        manager.registerMethodExecutor("java/security/MessageDigest.getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass messageDigestClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/security/MessageDigest"));
            net.lenni0451.minijvm.object.ExecutorObject messageDigest =
                context.getExecutionManager().instantiate(context, messageDigestClass);
            return ExecutionResult.returnValue(new StackObject(messageDigest));
        });

        // MessageDigest.engineUpdate([BII)V - stub for native update
        manager.registerMethodExecutor("java/security/MessageDigest.engineUpdate([BII)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // MessageDigest.engineDigest()[B - stub for native digest
        manager.registerMethodExecutor("java/security/MessageDigest.engineDigest()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass byteArrayClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[B"));
            net.lenni0451.minijvm.stack.StackElement[] elements = new net.lenni0451.minijvm.stack.StackElement[16];
            for (int i = 0; i < 16; i++) {
                elements[i] = new net.lenni0451.minijvm.stack.StackInt(0);
            }
            net.lenni0451.minijvm.object.ExecutorObject byteArray =
                context.getExecutionManager().instantiateArray(context, byteArrayClass, elements);
            return ExecutionResult.returnValue(new StackObject(byteArray));
        });

        // MessageDigest.engineReset()V - stub for native reset
        manager.registerMethodExecutor("java/security/MessageDigest.engineReset()V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Bypass GetInstance static initializer
        manager.registerMethodExecutor("sun/security/jca/GetInstance.<clinit>()V", MethodExecutor.NOOP_VOID);

        // GetInstance.getInstance(...) - returns GetInstance.Instance
        manager.registerMethodExecutor("sun/security/jca/GetInstance.getInstance(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Lsun/security/jca/GetInstance$Instance;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a simple Instance stub
            net.lenni0451.minijvm.object.ExecutorClass instanceClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("sun/security/jca/GetInstance$Instance"));
            net.lenni0451.minijvm.object.ExecutorObject instanceObj =
                context.getExecutionManager().instantiate(context, instanceClass);
            return ExecutionResult.returnValue(new StackObject(instanceObj));
        });

        // Bypass Signature static initializer
        manager.registerMethodExecutor("java/security/Signature.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass KeyFactory static initializer
        manager.registerMethodExecutor("java/security/KeyFactory.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass AlgorithmParameters static initializer
        manager.registerMethodExecutor("java/security/AlgorithmParameters.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Cipher static initializer
        manager.registerMethodExecutor("javax/crypto/Cipher.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass KeyGenerator static initializer
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass SecretKeyFactory static initializer
        manager.registerMethodExecutor("javax/crypto/SecretKeyFactory.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Mac static initializer
        manager.registerMethodExecutor("javax/crypto/Mac.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Cipher and KeyGenerator stubs - these would normally use native crypto implementations

        // KeyGenerator.getInstance(String) - returns KeyGenerator
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.getInstance(Ljava/lang/String;)Ljavax/crypto/KeyGenerator;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass keyGenClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/KeyGenerator"));
            net.lenni0451.minijvm.object.ExecutorObject keyGen =
                context.getExecutionManager().instantiate(context, keyGenClass);
            return ExecutionResult.returnValue(new StackObject(keyGen));
        });

        // KeyGenerator.init(int) - initialize with key size
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.init(I)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // KeyGenerator.generateKey() - generate a secret key
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.generateKey()Ljavax/crypto/SecretKey;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass secretKeyClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/spec/SecretKeySpec"));
            net.lenni0451.minijvm.object.ExecutorObject secretKey =
                context.getExecutionManager().instantiate(context, secretKeyClass);
            return ExecutionResult.returnValue(new StackObject(secretKey));
        });

        // SecretKeySpec.<init>([BLjava/lang/String;) - constructor
        manager.registerMethodExecutor("javax/crypto/spec/SecretKeySpec.<init>([BLjava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // SecretKeySpec.getEncoded() - returns key bytes
        manager.registerMethodExecutor("javax/crypto/spec/SecretKeySpec.getEncoded()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass byteArrayClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[B"));
            net.lenni0451.minijvm.stack.StackElement[] elements = new net.lenni0451.minijvm.stack.StackElement[16];
            java.util.Random random = new java.util.Random(12345);
            for (int i = 0; i < 16; i++) {
                elements[i] = new net.lenni0451.minijvm.stack.StackInt(random.nextInt(256) - 128);
            }
            net.lenni0451.minijvm.object.ExecutorObject byteArray =
                context.getExecutionManager().instantiateArray(context, byteArrayClass, elements);
            return ExecutionResult.returnValue(new StackObject(byteArray));
        });

        // Cipher.getInstance(String) - returns Cipher
        manager.registerMethodExecutor("javax/crypto/Cipher.getInstance(Ljava/lang/String;)Ljavax/crypto/Cipher;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass cipherClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/Cipher"));
            net.lenni0451.minijvm.object.ExecutorObject cipher =
                context.getExecutionManager().instantiate(context, cipherClass);
            return ExecutionResult.returnValue(new StackObject(cipher));
        });

        // Cipher.init(ILjava/security/Key;) - initialize cipher
        manager.registerMethodExecutor("javax/crypto/Cipher.init(ILjava/security/Key;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Cipher.doFinal([B)[B - encrypt/decrypt (stub returns input as-is)
        manager.registerMethodExecutor("javax/crypto/Cipher.doFinal([B)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            // Simple stub: return input as-is
            return ExecutionResult.returnValue(arguments[0]);
        });

        // Bypass all sun.security.provider classes
        manager.registerMethodExecutor("sun/security/provider/SunEntries.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/Sun.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/SecureRandom.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/NativePRNG.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/SHA.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/MD5.<clinit>()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("sun/security/provider/DSA.<clinit>()V", MethodExecutor.NOOP_VOID);
    }
}
