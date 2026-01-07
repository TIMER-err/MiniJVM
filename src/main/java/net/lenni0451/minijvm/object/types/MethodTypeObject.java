package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * Represents a java.lang.invoke.MethodType in the executor.
 * MethodType is immutable and describes the parameter types and return type of a method.
 */
public class MethodTypeObject extends ExecutorObject {

    private static final Type METHOD_TYPE = Type.getObjectType("java/lang/invoke/MethodType");

    private final Type returnType;
    private final Type[] parameterTypes;
    private final String descriptor;

    public MethodTypeObject(final ExecutionContext context, final String descriptor) {
        super(context, context.getExecutionManager().loadClass(context, METHOD_TYPE));
        this.descriptor = descriptor;
        this.returnType = Type.getReturnType(descriptor);
        this.parameterTypes = Type.getArgumentTypes(descriptor);
    }

    public MethodTypeObject(final ExecutionContext context, final Type returnType, final Type[] parameterTypes) {
        super(context, context.getExecutionManager().loadClass(context, METHOD_TYPE));
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.descriptor = Type.getMethodDescriptor(returnType, parameterTypes);
    }

    public Type getReturnType() {
        return this.returnType;
    }

    public Type[] getParameterTypes() {
        return this.parameterTypes;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public int parameterCount() {
        return this.parameterTypes.length;
    }

    public Type parameterType(int index) {
        return this.parameterTypes[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MethodTypeObject other)) return false;
        return this.descriptor.equals(other.descriptor);
    }

    @Override
    public int hashCode() {
        return this.descriptor.hashCode();
    }

    @Override
    public String toString() {
        return "MethodTypeObject{" + this.descriptor + "}";
    }

}
