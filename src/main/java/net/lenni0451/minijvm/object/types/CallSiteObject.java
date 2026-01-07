package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import org.objectweb.asm.Type;

/**
 * Represents a java.lang.invoke.CallSite in the executor.
 * CallSite caches the result of bootstrap method invocation.
 */
public class CallSiteObject extends ExecutorObject {

    private static final Type CALL_SITE = Type.getObjectType("java/lang/invoke/CallSite");

    public enum CallSiteType {
        CONSTANT,   // ConstantCallSite - target cannot change
        MUTABLE,    // MutableCallSite - target can change
        VOLATILE    // VolatileCallSite - target changes are immediately visible
    }

    private final CallSiteType type;
    private final MethodTypeObject methodType;
    private volatile MethodHandleObject target;

    public CallSiteObject(final ExecutionContext context, final CallSiteType type,
                          final MethodTypeObject methodType, final MethodHandleObject target) {
        super(context, context.getExecutionManager().loadClass(context, CALL_SITE));
        this.type = type;
        this.methodType = methodType;
        this.target = target;
    }

    /**
     * Create a ConstantCallSite with the given target.
     */
    public static CallSiteObject constant(final ExecutionContext context,
                                          final MethodTypeObject methodType,
                                          final MethodHandleObject target) {
        return new CallSiteObject(context, CallSiteType.CONSTANT, methodType, target);
    }

    /**
     * Create a MutableCallSite with the given target.
     */
    public static CallSiteObject mutable(final ExecutionContext context,
                                         final MethodTypeObject methodType,
                                         final MethodHandleObject target) {
        return new CallSiteObject(context, CallSiteType.MUTABLE, methodType, target);
    }

    public CallSiteType getCallSiteType() {
        return this.type;
    }

    public MethodTypeObject getMethodType() {
        return this.methodType;
    }

    public MethodHandleObject getTarget() {
        return this.target;
    }

    public void setTarget(final MethodHandleObject target) {
        if (this.type == CallSiteType.CONSTANT) {
            throw new UnsupportedOperationException("Cannot change target of ConstantCallSite");
        }
        this.target = target;
    }

    public boolean isConstant() {
        return this.type == CallSiteType.CONSTANT;
    }

    @Override
    public String toString() {
        return "CallSiteObject{type=" + type + ", target=" + target + "}";
    }

}
