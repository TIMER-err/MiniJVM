package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;
import static net.lenni0451.minijvm.execution.ExecutionResult.voidResult;

public class SystemNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/System.registerNatives()V", MethodExecutor.NOOP_VOID);

        // System static initializer - initialize out, err, in
        manager.registerMethodExecutor("java/lang/System.<clinit>()V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Create PrintStream instances for out, err
            ExecutorClass printStreamClass = executionContext.getExecutionManager().loadClass(executionContext,
                org.objectweb.asm.Type.getObjectType("java/io/PrintStream"));
            net.lenni0451.minijvm.object.ExecutorObject outStream =
                executionContext.getExecutionManager().instantiate(executionContext, printStreamClass);
            net.lenni0451.minijvm.object.ExecutorObject errStream =
                executionContext.getExecutionManager().instantiate(executionContext, printStreamClass);

            // Set System.out
            ExecutorClass.ResolvedField outField = currentClass.findField(executionContext, "out", "Ljava/io/PrintStream;");
            if (outField != null) {
                outField.set(new StackObject(outStream));
            }

            // Set System.err
            ExecutorClass.ResolvedField errField = currentClass.findField(executionContext, "err", "Ljava/io/PrintStream;");
            if (errField != null) {
                errField.set(new StackObject(errStream));
            }

            // Set System.in to null for now
            ExecutorClass.ResolvedField inField = currentClass.findField(executionContext, "in", "Ljava/io/InputStream;");
            if (inField != null) {
                inField.set(StackObject.NULL);
            }

            return ExecutionResult.voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            StackObject src = (StackObject) arguments[0];
            StackInt srcPos = (StackInt) arguments[1];
            StackObject dest = (StackObject) arguments[2];
            StackInt destPos = (StackInt) arguments[3];
            StackInt length = (StackInt) arguments[4];
            if (src == null) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "src");
            }
            if (dest == null) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "dest");
            }
            ArrayObject srcArray = (ArrayObject) src.value();
            ArrayObject destArray = (ArrayObject) dest.value();
            if (srcPos.value() < 0 || srcPos.value() + length.value() > srcArray.getElements().length) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "srcPos");
            }
            if (destPos.value() < 0 || destPos.value() + length.value() > destArray.getElements().length) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "destPos");
            }
            if (length.value() < 0) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "length");
            }
            //TODO: Component type check
            System.arraycopy(srcArray.getElements(), srcPos.value(), destArray.getElements(), destPos.value(), length.value());
            return voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.nanoTime()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackLong(System.nanoTime()));
        });
        manager.registerMethodExecutor("java/lang/System.currentTimeMillis()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackLong(System.currentTimeMillis()));
        });
        manager.registerMethodExecutor("java/lang/System.setIn0(Ljava/io/InputStream;)V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutorClass.ResolvedField field = currentClass.findField(executionContext, "in", "Ljava/io/InputStream;");
            if (field == null) throw new ExecutorException(executionContext, "Could not find 'in' field in System class");
            field.set(arguments[0]);
            return ExecutionResult.voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.setOut0(Ljava/io/PrintStream;)V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutorClass.ResolvedField field = currentClass.findField(executionContext, "out", "Ljava/io/PrintStream;");
            if (field == null) throw new ExecutorException(executionContext, "Could not find 'out' field in System class");
            field.set(arguments[0]);
            return ExecutionResult.voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.setErr0(Ljava/io/PrintStream;)V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutorClass.ResolvedField field = currentClass.findField(executionContext, "err", "Ljava/io/PrintStream;");
            if (field == null) throw new ExecutorException(executionContext, "Could not find 'err' field in System class");
            field.set(arguments[0]);
            return ExecutionResult.voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.getProperties()Ljava/util/Properties;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            // Return a Properties instance
            ExecutorClass propertiesClass = executionContext.getExecutionManager().loadClass(executionContext,
                org.objectweb.asm.Type.getObjectType("java/util/Properties"));
            net.lenni0451.minijvm.object.ExecutorObject properties =
                executionContext.getExecutionManager().instantiate(executionContext, propertiesClass);

            // Call Properties constructor
            ExecutorClass.ResolvedMethod constructor = propertiesClass.findMethod(executionContext, "<init>", "()V");
            if (constructor != null) {
                net.lenni0451.minijvm.execution.Executor.execute(executionContext, constructor.owner(), constructor.method(), properties);
            }

            return returnValue(new StackObject(properties));
        });

        // System.identityHashCode(Object) - returns identity hash code
        manager.registerMethodExecutor("java/lang/System.identityHashCode(Ljava/lang/Object;)I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            StackObject obj = (StackObject) arguments[0];
            if (obj == null || obj.isNull()) {
                return returnValue(new StackInt(0));
            }
            // Use Java's identity hash code for the ExecutorObject
            int hashCode = System.identityHashCode(obj.value());
            return returnValue(new StackInt(hashCode));
        });
    }

}
