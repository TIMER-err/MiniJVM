package test;

import net.lenni0451.commons.asm.provider.DelegatingClassProvider;
import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.commons.asm.provider.io.JarFileClassProvider;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * String deobfuscator for analyzing JAR files and extracting decrypted strings
 * from InvokeDynamic and InvokeStatic calls.
 */
public class StringDeobfuscator {

    private final ExecutionManager manager;
    private final ExecutionContext context;
    private final Map<String, Map<String, List<String>>> resultMap = new LinkedHashMap<>();
    private int successCount = 0;
    private int failureCount = 0;

    public StringDeobfuscator(File jarFile) throws Exception {
        System.out.println("=== String Deobfuscator ===\n");
        System.out.println("Loading JAR: " + jarFile.getAbsolutePath());

        // Create class provider
        JarFileClassProvider jarProvider = new JarFileClassProvider(jarFile);
        LoaderClassProvider loaderProvider = new LoaderClassProvider();
        DelegatingClassProvider classProvider = new DelegatingClassProvider(jarProvider, loaderProvider);

        this.manager = new ExecutionManager(classProvider);
        this.manager.setIgnoreClassNotFound(true); // Ignore missing external dependencies
        this.context = manager.newContext();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java test.StringDeobfuscator <jarfile> [output-file]");
            System.exit(1);
        }

        try {
            File jarFile = new File(args[0]);
            if (!jarFile.exists()) {
                System.err.println("JAR file not found: " + jarFile.getAbsolutePath());
                System.exit(1);
            }

            File outputFile = args.length > 1 ? new File(args[1]) : new File("output-dynamic.txt");

            StringDeobfuscator deobfuscator = new StringDeobfuscator(jarFile);
            deobfuscator.analyzeJar(jarFile);
            deobfuscator.saveToTextFile(outputFile);

            System.out.println("\n=== Analysis Complete ===");
            System.out.println("Success: " + deobfuscator.successCount);
            System.out.println("Failures: " + deobfuscator.failureCount);
            System.out.println("Output: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error during analysis:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void analyzeJar(File jarFile) throws Exception {
        System.out.println("\nAnalyzing classes...\n");

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");

                    try {
                        byte[] classBytes = jar.getInputStream(entry).readAllBytes();
                        analyzeClass(className, classBytes);
                        System.out.println("✓ " + className);
                    } catch (Throwable t) {
                        System.err.println("✗ " + className + ": " + t.getMessage());
                    }
                }
            }
        }
    }

    private void analyzeClass(String className, byte[] classBytes) throws Exception {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(classBytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES);

        Map<String, List<String>> methodMap = new LinkedHashMap<>();

        for (MethodNode methodNode : classNode.methods) {
            try {
                analyzeMethod(classNode, methodNode, methodMap);
            } catch (Throwable t) {
                // Continue analyzing other methods even if one fails
                if (ExecutionManager.DEBUG) {
                    System.err.println("  [ERROR] Method analysis failed: " + methodNode.name + methodNode.desc + " - " + t.getMessage());
                }
            }
        }

        if (!methodMap.isEmpty()) {
            resultMap.put(classNode.name, methodMap);
        }
    }

    private void analyzeMethod(ClassNode classNode, MethodNode methodNode, Map<String, List<String>> methodResults) {
        String methodName = methodNode.name + methodNode.desc;
        List<String> strings = new ArrayList<>();
        AbstractInsnNode[] instructions = methodNode.instructions.toArray();

        for (int i = 0; i < instructions.length; i++) {
            AbstractInsnNode insn = instructions[i];

            // Check for INVOKEDYNAMIC with signature (IJ)Ljava/lang/String;
            if (insn.getOpcode() == 186) { // INVOKEDYNAMIC
                InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                if ("(IJ)Ljava/lang/String;".equals(indy.desc)) {
                    List<Object> params = findParametersIJ(instructions, i);
                    if (params.size() == 2) {
                        try {
                            String result = executeInvokeDynamic(classNode, indy, params.get(0), params.get(1));
                            if (result != null) {
                                strings.add(result);
                                successCount++;
                                System.out.println("  [INDY] Found: \"" + result + "\" (params: " + params + ")");
                            }
                        } catch (Throwable t) {
                            failureCount++;
                            System.err.println("  [INDY] Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                        }
                    }
                }
            }

            // Check for INVOKESTATIC with signature (II)Ljava/lang/String;
            if (insn.getOpcode() == 184) { // INVOKESTATIC
                MethodInsnNode min = (MethodInsnNode) insn;
                if ("(II)Ljava/lang/String;".equals(min.desc)) {
                    List<Object> params = findParametersII(instructions, i);
                    if (params.size() == 2) {
                        try {
                            String result = executeInvokeStatic(min, params.get(0), params.get(1));
                            if (result != null) {
                                strings.add(result);
                                successCount++;
                                System.out.println("  [STATIC] Found: \"" + result + "\" (params: " + params + ")");
                            }
                        } catch (Throwable t) {
                            failureCount++;
                            System.err.println("  [STATIC] Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                        }
                    }
                }
            }
        }

        if (!strings.isEmpty()) {
            methodResults.put(methodName, strings);
        }
    }

    // Find parameters for (IJ)Ljava/lang/String; - int and long
    private List<Object> findParametersIJ(AbstractInsnNode[] instructions, int currentIndex) {
        List<Object> params = new ArrayList<>();
        params.add(0);  // int parameter
        params.add(0L); // long parameter
        int found = 0;

        for (int i = currentIndex - 1; i >= 0 && i >= currentIndex - 4 && found < 2; i--) {
            AbstractInsnNode insn = instructions[i];

            // LDC for long constant
            if (insn.getOpcode() == 18) { // LDC
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof Long) {
                    params.set(1, (Long) ldc.cst);
                    found++;
                }
            }

            // IntInsnNode for int parameter (BIPUSH, SIPUSH)
            if (insn instanceof IntInsnNode) {
                params.set(0, ((IntInsnNode) insn).operand);
                found++;
            }

            // ICONST for small int constants
            if (insn.getOpcode() >= 2 && insn.getOpcode() <= 8) { // ICONST_M1 to ICONST_5
                params.set(0, insn.getOpcode() - 3);
                found++;
            }
        }

        return params;
    }

    // Find parameters for (II)Ljava/lang/String; - two ints
    private List<Object> findParametersII(AbstractInsnNode[] instructions, int currentIndex) {
        List<Object> params = new ArrayList<>();
        params.add(0);
        params.add(0);
        int found = 0;

        for (int i = currentIndex - 1; i >= 0 && i >= currentIndex - 4 && found < 2; i--) {
            AbstractInsnNode insn = instructions[i];

            // IntInsnNode for int parameter (BIPUSH, SIPUSH)
            if (insn instanceof IntInsnNode) {
                params.set(1 - found, ((IntInsnNode) insn).operand);
                found++;
            }

            // ICONST for small int constants
            if (insn.getOpcode() >= 2 && insn.getOpcode() <= 8) { // ICONST_M1 to ICONST_5
                params.set(1 - found, insn.getOpcode() - 3);
                found++;
            }
        }

        Collections.reverse(params); // Reverse to get correct order
        return params;
    }

    private String executeInvokeDynamic(ClassNode classNode, InvokeDynamicInsnNode indy, Object intParam, Object longParam) throws Exception {
        // Load the target class into MiniJVM
        ExecutorClass targetClass = manager.loadClass(context, Type.getObjectType(classNode.name));

        // Use MiniJVM's BootstrapMethodResolver to resolve the invokedynamic
        net.lenni0451.minijvm.object.types.CallSiteObject callSite =
            net.lenni0451.minijvm.execution.BootstrapMethodResolver.resolve(context, indy, targetClass);

        // Get the target MethodHandle from the CallSite
        net.lenni0451.minijvm.object.types.MethodHandleObject methodHandle = callSite.getTarget();

        // Prepare actual method arguments
        StackElement[] args = new StackElement[] {
            new StackInt((Integer) intParam),
            new StackLong((Long) longParam)
        };

        // Invoke the MethodHandle with the actual arguments
        ExecutionResult result = methodHandle.invoke(context, args);

        if (result.hasException()) {
            throw new RuntimeException("InvokeDynamic execution failed");
        }

        if (result.hasReturnValue() && result.getReturnValue() instanceof StackObject) {
            StackObject retObj = (StackObject) result.getReturnValue();
            if (!retObj.isNull() && retObj.value() instanceof ExecutorObject) {
                return ExecutorTypeUtils.fromExecutorString(context, (ExecutorObject) retObj.value());
            }
        }

        return null;
    }

    private String executeInvokeStatic(MethodInsnNode min, Object param1, Object param2) throws Exception {
        // Load the target class
        ExecutorClass targetClass = manager.loadClass(context, Type.getObjectType(min.owner));

        // Find the method
        ExecutorClass.ResolvedMethod method = targetClass.findMethod(context, min.name, min.desc);
        if (method == null) {
            throw new RuntimeException("Method not found: " + min.owner + "." + min.name + min.desc);
        }

        // Execute with parameters
        StackElement[] args = new StackElement[] {
            new StackInt((Integer) param1),
            new StackInt((Integer) param2)
        };

        ExecutionResult result = Executor.execute(context, method.owner(), method.method(), null, args);

        if (result.hasException()) {
            throw new RuntimeException("InvokeStatic execution failed");
        }

        if (result.hasReturnValue() && result.getReturnValue() instanceof StackObject) {
            StackObject retObj = (StackObject) result.getReturnValue();
            if (!retObj.isNull() && retObj.value() instanceof ExecutorObject) {
                return ExecutorTypeUtils.fromExecutorString(context, (ExecutorObject) retObj.value());
            }
        }

        return null;
    }

    public void saveToTextFile(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Map<String, List<String>>> classEntry : resultMap.entrySet()) {
                writer.println("# " + classEntry.getKey());

                for (Map.Entry<String, List<String>> methodEntry : classEntry.getValue().entrySet()) {
                    writer.print("  " + methodEntry.getKey() + " = ");
                    writer.println(String.join(", ", methodEntry.getValue()));
                }

                writer.println();
            }
        }

        System.out.println("\nResults written to: " + file.getAbsolutePath());
    }
}
