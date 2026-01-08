package test;

import net.lenni0451.commons.asm.provider.DelegatingClassProvider;
import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.commons.asm.provider.io.JarFileClassProvider;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import org.objectweb.asm.Type;

import java.io.File;

/**
 * Test runner for obfuscated JAR file.
 */
public class ObfTestRunner {

    public static void main(String[] args) {
        System.out.println("=== Obfuscated JAR Test ===\n");

        try {
            // Create class provider from JAR
            File jarFile = new File(System.getProperty("user.home") + "/sandbox/obf-test.jar");
            if (!jarFile.exists()) {
                System.err.println("JAR file not found: " + jarFile.getAbsolutePath());
                System.exit(1);
            }

            System.out.println("Loading JAR: " + jarFile.getAbsolutePath());

            // Create a delegating class provider that checks JAR first, then system classloader
            JarFileClassProvider jarProvider = new JarFileClassProvider(jarFile);
            LoaderClassProvider loaderProvider = new LoaderClassProvider();
            DelegatingClassProvider classProvider = new DelegatingClassProvider(jarProvider, loaderProvider);

            // Create execution manager with combined class provider
            ExecutionManager manager = new ExecutionManager(classProvider);
            ExecutionContext context = manager.newContext();

            // Load and execute main class
            String mainClassName = "dev/sim0n/app/Main";
            System.out.println("Loading main class: " + mainClassName);

            ExecutorClass mainClass = manager.loadClass(context, Type.getObjectType(mainClassName));
            ExecutorClass.ResolvedMethod mainMethod = mainClass.findMethod(context, "main", "([Ljava/lang/String;)V");

            if (mainMethod == null) {
                System.err.println("Main method not found in class: " + mainClassName);
                System.exit(1);
            }

            System.out.println("Executing main method...\n");
            System.out.println("--- Output ---");

            // Execute main method with empty args
            ExecutionResult result = Executor.execute(context, mainClass, mainMethod.method(), null);

            System.out.println("\n--- Execution Result ---");
            if (result.hasException()) {
                System.err.println("Exception occurred:");
                net.lenni0451.minijvm.object.ExecutorObject exception = result.getException();
                System.err.println("  Exception class: " + exception.getClazz().getClassNode().name);

                // Try to get exception message
                try {
                    net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod getMessageMethod =
                        exception.getClazz().findMethod(context, "getMessage", "()Ljava/lang/String;");
                    if (getMessageMethod != null) {
                        ExecutionResult msgResult = Executor.execute(context, getMessageMethod.owner(),
                            getMessageMethod.method(), exception);
                        if (msgResult.hasReturnValue() && !msgResult.getReturnValue().isNull()) {
                            net.lenni0451.minijvm.stack.StackObject msgObj = (net.lenni0451.minijvm.stack.StackObject) msgResult.getReturnValue();
                            String message = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, msgObj.value());
                            System.err.println("  Message: " + message);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("  (Could not retrieve message: " + e.getMessage() + ")");
                }

                // Try to get cause
                try {
                    net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod getCauseMethod =
                        exception.getClazz().findMethod(context, "getCause", "()Ljava/lang/Throwable;");
                    if (getCauseMethod != null) {
                        ExecutionResult causeResult = Executor.execute(context, getCauseMethod.owner(),
                            getCauseMethod.method(), exception);
                        if (causeResult.hasReturnValue() && !causeResult.getReturnValue().isNull()) {
                            net.lenni0451.minijvm.stack.StackObject causeObj = (net.lenni0451.minijvm.stack.StackObject) causeResult.getReturnValue();
                            System.err.println("  Cause: " + causeObj.value().getClazz().getClassNode().name);

                            // Get cause message
                            net.lenni0451.minijvm.object.ExecutorClass.ResolvedMethod causeGetMessageMethod =
                                causeObj.value().getClazz().findMethod(context, "getMessage", "()Ljava/lang/String;");
                            if (causeGetMessageMethod != null) {
                                ExecutionResult causeMsgResult = Executor.execute(context, causeGetMessageMethod.owner(),
                                    causeGetMessageMethod.method(), causeObj.value());
                                if (causeMsgResult.hasReturnValue() && !causeMsgResult.getReturnValue().isNull()) {
                                    net.lenni0451.minijvm.stack.StackObject causeMsgObj = (net.lenni0451.minijvm.stack.StackObject) causeMsgResult.getReturnValue();
                                    String causeMessage = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(context, causeMsgObj.value());
                                    System.err.println("  Cause message: " + causeMessage);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }

                System.err.println("  Exception object: " + exception);

                // Print stack frames
                System.err.println("\n  Stack trace:");
                net.lenni0451.minijvm.ExecutionContext.StackFrame[] frames = context.getStackFrames();
                for (int i = frames.length - 1; i >= Math.max(0, frames.length - 10); i--) {
                    net.lenni0451.minijvm.ExecutionContext.StackFrame frame = frames[i];
                    System.err.println("    at " + frame.getExecutorClass().getClassNode().name + "." +
                        frame.getMethodNode().name + frame.getMethodNode().desc);
                }
            } else {
                System.out.println("Execution completed successfully!");
            }

        } catch (Exception e) {
            System.err.println("Error running obfuscated JAR:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
