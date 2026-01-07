package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.CallSiteObject;
import net.lenni0451.minijvm.object.types.MethodHandleObject;
import net.lenni0451.minijvm.object.types.MethodTypeObject;
import net.lenni0451.minijvm.stack.StackDouble;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackFloat;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves and invokes bootstrap methods for invokedynamic instructions.
 */
public class BootstrapMethodResolver {

    private static final Type LOOKUP_TYPE = Type.getObjectType("java/lang/invoke/MethodHandles$Lookup");
    private static final Type METHOD_TYPE_TYPE = Type.getObjectType("java/lang/invoke/MethodType");
    private static final Type CALL_SITE_TYPE = Type.getObjectType("java/lang/invoke/CallSite");

    /**
     * Resolve an invokedynamic instruction by invoking its bootstrap method.
     *
     * @param context     The execution context
     * @param indy        The invokedynamic instruction node
     * @param callerClass The class containing the invokedynamic instruction
     * @return The resolved CallSite
     */
    public static CallSiteObject resolve(final ExecutionContext context,
                                         final InvokeDynamicInsnNode indy,
                                         final ExecutorClass callerClass) {
        ExecutionManager manager = context.getExecutionManager();

        // Get bootstrap method handle
        Handle bsm = indy.bsm;
        MethodHandleObject bsmHandle = new MethodHandleObject(context, bsm);

        // Prepare bootstrap method arguments:
        // 1. MethodHandles.Lookup caller
        // 2. String invokedName
        // 3. MethodType invokedType
        // 4. ...static arguments from bsmArgs

        List<StackElement> bsmArgList = new ArrayList<>();

        // 1. Create Lookup object for the caller class
        StackObject lookupObject = createLookup(context, callerClass);
        bsmArgList.add(lookupObject);

        // 2. Invoked name (String)
        StackElement invokedName = ExecutorTypeUtils.parse(context, indy.name);
        bsmArgList.add(invokedName);

        // 3. Invoked type (MethodType)
        MethodTypeObject invokedType = new MethodTypeObject(context, indy.desc);
        bsmArgList.add(new StackObject(invokedType));

        // 4. Static arguments from bsmArgs
        if (indy.bsmArgs != null) {
            for (Object arg : indy.bsmArgs) {
                StackElement converted = convertBootstrapArg(context, arg);
                bsmArgList.add(converted);
            }
        }

        // Invoke the bootstrap method
        StackElement[] bsmArgs = bsmArgList.toArray(new StackElement[0]);
        ExecutionResult result = bsmHandle.invoke(context, bsmArgs);

        if (result.hasException()) {
            throw new RuntimeException("Bootstrap method threw exception: " + result.getException());
        }

        if (!result.hasReturnValue()) {
            throw new RuntimeException("Bootstrap method did not return a value");
        }

        StackElement returnValue = result.getReturnValue();
        if (!(returnValue instanceof StackObject stackObject)) {
            throw new RuntimeException("Bootstrap method returned non-object: " + returnValue);
        }

        ExecutorObject callSiteObj = stackObject.value();
        if (callSiteObj instanceof CallSiteObject callSite) {
            return callSite;
        }

        // If the bootstrap method returned a raw CallSite (not our wrapper),
        // we need to extract the target MethodHandle
        throw new RuntimeException("Bootstrap method returned unexpected type: " + callSiteObj.getClazz().getClassNode().name);
    }

    /**
     * Create a MethodHandles.Lookup object for the given caller class.
     */
    public static StackObject createLookup(final ExecutionContext context, final ExecutorClass callerClass) {
        // Create a LookupObject that wraps the caller class
        LookupObject lookup = new LookupObject(context, callerClass);
        return new StackObject(lookup);
    }

    /**
     * Convert a bootstrap method argument from ASM representation to executor representation.
     */
    public static StackElement convertBootstrapArg(final ExecutionContext context, final Object arg) {
        if (arg == null) {
            return StackObject.NULL;
        }

        if (arg instanceof Integer i) {
            return new StackInt(i);
        } else if (arg instanceof Long l) {
            return new StackLong(l);
        } else if (arg instanceof Float f) {
            return new StackFloat(f);
        } else if (arg instanceof Double d) {
            return new StackDouble(d);
        } else if (arg instanceof String s) {
            return ExecutorTypeUtils.parse(context, s);
        } else if (arg instanceof Type t) {
            if (t.getSort() == Type.METHOD) {
                // MethodType
                return new StackObject(new MethodTypeObject(context, t.getDescriptor()));
            } else {
                // Class
                ExecutorClass typeClass = context.getExecutionManager().loadClass(context, t);
                return new StackObject(context.getExecutionManager().instantiateClass(context, typeClass));
            }
        } else if (arg instanceof Handle h) {
            // MethodHandle
            return new StackObject(new MethodHandleObject(context, h));
        } else {
            throw new UnsupportedOperationException("Unsupported bootstrap argument type: " + arg.getClass().getName());
        }
    }

    /**
     * Represents a MethodHandles.Lookup object in the executor.
     */
    public static class LookupObject extends ExecutorObject {

        private final ExecutorClass lookupClass;

        public LookupObject(final ExecutionContext context, final ExecutorClass lookupClass) {
            super(context, context.getExecutionManager().loadClass(context, LOOKUP_TYPE));
            this.lookupClass = lookupClass;
        }

        public ExecutorClass getLookupClass() {
            return this.lookupClass;
        }

        @Override
        public String toString() {
            return "LookupObject{" + lookupClass.getClassNode().name + "}";
        }
    }

}
