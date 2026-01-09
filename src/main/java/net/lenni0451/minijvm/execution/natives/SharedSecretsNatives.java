package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for jdk.internal.access classes.
 */
public class SharedSecretsNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // SharedSecrets.getJavaLangAccess() - returns JavaLangAccess instance
        manager.registerMethodExecutor("jdk/internal/access/SharedSecrets.getJavaLangAccess()Ljdk/internal/access/JavaLangAccess;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub JavaLangAccess instance
            ExecutorClass javaLangAccessClass = executionContext.getExecutionManager().loadClass(executionContext,
                Type.getObjectType("jdk/internal/access/JavaLangAccess"));
            net.lenni0451.minijvm.object.ExecutorObject javaLangAccess =
                executionContext.getExecutionManager().instantiate(executionContext, javaLangAccessClass);
            return returnValue(new StackObject(javaLangAccess));
        });

        // JavaLangAccess.getEnumConstantsShared(Class) - returns enum constants
        manager.registerMethodExecutor("jdk/internal/access/JavaLangAccess.getEnumConstantsShared(Ljava/lang/Class;)[Ljava/lang/Enum;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Get the Class object from arguments
            StackObject classArg = (StackObject) arguments[0];
            if (classArg.isNull()) {
                return returnValue(StackObject.NULL);
            }

            ClassObject classObject = (ClassObject) classArg.value();
            ExecutorClass executorClass = classObject.getClassType();

            // Check if this is an enum class
            String superName = executorClass.getClassNode().superName;
            if (superName == null || !superName.equals("java/lang/Enum")) {
                return returnValue(StackObject.NULL);
            }

            // Find and invoke the static values() method
            ExecutorClass.ResolvedMethod valuesMethod = executorClass.findMethod(executionContext, "values",
                "()[L" + executorClass.getClassNode().name + ";");

            if (valuesMethod == null) {
                return returnValue(StackObject.NULL);
            }

            // Invoke the static values() method
            ExecutionResult result = net.lenni0451.minijvm.execution.Executor.execute(executionContext,
                valuesMethod.owner(), valuesMethod.method(), null);

            if (result.hasException()) {
                return result;
            }

            // Return the array of enum constants
            return returnValue(result.getReturnValue());
        });

        // StreamOpFlag static initializer stub - avoid complex enum initialization
        manager.registerMethodExecutor("java/util/stream/StreamOpFlag.<clinit>()V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Skip the static initializer to avoid JavaLangAccess dependency
            return ExecutionResult.voidResult();
        });

        // StreamOpFlag.isKnown(int) - check if flag is known
        manager.registerMethodExecutor("java/util/stream/StreamOpFlag.isKnown(I)Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return false to indicate flag is not known (safe default)
            return returnValue(net.lenni0451.minijvm.stack.StackInt.ZERO);
        });

        // Bypass stream-related static initializers that may cause issues
        String[] streamClasses = {
            "java/util/stream/StreamSupport",
            "java/util/stream/AbstractPipeline",
            "java/util/stream/ReferencePipeline",
            "java/util/stream/IntPipeline",
            "java/util/stream/LongPipeline",
            "java/util/stream/DoublePipeline"
        };

        for (String streamClass : streamClasses) {
            manager.registerMethodExecutor(streamClass + ".<clinit>()V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
                return ExecutionResult.voidResult();
            });
        }
    }
}
