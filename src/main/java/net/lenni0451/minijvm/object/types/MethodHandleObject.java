package net.lenni0451.minijvm.object.types;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a java.lang.invoke.MethodHandle in the executor.
 * Wraps method references and provides invocation capability.
 */
public class MethodHandleObject extends ExecutorObject {

    private static final Type METHOD_HANDLE = Type.getObjectType("java/lang/invoke/MethodHandle");

    // Handle kinds from java.lang.invoke.MethodHandleInfo
    public static final int REF_getField = 1;
    public static final int REF_getStatic = 2;
    public static final int REF_putField = 3;
    public static final int REF_putStatic = 4;
    public static final int REF_invokeVirtual = 5;
    public static final int REF_invokeStatic = 6;
    public static final int REF_invokeSpecial = 7;
    public static final int REF_newInvokeSpecial = 8;
    public static final int REF_invokeInterface = 9;

    private final int kind;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;
    private final MethodTypeObject methodType;

    public MethodHandleObject(final ExecutionContext context, final Handle handle) {
        super(context, context.getExecutionManager().loadClass(context, METHOD_HANDLE));
        this.kind = handle.getTag();
        this.owner = handle.getOwner();
        this.name = handle.getName();
        this.descriptor = handle.getDesc();
        this.isInterface = handle.isInterface();

        // For method handles, the descriptor is the method descriptor
        // For field handles, we need to construct the appropriate method type
        if (isFieldHandle()) {
            this.methodType = createFieldMethodType(context);
        } else if (kind == REF_newInvokeSpecial) {
            // Constructor: returns the new object, takes constructor params
            Type[] argTypes = Type.getArgumentTypes(descriptor);
            this.methodType = new MethodTypeObject(context, Type.getObjectType(owner), argTypes);
        } else {
            this.methodType = new MethodTypeObject(context, descriptor);
        }
    }

    public MethodHandleObject(final ExecutionContext context, final int kind, final String owner,
                               final String name, final String descriptor, final boolean isInterface) {
        super(context, context.getExecutionManager().loadClass(context, METHOD_HANDLE));
        this.kind = kind;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;

        if (isFieldHandle()) {
            this.methodType = createFieldMethodType(context);
        } else if (kind == REF_newInvokeSpecial) {
            Type[] argTypes = Type.getArgumentTypes(descriptor);
            this.methodType = new MethodTypeObject(context, Type.getObjectType(owner), argTypes);
        } else {
            this.methodType = new MethodTypeObject(context, descriptor);
        }
    }

    private boolean isFieldHandle() {
        return kind >= REF_getField && kind <= REF_putStatic;
    }

    private MethodTypeObject createFieldMethodType(ExecutionContext context) {
        Type fieldType = Type.getType(descriptor);
        Type ownerType = Type.getObjectType(owner);

        return switch (kind) {
            case REF_getField -> new MethodTypeObject(context, fieldType, new Type[]{ownerType});
            case REF_getStatic -> new MethodTypeObject(context, fieldType, new Type[0]);
            case REF_putField -> new MethodTypeObject(context, Type.VOID_TYPE, new Type[]{ownerType, fieldType});
            case REF_putStatic -> new MethodTypeObject(context, Type.VOID_TYPE, new Type[]{fieldType});
            default -> throw new IllegalStateException("Not a field handle: " + kind);
        };
    }

    public int getKind() {
        return this.kind;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public boolean isInterface() {
        return this.isInterface;
    }

    public MethodTypeObject getMethodType() {
        return this.methodType;
    }

    /**
     * Invoke this method handle with the given arguments.
     */
    public ExecutionResult invoke(final ExecutionContext context, final StackElement... args) {
        try {
            return switch (kind) {
                case REF_getField -> invokeGetField(context, args);
                case REF_getStatic -> invokeGetStatic(context, args);
                case REF_putField -> invokePutField(context, args);
                case REF_putStatic -> invokePutStatic(context, args);
                case REF_invokeVirtual -> invokeVirtual(context, args);
                case REF_invokeStatic -> invokeStatic(context, args);
                case REF_invokeSpecial -> invokeSpecial(context, args);
                case REF_newInvokeSpecial -> invokeNewSpecial(context, args);
                case REF_invokeInterface -> invokeInterface(context, args);
                default -> throw new UnsupportedOperationException("Unsupported handle kind: " + kind);
            };
        } catch (Exception e) {
            return ExceptionUtils.newException(context, Types.INTERNAL_ERROR,
                "MethodHandle invocation failed: " + e.getMessage());
        }
    }

    private ExecutionResult invokeGetField(ExecutionContext context, StackElement[] args) {
        if (args.length < 1) {
            return ExceptionUtils.newException(context, Types.ILLEGAL_ARGUMENT_EXCEPTION, "getField requires object instance");
        }
        StackObject obj = (StackObject) args[0];
        if (obj.isNull()) {
            return ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Cannot get field from null");
        }
        ExecutorObject instance = obj.value();
        ExecutorClass.ResolvedField field = instance.getClazz().findField(context, name, descriptor);
        if (field == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, owner + "." + name);
        }
        return ExecutionResult.returnValue(instance.getField(field.field()));
    }

    private ExecutionResult invokeGetStatic(ExecutionContext context, StackElement[] args) {
        ExecutorClass ownerClass = context.getExecutionManager().loadClass(context, Type.getObjectType(owner));
        ExecutorClass.ResolvedField field = ownerClass.findField(context, name, descriptor);
        if (field == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, owner + "." + name);
        }
        return ExecutionResult.returnValue(field.get());
    }

    private ExecutionResult invokePutField(ExecutionContext context, StackElement[] args) {
        if (args.length < 2) {
            return ExceptionUtils.newException(context, Types.ILLEGAL_ARGUMENT_EXCEPTION, "putField requires object and value");
        }
        StackObject obj = (StackObject) args[0];
        if (obj.isNull()) {
            return ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Cannot put field to null");
        }
        ExecutorObject instance = obj.value();
        ExecutorClass.ResolvedField field = instance.getClazz().findField(context, name, descriptor);
        if (field == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, owner + "." + name);
        }
        instance.setField(field.field(), args[1]);
        return ExecutionResult.voidResult();
    }

    private ExecutionResult invokePutStatic(ExecutionContext context, StackElement[] args) {
        if (args.length < 1) {
            return ExceptionUtils.newException(context, Types.ILLEGAL_ARGUMENT_EXCEPTION, "putStatic requires value");
        }
        ExecutorClass ownerClass = context.getExecutionManager().loadClass(context, Type.getObjectType(owner));
        ExecutorClass.ResolvedField field = ownerClass.findField(context, name, descriptor);
        if (field == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, owner + "." + name);
        }
        field.set(args[0]);
        return ExecutionResult.voidResult();
    }

    private ExecutionResult invokeVirtual(ExecutionContext context, StackElement[] args) {
        if (args.length < 1) {
            return ExceptionUtils.newException(context, Types.ILLEGAL_ARGUMENT_EXCEPTION, "invokeVirtual requires object instance");
        }
        StackObject obj = (StackObject) args[0];
        if (obj.isNull()) {
            return ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Cannot invoke method on null");
        }
        ExecutorObject instance = obj.value();

        // Virtual dispatch: use actual object's class
        ExecutorClass.ResolvedMethod method = instance.getClazz().findMethod(context, name, descriptor);
        if (method == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, owner + "." + name + descriptor);
        }

        // Remove instance from args
        StackElement[] methodArgs = new StackElement[args.length - 1];
        System.arraycopy(args, 1, methodArgs, 0, methodArgs.length);

        return Executor.execute(context, method.owner(), method.method(), instance, methodArgs);
    }

    private ExecutionResult invokeStatic(ExecutionContext context, StackElement[] args) {
        ExecutorClass ownerClass = context.getExecutionManager().loadClass(context, Type.getObjectType(owner));
        ExecutorClass.ResolvedMethod method = ownerClass.findMethod(context, name, descriptor);
        if (method == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, owner + "." + name + descriptor);
        }
        return Executor.execute(context, method.owner(), method.method(), null, args);
    }

    private ExecutionResult invokeSpecial(ExecutionContext context, StackElement[] args) {
        if (args.length < 1) {
            return ExceptionUtils.newException(context, Types.ILLEGAL_ARGUMENT_EXCEPTION, "invokeSpecial requires object instance");
        }
        StackObject obj = (StackObject) args[0];
        if (obj.isNull()) {
            return ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Cannot invoke method on null");
        }
        ExecutorObject instance = obj.value();

        // Special dispatch: use declared owner class (for super calls, private methods)
        ExecutorClass ownerClass = context.getExecutionManager().loadClass(context, Type.getObjectType(owner));
        ExecutorClass.ResolvedMethod method = ownerClass.findMethod(context, name, descriptor);
        if (method == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, owner + "." + name + descriptor);
        }

        StackElement[] methodArgs = new StackElement[args.length - 1];
        System.arraycopy(args, 1, methodArgs, 0, methodArgs.length);

        return Executor.execute(context, method.owner(), method.method(), instance, methodArgs);
    }

    private ExecutionResult invokeNewSpecial(ExecutionContext context, StackElement[] args) {
        // Create new instance and invoke constructor
        ExecutorClass ownerClass = context.getExecutionManager().loadClass(context, Type.getObjectType(owner));
        ExecutorObject newInstance = context.getExecutionManager().instantiate(context, ownerClass);

        // Find constructor
        MethodNode constructor = ASMUtils.getMethod(ownerClass.getClassNode(), "<init>", descriptor);
        if (constructor == null) {
            return ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, owner + ".<init>" + descriptor);
        }

        // Invoke constructor
        ExecutionResult initResult = Executor.execute(context, ownerClass, constructor, newInstance, args);
        if (initResult.hasException()) {
            return initResult;
        }

        // Return the new instance
        return ExecutionResult.returnValue(new StackObject(newInstance));
    }

    private ExecutionResult invokeInterface(ExecutionContext context, StackElement[] args) {
        // Interface dispatch is same as virtual dispatch
        return invokeVirtual(context, args);
    }

    @Override
    public String toString() {
        String kindName = switch (kind) {
            case REF_getField -> "getField";
            case REF_getStatic -> "getStatic";
            case REF_putField -> "putField";
            case REF_putStatic -> "putStatic";
            case REF_invokeVirtual -> "invokeVirtual";
            case REF_invokeStatic -> "invokeStatic";
            case REF_invokeSpecial -> "invokeSpecial";
            case REF_newInvokeSpecial -> "newInvokeSpecial";
            case REF_invokeInterface -> "invokeInterface";
            default -> "unknown(" + kind + ")";
        };
        return "MethodHandleObject{" + kindName + " " + owner + "." + name + descriptor + "}";
    }

}
