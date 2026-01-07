package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.object.types.CallSiteObject;
import net.lenni0451.minijvm.object.types.MethodHandleObject;
import net.lenni0451.minijvm.object.types.MethodTypeObject;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for java.lang.invoke.StringConcatFactory.
 * This enables Java 9+ string concatenation optimization.
 */
public class StringConcatFactoryNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // StringConcatFactory.makeConcatWithConstants - the main bootstrap method
        manager.registerMethodExecutor(
            "java/lang/invoke/StringConcatFactory.makeConcatWithConstants(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Arguments:
                // 0: MethodHandles.Lookup caller
                // 1: String name (ignored)
                // 2: MethodType concatType (parameter types to concatenate)
                // 3: String recipe (template with \u0001 placeholders)
                // 4: Object[] constants (constant strings in recipe)

                MethodTypeObject concatType = (MethodTypeObject) ((StackObject) arguments[2]).value();
                String recipe = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[3]).value());

                StackElement[] constants;
                if (arguments[4] instanceof StackObject so && !so.isNull()) {
                    ArrayObject arr = (ArrayObject) so.value();
                    constants = arr.getElements();
                } else {
                    constants = new StackElement[0];
                }

                // Create a MethodHandle that performs the concatenation
                MethodHandleObject concatHandle = new StringConcatMethodHandle(
                    context, concatType, recipe, constants
                );

                CallSiteObject callSite = CallSiteObject.constant(context, concatType, concatHandle);
                return returnValue(new StackObject(callSite));
            }
        );

        // StringConcatFactory.makeConcat - simpler variant without constants
        manager.registerMethodExecutor(
            "java/lang/invoke/StringConcatFactory.makeConcat(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                MethodTypeObject concatType = (MethodTypeObject) ((StackObject) arguments[2]).value();

                // Create a simple recipe with all placeholders
                StringBuilder recipe = new StringBuilder();
                for (int i = 0; i < concatType.parameterCount(); i++) {
                    recipe.append('\u0001');
                }

                MethodHandleObject concatHandle = new StringConcatMethodHandle(
                    context, concatType, recipe.toString(), new StackElement[0]
                );

                CallSiteObject callSite = CallSiteObject.constant(context, concatType, concatHandle);
                return returnValue(new StackObject(callSite));
            }
        );
    }

    /**
     * A MethodHandle that performs string concatenation according to a recipe.
     */
    public static class StringConcatMethodHandle extends MethodHandleObject {

        private final MethodTypeObject concatType;
        private final String recipe;
        private final StackElement[] constants;

        public StringConcatMethodHandle(ExecutionContext context,
                                        MethodTypeObject concatType,
                                        String recipe,
                                        StackElement[] constants) {
            super(context, REF_invokeStatic, "java/lang/invoke/StringConcatFactory",
                  "concat", concatType.getDescriptor(), false);
            this.concatType = concatType;
            this.recipe = recipe;
            this.constants = constants;
        }

        @Override
        public ExecutionResult invoke(ExecutionContext context, StackElement... args) {
            StringBuilder result = new StringBuilder();

            int argIndex = 0;
            int constantIndex = 0;

            for (int i = 0; i < recipe.length(); i++) {
                char c = recipe.charAt(i);
                if (c == '\u0001') {
                    // Placeholder for argument
                    if (argIndex < args.length) {
                        result.append(stackElementToString(context, args[argIndex++]));
                    }
                } else if (c == '\u0002') {
                    // Placeholder for constant
                    if (constantIndex < constants.length) {
                        result.append(stackElementToString(context, constants[constantIndex++]));
                    }
                } else {
                    // Literal character
                    result.append(c);
                }
            }

            // Create executor String object
            StackElement stringResult = ExecutorTypeUtils.parse(context, result.toString());
            return returnValue(stringResult);
        }

        private String stackElementToString(ExecutionContext context, StackElement element) {
            if (element instanceof StackInt si) {
                // Could be int, char, boolean, byte, short
                Type paramType = getParamTypeForIndex(context);
                if (paramType != null && paramType.equals(Type.CHAR_TYPE)) {
                    return String.valueOf((char) si.value());
                } else if (paramType != null && paramType.equals(Type.BOOLEAN_TYPE)) {
                    return si.value() != 0 ? "true" : "false";
                }
                return String.valueOf(si.value());
            } else if (element instanceof StackLong sl) {
                return String.valueOf(sl.value());
            } else if (element instanceof StackFloat sf) {
                return String.valueOf(sf.value());
            } else if (element instanceof StackDouble sd) {
                return String.valueOf(sd.value());
            } else if (element instanceof StackObject so) {
                if (so.isNull()) {
                    return "null";
                }
                // Try to convert to string
                if (so.value().getClazz().getClassNode().name.equals("java/lang/String")) {
                    return ExecutorTypeUtils.fromExecutorString(context, so.value());
                }
                // For other objects, use toString representation
                return so.value().toString();
            }
            return String.valueOf(element);
        }

        private Type getParamTypeForIndex(ExecutionContext context) {
            // This is a simplified implementation
            // In a full implementation, we'd track which parameter type corresponds to each placeholder
            return null;
        }
    }

}
