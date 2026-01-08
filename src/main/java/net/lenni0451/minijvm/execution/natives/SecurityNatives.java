package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

/**
 * Native method implementations for sun.security classes.
 */
public class SecurityNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // sun.security.util.Debug.<clinit> - bypass the static initializer
        manager.registerMethodExecutor(
            "sun/security/util/Debug.<clinit>()V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Skip the complex static initialization - just return success
                return ExecutionResult.voidResult();
            }
        );

        // sun.security.util.Debug.getInstance(String)
        // Returns null to disable all debug output
        manager.registerMethodExecutor(
            "sun/security/util/Debug.getInstance(Ljava/lang/String;)Lsun/security/util/Debug;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Return null to disable debug output
                return ExecutionResult.returnValue(StackObject.NULL);
            }
        );

        // sun.security.util.Debug.getInstance(String, String)
        manager.registerMethodExecutor(
            "sun/security/util/Debug.getInstance(Ljava/lang/String;Ljava/lang/String;)Lsun/security/util/Debug;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.returnValue(StackObject.NULL);
            }
        );

        // sun.security.util.Debug.println(String)
        manager.registerMethodExecutor(
            "sun/security/util/Debug.println(Ljava/lang/String;)V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Do nothing when debug is disabled
                return ExecutionResult.voidResult();
            }
        );

        // sun.security.util.Debug.println()
        manager.registerMethodExecutor(
            "sun/security/util/Debug.println()V",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.voidResult();
            }
        );

        // sun.security.util.Debug.isOn(String)
        manager.registerMethodExecutor(
            "sun/security/util/Debug.isOn(Ljava/lang/String;)Z",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Always return false - debug is off
                return ExecutionResult.returnValue(new net.lenni0451.minijvm.stack.StackInt(0));
            }
        );

        // sun.security.action.GetPropertyAction.privilegedRun()
        manager.registerMethodExecutor(
            "sun/security/action/GetPropertyAction.privilegedRun()Ljava/lang/String;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Return null for all property requests in MiniJVM
                return ExecutionResult.returnValue(StackObject.NULL);
            }
        );

        // sun.security.action.GetPropertyAction.run()
        manager.registerMethodExecutor(
            "sun/security/action/GetPropertyAction.run()Ljava/lang/Object;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.returnValue(StackObject.NULL);
            }
        );

        // Bypass SecurityConstants static initializer
        manager.registerMethodExecutor("sun/security/util/SecurityConstants.<clinit>()V", MethodExecutor.NOOP_VOID);
    }
}
