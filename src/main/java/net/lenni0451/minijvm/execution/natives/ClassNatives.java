package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ClassUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ClassNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Class.registerNatives()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/lang/Class.desiredAssertionStatus0(Ljava/lang/Class;)Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
        manager.registerMethodExecutor("java/lang/Class.getPrimitiveClass(Ljava/lang/String;)Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            String className = ExecutorTypeUtils.fromExecutorString(executionContext, ((StackObject) arguments[0]).value());
            if (!ClassUtils.PRIMITIVE_CLASS_TO_DESCRIPTOR.containsKey(className)) {
                return ExceptionUtils.newException(executionContext, Types.CLASS_NOT_FOUND_EXCEPTION, className);
            }
            ExecutorClass primitiveClass = executionContext.getExecutionManager().loadClass(executionContext, Type.getType(ClassUtils.PRIMITIVE_CLASS_TO_DESCRIPTOR.get(className)));
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateClass(executionContext, primitiveClass)));
        });
        manager.registerMethodExecutor("java/lang/Class.isArray()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            boolean isArray = ((ClassObject) instance).getClassType().getClassNode().name.startsWith("[");
            return returnValue(new StackInt(isArray));
        });
        manager.registerMethodExecutor("java/lang/Class.isPrimitive()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject clazz = (ClassObject) instance;
            return ExecutionResult.returnValue(new StackInt(Types.isPrimitive(clazz.getClassType().getType())));
        });
        manager.registerMethodExecutor("java/lang/Class.getRawAnnotations()[B", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no annotations available in MiniJVM
            return returnValue(StackObject.NULL);
        });
        manager.registerMethodExecutor("java/lang/Class.getRawTypeAnnotations()[B", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no type annotations available in MiniJVM
            return returnValue(StackObject.NULL);
        });
        manager.registerMethodExecutor("java/lang/Class.getConstantPool()Ljdk/internal/reflect/ConstantPool;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return a stub ConstantPool instance
            ExecutorClass constantPoolClass = executionContext.getExecutionManager().loadClass(executionContext,
                Type.getObjectType("jdk/internal/reflect/ConstantPool"));
            net.lenni0451.minijvm.object.ExecutorObject constantPool =
                executionContext.getExecutionManager().instantiate(executionContext, constantPoolClass);
            return returnValue(new StackObject(constantPool));
        });
        manager.registerMethodExecutor("java/lang/Class.getSuperclass()Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject classObject = (ClassObject) instance;
            ExecutorClass executorClass = classObject.getClassType();

            // Get superclass name from ClassNode
            String superName = executorClass.getClassNode().superName;
            if (superName == null || superName.equals("java/lang/Object")) {
                // Object has no superclass
                return returnValue(StackObject.NULL);
            }

            // Load and return the superclass
            ExecutorClass superClass = executionContext.getExecutionManager().loadClass(executionContext,
                Type.getObjectType(superName));
            net.lenni0451.minijvm.object.ExecutorObject superClassObject =
                executionContext.getExecutionManager().instantiateClass(executionContext, superClass);
            return returnValue(new StackObject(superClassObject));
        });
        manager.registerMethodExecutor("java/lang/Class.getModifiers()I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject classObject = (ClassObject) instance;
            ExecutorClass executorClass = classObject.getClassType();
            // Return the access flags from the ClassNode
            return returnValue(new StackInt(executorClass.getClassNode().access));
        });
        manager.registerMethodExecutor("java/lang/Class.getInterfaces0()[Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject classObject = (ClassObject) instance;
            ExecutorClass executorClass = classObject.getClassType();

            // Get interfaces from ClassNode
            java.util.List<String> interfaces = executorClass.getClassNode().interfaces;
            if (interfaces == null || interfaces.isEmpty()) {
                // Return empty array
                ExecutorClass classArrayClass = executionContext.getExecutionManager().loadClass(executionContext,
                    Type.getType("[Ljava/lang/Class;"));
                net.lenni0451.minijvm.object.ExecutorObject emptyArray =
                    executionContext.getExecutionManager().instantiateArray(executionContext, classArrayClass, 0);
                return returnValue(new StackObject(emptyArray));
            }

            // Create array of Class objects for interfaces
            ExecutorClass classArrayClass = executionContext.getExecutionManager().loadClass(executionContext,
                Type.getType("[Ljava/lang/Class;"));
            net.lenni0451.minijvm.stack.StackElement[] interfaceClasses =
                new net.lenni0451.minijvm.stack.StackElement[interfaces.size()];

            for (int i = 0; i < interfaces.size(); i++) {
                ExecutorClass interfaceClass = executionContext.getExecutionManager().loadClass(executionContext,
                    Type.getObjectType(interfaces.get(i)));
                net.lenni0451.minijvm.object.ExecutorObject interfaceClassObj =
                    executionContext.getExecutionManager().instantiateClass(executionContext, interfaceClass);
                interfaceClasses[i] = new StackObject(interfaceClassObj);
            }

            net.lenni0451.minijvm.object.ExecutorObject interfaceArray =
                executionContext.getExecutionManager().instantiateArray(executionContext, classArrayClass, interfaceClasses);
            return returnValue(new StackObject(interfaceArray));
        });
        manager.registerMethodExecutor("java/lang/Class.getMethod0(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return null to indicate method not found
            // This will cause Class.getMethod to throw NoSuchMethodException
            return returnValue(StackObject.NULL);
        });
        manager.registerMethodExecutor("java/lang/Class.getEnumConstantsShared()[Ljava/lang/Object;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject classObject = (ClassObject) instance;
            ExecutorClass executorClass = classObject.getClassType();

            // Check if this is an enum class by checking if it extends java.lang.Enum
            String superName = executorClass.getClassNode().superName;
            if (superName == null || !superName.equals("java/lang/Enum")) {
                // Not an enum, return null
                return returnValue(StackObject.NULL);
            }

            // Try to find and invoke the static values() method
            ExecutorClass.ResolvedMethod valuesMethod = executorClass.findMethod(executionContext, "values",
                "()[L" + executorClass.getClassNode().name + ";");

            if (valuesMethod == null) {
                // values() method not found, return null
                return returnValue(StackObject.NULL);
            }

            // Invoke the static values() method
            net.lenni0451.minijvm.execution.ExecutionResult result =
                net.lenni0451.minijvm.execution.Executor.execute(executionContext,
                    valuesMethod.owner(), valuesMethod.method(), null);

            if (result.hasException()) {
                return result;
            }

            // Return the array of enum constants
            return returnValue(result.getReturnValue());
        });
    }

}
