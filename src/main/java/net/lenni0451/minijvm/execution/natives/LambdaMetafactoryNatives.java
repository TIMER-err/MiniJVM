package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.BootstrapMethodResolver;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.CallSiteObject;
import net.lenni0451.minijvm.object.types.MethodHandleObject;
import net.lenni0451.minijvm.object.types.MethodTypeObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for java.lang.invoke.LambdaMetafactory.
 * This enables lambda expression support in the VM.
 */
public class LambdaMetafactoryNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // LambdaMetafactory.metafactory - the main bootstrap method for lambdas
        manager.registerMethodExecutor(
            "java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Arguments:
                // 0: MethodHandles.Lookup caller
                // 1: String invokedName (the SAM method name, e.g., "run", "apply")
                // 2: MethodType invokedType (the functional interface type to return)
                // 3: MethodType samMethodType (SAM method signature)
                // 4: MethodHandle implMethod (the actual implementation)
                // 5: MethodType instantiatedMethodType (instantiated SAM type)

                BootstrapMethodResolver.LookupObject lookup =
                    (BootstrapMethodResolver.LookupObject) ((StackObject) arguments[0]).value();
                String invokedName = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(
                    context, ((StackObject) arguments[1]).value());
                MethodTypeObject invokedType = (MethodTypeObject) ((StackObject) arguments[2]).value();
                MethodTypeObject samMethodType = (MethodTypeObject) ((StackObject) arguments[3]).value();
                MethodHandleObject implMethod = (MethodHandleObject) ((StackObject) arguments[4]).value();
                MethodTypeObject instantiatedMethodType = (MethodTypeObject) ((StackObject) arguments[5]).value();

                // Create a lambda proxy that wraps the implementation method
                // The invokedType describes what gets captured and what type is returned
                // e.g., for `Runnable r = () -> foo()`, invokedType is `()Runnable`
                // e.g., for `Runnable r = x -> foo(x)` where x is captured, invokedType is `(int)Runnable`

                // Create a MethodHandle that, when invoked with captured args, returns a lambda proxy
                MethodHandleObject lambdaFactory = new LambdaFactoryMethodHandle(
                    context, invokedType, invokedName, samMethodType, implMethod, instantiatedMethodType
                );

                // Return a ConstantCallSite with the factory as target
                CallSiteObject callSite = CallSiteObject.constant(context, invokedType, lambdaFactory);
                return returnValue(new StackObject(callSite));
            }
        );

        // LambdaMetafactory.altMetafactory - alternative metafactory with extra flags
        manager.registerMethodExecutor(
            "java/lang/invoke/LambdaMetafactory.altMetafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Simplified implementation - delegate to regular metafactory behavior
                // The args array contains: samMethodType, implMethod, instantiatedMethodType, flags, ...

                BootstrapMethodResolver.LookupObject lookup =
                    (BootstrapMethodResolver.LookupObject) ((StackObject) arguments[0]).value();
                String invokedName = net.lenni0451.minijvm.utils.ExecutorTypeUtils.fromExecutorString(
                    context, ((StackObject) arguments[1]).value());
                MethodTypeObject invokedType = (MethodTypeObject) ((StackObject) arguments[2]).value();

                net.lenni0451.minijvm.object.types.ArrayObject argsArray =
                    (net.lenni0451.minijvm.object.types.ArrayObject) ((StackObject) arguments[3]).value();
                StackElement[] args = argsArray.getElements();

                // Extract key arguments from the array
                MethodTypeObject samMethodType = (MethodTypeObject) ((StackObject) args[0]).value();
                MethodHandleObject implMethod = (MethodHandleObject) ((StackObject) args[1]).value();
                MethodTypeObject instantiatedMethodType = (MethodTypeObject) ((StackObject) args[2]).value();
                // args[3] would be flags, but we ignore them for simplicity

                MethodHandleObject lambdaFactory = new LambdaFactoryMethodHandle(
                    context, invokedType, invokedName, samMethodType, implMethod, instantiatedMethodType
                );

                CallSiteObject callSite = CallSiteObject.constant(context, invokedType, lambdaFactory);
                return returnValue(new StackObject(callSite));
            }
        );
    }

    /**
     * A special MethodHandle that creates lambda proxy instances when invoked.
     * When invoked with captured arguments, it returns a LambdaProxyObject.
     */
    public static class LambdaFactoryMethodHandle extends MethodHandleObject {

        private final MethodTypeObject invokedType;
        private final String samMethodName;
        private final MethodTypeObject samMethodType;
        private final MethodHandleObject implMethod;
        private final MethodTypeObject instantiatedMethodType;

        public LambdaFactoryMethodHandle(ExecutionContext context,
                                         MethodTypeObject invokedType,
                                         String samMethodName,
                                         MethodTypeObject samMethodType,
                                         MethodHandleObject implMethod,
                                         MethodTypeObject instantiatedMethodType) {
            super(context, REF_invokeStatic, "java/lang/invoke/LambdaMetafactory",
                  "lambda$factory", invokedType.getDescriptor(), false);
            this.invokedType = invokedType;
            this.samMethodName = samMethodName;
            this.samMethodType = samMethodType;
            this.implMethod = implMethod;
            this.instantiatedMethodType = instantiatedMethodType;
        }

        @Override
        public ExecutionResult invoke(ExecutionContext context, StackElement... capturedArgs) {
            // Create and return a lambda proxy object
            LambdaProxyObject proxy = new LambdaProxyObject(
                context, invokedType, samMethodName, samMethodType, implMethod, instantiatedMethodType, capturedArgs
            );
            return returnValue(new StackObject(proxy));
        }

        public String getSamMethodName() {
            return samMethodName;
        }

        public MethodTypeObject getSamMethodType() {
            return samMethodType;
        }

        public MethodHandleObject getImplMethod() {
            return implMethod;
        }
    }

    /**
     * A proxy object representing a lambda instance.
     * It implements the functional interface by delegating to the implementation method.
     */
    public static class LambdaProxyObject extends ExecutorObject {

        private final String samMethodName;
        private final MethodTypeObject samMethodType;
        private final MethodHandleObject implMethod;
        private final MethodTypeObject instantiatedMethodType;
        private final StackElement[] capturedArgs;

        public LambdaProxyObject(ExecutionContext context,
                                 MethodTypeObject invokedType,
                                 String samMethodName,
                                 MethodTypeObject samMethodType,
                                 MethodHandleObject implMethod,
                                 MethodTypeObject instantiatedMethodType,
                                 StackElement[] capturedArgs) {
            // Use Object as the class for now - in a full implementation, we'd generate a proper class
            super(context, context.getExecutionManager().loadClass(context, invokedType.getReturnType()));
            this.samMethodName = samMethodName;
            this.samMethodType = samMethodType;
            this.implMethod = implMethod;
            this.instantiatedMethodType = instantiatedMethodType;
            this.capturedArgs = capturedArgs;
        }

        public String getSamMethodName() {
            return samMethodName;
        }

        public MethodTypeObject getSamMethodType() {
            return samMethodType;
        }

        public MethodHandleObject getImplMethod() {
            return implMethod;
        }

        public StackElement[] getCapturedArgs() {
            return capturedArgs;
        }

        /**
         * Invoke the SAM method on this lambda proxy.
         */
        public ExecutionResult invokeSam(ExecutionContext context, StackElement... methodArgs) {
            // Combine captured args with method args
            StackElement[] allArgs = new StackElement[capturedArgs.length + methodArgs.length];
            System.arraycopy(capturedArgs, 0, allArgs, 0, capturedArgs.length);
            System.arraycopy(methodArgs, 0, allArgs, capturedArgs.length, methodArgs.length);

            // Invoke the implementation method
            return implMethod.invoke(context, allArgs);
        }

        @Override
        public String toString() {
            return "LambdaProxyObject{" + samMethodName + ", impl=" + implMethod + "}";
        }
    }

}
