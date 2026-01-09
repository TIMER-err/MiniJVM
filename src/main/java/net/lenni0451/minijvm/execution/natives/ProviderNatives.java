package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Stub implementation for java.security.Provider and related security classes.
 * Bypasses complex security infrastructure initialization but uses real host JVM crypto.
 */
public class ProviderNatives implements Consumer<ExecutionManager> {

    // Storage for real crypto objects mapped to MiniJVM objects
    private static final Map<Object, Object> cryptoObjects = new HashMap<>();

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

        // MessageDigest stubs - using real host JVM crypto implementations

        // MessageDigest.getInstance(String) - returns MessageDigest
        manager.registerMethodExecutor("java/security/MessageDigest.getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                // Get algorithm name from MiniJVM string
                StackObject algorithmObj = (StackObject) arguments[0];
                String algorithm = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, (net.lenni0451.minijvm.object.ExecutorObject) algorithmObj.value());

                // Create real MessageDigest using host JVM
                MessageDigest realDigest = MessageDigest.getInstance(algorithm);

                // Create MiniJVM MessageDigest object
                net.lenni0451.minijvm.object.ExecutorClass messageDigestClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/security/MessageDigest"));
                net.lenni0451.minijvm.object.ExecutorObject messageDigest =
                    context.getExecutionManager().instantiate(context, messageDigestClass);

                // Store the real digest mapped to MiniJVM object
                cryptoObjects.put(messageDigest, realDigest);

                return ExecutionResult.returnValue(new StackObject(messageDigest));
            } catch (Exception e) {
                // Return a stub if algorithm not available
                net.lenni0451.minijvm.object.ExecutorClass messageDigestClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/security/MessageDigest"));
                net.lenni0451.minijvm.object.ExecutorObject messageDigest =
                    context.getExecutionManager().instantiate(context, messageDigestClass);
                return ExecutionResult.returnValue(new StackObject(messageDigest));
            }
        });

        // MessageDigest.engineUpdate([BII)V - delegate to real MessageDigest
        manager.registerMethodExecutor("java/security/MessageDigest.engineUpdate([BII)V", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                MessageDigest realDigest = (MessageDigest) cryptoObjects.get(instance);
                if (realDigest != null && arguments[0] instanceof StackObject) {
                    StackObject byteArrayObj = (StackObject) arguments[0];
                    if (byteArrayObj.value() instanceof ArrayObject) {
                        ArrayObject byteArray = (ArrayObject) byteArrayObj.value();
                        int offset = ((StackInt) arguments[1]).value();
                        int length = ((StackInt) arguments[2]).value();

                        // Convert MiniJVM byte array to real byte array
                        byte[] bytes = new byte[length];
                        for (int i = 0; i < length; i++) {
                            bytes[i] = (byte) ((StackInt) byteArray.getElements()[offset + i]).value();
                        }

                        realDigest.update(bytes);
                    }
                }
            } catch (Exception e) {
                // Silently ignore if real digest not available
            }
            return ExecutionResult.voidResult();
        });

        // MessageDigest.engineDigest()[B - delegate to real MessageDigest
        manager.registerMethodExecutor("java/security/MessageDigest.engineDigest()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                MessageDigest realDigest = (MessageDigest) cryptoObjects.get(instance);
                if (realDigest != null) {
                    byte[] hash = realDigest.digest();

                    // Convert real byte array to MiniJVM byte array
                    net.lenni0451.minijvm.object.ExecutorClass byteArrayClass =
                        context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[B"));
                    net.lenni0451.minijvm.stack.StackElement[] elements = new net.lenni0451.minijvm.stack.StackElement[hash.length];
                    for (int i = 0; i < hash.length; i++) {
                        elements[i] = new net.lenni0451.minijvm.stack.StackInt(hash[i]);
                    }
                    net.lenni0451.minijvm.object.ExecutorObject byteArray =
                        context.getExecutionManager().instantiateArray(context, byteArrayClass, elements);
                    return ExecutionResult.returnValue(new StackObject(byteArray));
                }
            } catch (Exception e) {
                // Fall through to stub implementation
            }

            // Stub fallback: return dummy hash
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

        // MessageDigest.engineReset()V - delegate to real MessageDigest
        manager.registerMethodExecutor("java/security/MessageDigest.engineReset()V", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                MessageDigest realDigest = (MessageDigest) cryptoObjects.get(instance);
                if (realDigest != null) {
                    realDigest.reset();
                }
            } catch (Exception e) {
                // Silently ignore
            }
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

        // Cipher and KeyGenerator stubs - using real host JVM crypto implementations

        // KeyGenerator.getInstance(String) - returns KeyGenerator
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.getInstance(Ljava/lang/String;)Ljavax/crypto/KeyGenerator;", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                StackObject algorithmObj = (StackObject) arguments[0];
                String algorithm = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, (net.lenni0451.minijvm.object.ExecutorObject) algorithmObj.value());

                KeyGenerator realKeyGen = KeyGenerator.getInstance(algorithm);

                net.lenni0451.minijvm.object.ExecutorClass keyGenClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/KeyGenerator"));
                net.lenni0451.minijvm.object.ExecutorObject keyGen =
                    context.getExecutionManager().instantiate(context, keyGenClass);

                cryptoObjects.put(keyGen, realKeyGen);
                return ExecutionResult.returnValue(new StackObject(keyGen));
            } catch (Exception e) {
                net.lenni0451.minijvm.object.ExecutorClass keyGenClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/KeyGenerator"));
                net.lenni0451.minijvm.object.ExecutorObject keyGen =
                    context.getExecutionManager().instantiate(context, keyGenClass);
                return ExecutionResult.returnValue(new StackObject(keyGen));
            }
        });

        // KeyGenerator.init(int) - initialize with key size
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.init(I)V", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                KeyGenerator realKeyGen = (KeyGenerator) cryptoObjects.get(instance);
                if (realKeyGen != null) {
                    int keySize = ((StackInt) arguments[0]).value();
                    realKeyGen.init(keySize);
                }
            } catch (Exception e) {
                // Silently ignore
            }
            return ExecutionResult.voidResult();
        });

        // KeyGenerator.generateKey() - generate a secret key
        manager.registerMethodExecutor("javax/crypto/KeyGenerator.generateKey()Ljavax/crypto/SecretKey;", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                KeyGenerator realKeyGen = (KeyGenerator) cryptoObjects.get(instance);
                if (realKeyGen != null) {
                    SecretKey realKey = realKeyGen.generateKey();

                    net.lenni0451.minijvm.object.ExecutorClass secretKeyClass =
                        context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/spec/SecretKeySpec"));
                    net.lenni0451.minijvm.object.ExecutorObject secretKey =
                        context.getExecutionManager().instantiate(context, secretKeyClass);

                    cryptoObjects.put(secretKey, realKey);
                    return ExecutionResult.returnValue(new StackObject(secretKey));
                }
            } catch (Exception e) {
                // Fall through
            }

            net.lenni0451.minijvm.object.ExecutorClass secretKeyClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/spec/SecretKeySpec"));
            net.lenni0451.minijvm.object.ExecutorObject secretKey =
                context.getExecutionManager().instantiate(context, secretKeyClass);
            return ExecutionResult.returnValue(new StackObject(secretKey));
        });

        // SecretKeySpec.<init>([BLjava/lang/String;) - constructor
        manager.registerMethodExecutor("javax/crypto/spec/SecretKeySpec.<init>([BLjava/lang/String;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                if (arguments[0] instanceof StackObject && arguments[1] instanceof StackObject) {
                    StackObject keyBytesObj = (StackObject) arguments[0];
                    StackObject algorithmObj = (StackObject) arguments[1];

                    if (keyBytesObj.value() instanceof ArrayObject) {
                        ArrayObject keyBytesArray = (ArrayObject) keyBytesObj.value();
                        String algorithm = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, (net.lenni0451.minijvm.object.ExecutorObject) algorithmObj.value());

                        // Convert MiniJVM byte array to real byte array
                        byte[] keyBytes = new byte[keyBytesArray.getElements().length];
                        for (int i = 0; i < keyBytes.length; i++) {
                            keyBytes[i] = (byte) ((StackInt) keyBytesArray.getElements()[i]).value();
                        }

                        SecretKey realKey = new SecretKeySpec(keyBytes, algorithm);
                        cryptoObjects.put(instance, realKey);
                    }
                }
            } catch (Exception e) {
                // Silently ignore
            }
            return ExecutionResult.voidResult();
        });

        // SecretKeySpec.getEncoded() - returns key bytes
        manager.registerMethodExecutor("javax/crypto/spec/SecretKeySpec.getEncoded()[B", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                SecretKey realKey = (SecretKey) cryptoObjects.get(instance);
                if (realKey != null) {
                    byte[] keyBytes = realKey.getEncoded();

                    net.lenni0451.minijvm.object.ExecutorClass byteArrayClass =
                        context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[B"));
                    net.lenni0451.minijvm.stack.StackElement[] elements = new net.lenni0451.minijvm.stack.StackElement[keyBytes.length];
                    for (int i = 0; i < keyBytes.length; i++) {
                        elements[i] = new net.lenni0451.minijvm.stack.StackInt(keyBytes[i]);
                    }
                    net.lenni0451.minijvm.object.ExecutorObject byteArray =
                        context.getExecutionManager().instantiateArray(context, byteArrayClass, elements);
                    return ExecutionResult.returnValue(new StackObject(byteArray));
                }
            } catch (Exception e) {
                // Fall through
            }

            // Stub fallback
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
            try {
                StackObject transformationObj = (StackObject) arguments[0];
                String transformation = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, (net.lenni0451.minijvm.object.ExecutorObject) transformationObj.value());

                Cipher realCipher = Cipher.getInstance(transformation);

                net.lenni0451.minijvm.object.ExecutorClass cipherClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/Cipher"));
                net.lenni0451.minijvm.object.ExecutorObject cipher =
                    context.getExecutionManager().instantiate(context, cipherClass);

                cryptoObjects.put(cipher, realCipher);
                return ExecutionResult.returnValue(new StackObject(cipher));
            } catch (Exception e) {
                net.lenni0451.minijvm.object.ExecutorClass cipherClass =
                    context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("javax/crypto/Cipher"));
                net.lenni0451.minijvm.object.ExecutorObject cipher =
                    context.getExecutionManager().instantiate(context, cipherClass);
                return ExecutionResult.returnValue(new StackObject(cipher));
            }
        });

        // Cipher.init(ILjava/security/Key;) - initialize cipher
        manager.registerMethodExecutor("javax/crypto/Cipher.init(ILjava/security/Key;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                Cipher realCipher = (Cipher) cryptoObjects.get(instance);
                if (realCipher != null && arguments[1] instanceof StackObject) {
                    int opmode = ((StackInt) arguments[0]).value();
                    StackObject keyObj = (StackObject) arguments[1];

                    SecretKey realKey = (SecretKey) cryptoObjects.get(keyObj.value());
                    if (realKey != null) {
                        realCipher.init(opmode, realKey);
                    }
                }
            } catch (Exception e) {
                // Silently ignore
            }
            return ExecutionResult.voidResult();
        });

        // Cipher.doFinal([B)[B - encrypt/decrypt using real cipher
        manager.registerMethodExecutor("javax/crypto/Cipher.doFinal([B)[B", (context, currentClass, currentMethod, instance, arguments) -> {
            try {
                Cipher realCipher = (Cipher) cryptoObjects.get(instance);
                if (realCipher != null && arguments[0] instanceof StackObject) {
                    StackObject inputObj = (StackObject) arguments[0];
                    if (inputObj.value() instanceof ArrayObject) {
                        ArrayObject inputArray = (ArrayObject) inputObj.value();

                        // Convert MiniJVM byte array to real byte array
                        byte[] input = new byte[inputArray.getElements().length];
                        for (int i = 0; i < input.length; i++) {
                            input[i] = (byte) ((StackInt) inputArray.getElements()[i]).value();
                        }

                        byte[] output = realCipher.doFinal(input);

                        // Convert real byte array to MiniJVM byte array
                        net.lenni0451.minijvm.object.ExecutorClass byteArrayClass =
                            context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[B"));
                        net.lenni0451.minijvm.stack.StackElement[] elements = new net.lenni0451.minijvm.stack.StackElement[output.length];
                        for (int i = 0; i < output.length; i++) {
                            elements[i] = new net.lenni0451.minijvm.stack.StackInt(output[i]);
                        }
                        net.lenni0451.minijvm.object.ExecutorObject byteArray =
                            context.getExecutionManager().instantiateArray(context, byteArrayClass, elements);
                        return ExecutionResult.returnValue(new StackObject(byteArray));
                    }
                }
            } catch (Exception e) {
                // Fall through to stub
            }

            // Stub fallback: return input as-is
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
