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

    private static net.lenni0451.minijvm.object.ExecutorObject javaLangAccessInstance = null;

    @Override
    public void accept(ExecutionManager manager) {
        // SharedSecrets.getJavaLangAccess() - returns JavaLangAccess singleton
        manager.registerMethodExecutor("jdk/internal/access/SharedSecrets.getJavaLangAccess()Ljdk/internal/access/JavaLangAccess;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (javaLangAccessInstance == null) {
                // Create a singleton JavaLangAccess implementation
                ExecutorClass javaLangAccessClass = executionContext.getExecutionManager().loadClass(executionContext,
                    Type.getObjectType("jdk/internal/access/JavaLangAccess"));
                javaLangAccessInstance = executionContext.getExecutionManager().instantiate(executionContext, javaLangAccessClass);
            }
            return returnValue(new StackObject(javaLangAccessInstance));
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
    }
}
