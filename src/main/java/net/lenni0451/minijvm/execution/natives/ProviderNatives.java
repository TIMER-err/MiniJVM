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
        // Bypass Provider's static initializer
        manager.registerMethodExecutor("java/security/Provider.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Provider constructor
        manager.registerMethodExecutor("java/security/Provider.<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Bypass SecureRandom static initializer
        manager.registerMethodExecutor("java/security/SecureRandom.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Security static initializer
        manager.registerMethodExecutor("java/security/Security.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Security.getProperty(String) - returns security property
        manager.registerMethodExecutor("java/security/Security.getProperty(Ljava/lang/String;)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no special security properties
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // Bypass ProviderList static initializer
        manager.registerMethodExecutor("sun/security/jca/ProviderList.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Providers static initializer
        manager.registerMethodExecutor("sun/security/jca/Providers.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass JCAUtil static initializer
        manager.registerMethodExecutor("sun/security/jca/JCAUtil.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass MessageDigest static initializer
        manager.registerMethodExecutor("java/security/MessageDigest.<clinit>()V", MethodExecutor.NOOP_VOID);

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
    }
}
