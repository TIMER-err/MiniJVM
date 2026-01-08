package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for annotation support.
 */
public class AnnotationNatives implements Consumer<ExecutionManager> {

    // Store annotation values keyed by annotation object instance
    private static final Map<ExecutorObject, Map<String, Object>> ANNOTATION_VALUES = new WeakHashMap<>();

    @Override
    public void accept(ExecutionManager manager) {
        // Class.isAnnotationPresent(Class) - check if annotation is present
        manager.registerMethodExecutor("java/lang/Class.isAnnotationPresent(Ljava/lang/Class;)Z",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject classObject = (ClassObject) instance;
                ExecutorClass executorClass = classObject.getClassType();

                // Get the annotation class to check for
                if (arguments[0].isNull()) {
                    return returnValue(StackInt.ZERO);
                }
                ClassObject annotationClass = (ClassObject) ((StackObject) arguments[0]).value();
                String annotationType = "L" + annotationClass.getClassType().getClassNode().name + ";";

                // Check visibleAnnotations from ASM ClassNode
                List<AnnotationNode> annotations = executorClass.getClassNode().visibleAnnotations;
                if (annotations != null) {
                    for (AnnotationNode ann : annotations) {
                        if (ann.desc.equals(annotationType)) {
                            return returnValue(new StackInt(1)); // true
                        }
                    }
                }

                return returnValue(StackInt.ZERO); // false
            }
        );

        // Class.getAnnotation(Class) - get annotation instance
        manager.registerMethodExecutor("java/lang/Class.getAnnotation(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject classObject = (ClassObject) instance;
                ExecutorClass executorClass = classObject.getClassType();

                // Get the annotation class to retrieve
                if (arguments[0].isNull()) {
                    return returnValue(StackObject.NULL);
                }
                ClassObject annotationClass = (ClassObject) ((StackObject) arguments[0]).value();
                String annotationType = "L" + annotationClass.getClassType().getClassNode().name + ";";

                // Find the annotation in visibleAnnotations
                List<AnnotationNode> annotations = executorClass.getClassNode().visibleAnnotations;
                if (annotations != null) {
                    for (AnnotationNode ann : annotations) {
                        if (ann.desc.equals(annotationType)) {
                            // Create an annotation proxy object
                            ExecutorClass annClass = context.getExecutionManager().loadClass(context,
                                Type.getObjectType(annotationClass.getClassType().getClassNode().name));
                            ExecutorObject annObject = context.getExecutionManager().instantiate(context, annClass);

                            // Store annotation values in our static map
                            Map<String, Object> values = new HashMap<>();
                            if (ann.values != null) {
                                for (int i = 0; i < ann.values.size(); i += 2) {
                                    String key = (String) ann.values.get(i);
                                    Object value = ann.values.get(i + 1);
                                    values.put(key, value);
                                }
                            }

                            // Store the values for later retrieval
                            ANNOTATION_VALUES.put(annObject, values);

                            return returnValue(new StackObject(annObject));
                        }
                    }
                }

                return returnValue(StackObject.NULL);
            }
        );

        // Generic annotation method handler - intercept annotation interface method calls
        // This will handle calls like annotation.string(), annotation.doubleValue(), etc.
        // We need to register these dynamically or handle them in a special way
    }

    /**
     * Get the annotation value for a given method name on an annotation instance.
     * This is called by JVMMethodExecutor when an annotation interface method is invoked.
     */
    public static StackElement getAnnotationValue(ExecutorObject annotationInstance, String methodName) {
        Map<String, Object> values = ANNOTATION_VALUES.get(annotationInstance);
        if (values == null) {
            return null;
        }

        Object value = values.get(methodName);
        if (value == null) {
            return null;
        }

        // Convert the ASM annotation value to a StackElement
        if (value instanceof String) {
            // Return as ExecutorString
            return StackObject.NULL; // Will be handled by caller
        } else if (value instanceof Integer) {
            return new StackInt((Integer) value);
        } else if (value instanceof Long) {
            return new StackLong((Long) value);
        } else if (value instanceof Float) {
            return new StackFloat((Float) value);
        } else if (value instanceof Double) {
            return new StackDouble((Double) value);
        } else if (value instanceof Boolean) {
            return new StackInt((Boolean) value ? 1 : 0);
        }

        return null;
    }

    /**
     * Check if an object is an annotation instance.
     */
    public static boolean isAnnotationInstance(ExecutorObject object) {
        return ANNOTATION_VALUES.containsKey(object);
    }

    /**
     * Get raw annotation value (for String conversion).
     */
    public static Object getRawAnnotationValue(ExecutorObject annotationInstance, String methodName) {
        Map<String, Object> values = ANNOTATION_VALUES.get(annotationInstance);
        if (values == null) {
            return null;
        }
        return values.get(methodName);
    }
}
