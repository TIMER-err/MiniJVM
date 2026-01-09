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

        // Removed: Let Security's static initializer run to register crypto providers
        // manager.registerMethodExecutor("java/security/Security.<clinit>()V", MethodExecutor.NOOP_VOID);

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

        // Removed MessageDigest stubs - let JDK's real MessageDigest implementations work

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

        // Removed Cipher and KeyGenerator stubs - let JDK's real crypto implementations work

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
