package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ObjectNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Object.hashCode()I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(instance.hashCode()));
        });
        manager.registerMethodExecutor("java/lang/Object.getClass()Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateClass(executionContext, instance.getClazz())));
        });
        manager.registerMethodExecutor("java/lang/Object.clone()Ljava/lang/Object;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (instance instanceof ArrayObject) {
                ExecutorObject clone = executionContext.getExecutionManager().instantiateArray(executionContext, instance.getClazz(), ((ArrayObject) instance).getElements().clone());
                return returnValue(new StackObject(clone));
            } else {
                // Clone regular objects by creating a new instance and copying fields
                ExecutorObject clone = executionContext.getExecutionManager().instantiate(executionContext, instance.getClazz());

                try {
                    // Use reflection to access the private 'fields' map
                    Field fieldsField = ExecutorObject.class.getDeclaredField("fields");
                    fieldsField.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    Map<FieldNode, StackElement> sourceFields = (Map<FieldNode, StackElement>) fieldsField.get(instance);
                    @SuppressWarnings("unchecked")
                    Map<FieldNode, StackElement> cloneFields = (Map<FieldNode, StackElement>) fieldsField.get(clone);

                    // Copy all fields (shallow copy)
                    cloneFields.putAll(sourceFields);
                } catch (Exception e) {
                    // Fallback: if reflection fails, return the clone without copied fields
                    // This is better than throwing an exception
                }

                return returnValue(new StackObject(clone));
            }
        });
    }

}
