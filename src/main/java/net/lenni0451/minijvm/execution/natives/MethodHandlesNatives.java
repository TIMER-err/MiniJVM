package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.BootstrapMethodResolver;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.object.types.MethodHandleObject;
import net.lenni0451.minijvm.object.types.MethodTypeObject;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * Native method implementations for java.lang.invoke.MethodHandles and related classes.
 */
public class MethodHandlesNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // MethodHandles.lookup() - returns a Lookup object
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles.lookup()Ljava/lang/invoke/MethodHandles$Lookup;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // Create a Lookup object for the caller class
                // We use the current class as the lookup class
                BootstrapMethodResolver.LookupObject lookup =
                    new BootstrapMethodResolver.LookupObject(context, currentClass);
                return returnValue(new StackObject(lookup));
            }
        );

        // MethodHandles.publicLookup() - returns a public Lookup object
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles.publicLookup()Ljava/lang/invoke/MethodHandles$Lookup;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                // For simplicity, return a lookup with Object as the lookup class
                ExecutorClass objectClass = context.getExecutionManager().loadClass(context, Type.getType(Object.class));
                BootstrapMethodResolver.LookupObject lookup =
                    new BootstrapMethodResolver.LookupObject(context, objectClass);
                return returnValue(new StackObject(lookup));
            }
        );

        // Lookup.findVirtual
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findVirtual(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                String name = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[1]).value());
                MethodTypeObject type = (MethodTypeObject) ((StackObject) arguments[2]).value();

                String owner = refc.getClassType().getClassNode().name;
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_invokeVirtual, owner, name, type.getDescriptor(), false);
                return returnValue(new StackObject(mh));
            }
        );

        // Lookup.findStatic
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findStatic(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                String name = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[1]).value());
                MethodTypeObject type = (MethodTypeObject) ((StackObject) arguments[2]).value();

                String owner = refc.getClassType().getClassNode().name;
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_invokeStatic, owner, name, type.getDescriptor(), false);
                return returnValue(new StackObject(mh));
            }
        );

        // Lookup.findSpecial
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findSpecial(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                String name = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[1]).value());
                MethodTypeObject type = (MethodTypeObject) ((StackObject) arguments[2]).value();
                // arguments[3] is specialCaller, ignored for simplicity

                String owner = refc.getClassType().getClassNode().name;
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_invokeSpecial, owner, name, type.getDescriptor(), false);
                return returnValue(new StackObject(mh));
            }
        );

        // Lookup.findConstructor
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findConstructor(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                MethodTypeObject type = (MethodTypeObject) ((StackObject) arguments[1]).value();

                String owner = refc.getClassType().getClassNode().name;
                // Constructor descriptor: change return type to void
                String desc = Type.getMethodDescriptor(Type.VOID_TYPE, type.getParameterTypes());
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_newInvokeSpecial, owner, "<init>", desc, false);
                return returnValue(new StackObject(mh));
            }
        );

        // Lookup.findGetter
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                String name = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[1]).value());
                ClassObject fieldType = (ClassObject) ((StackObject) arguments[2]).value();

                String owner = refc.getClassType().getClassNode().name;
                String desc = fieldType.getClassType().getType().getDescriptor();
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_getField, owner, name, desc, false);
                return returnValue(new StackObject(mh));
            }
        );

        // Lookup.findStaticGetter
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.findStaticGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject refc = (ClassObject) ((StackObject) arguments[0]).value();
                String name = ExecutorTypeUtils.fromExecutorString(context, ((StackObject) arguments[1]).value());
                ClassObject fieldType = (ClassObject) ((StackObject) arguments[2]).value();

                String owner = refc.getClassType().getClassNode().name;
                String desc = fieldType.getClassType().getType().getDescriptor();
                MethodHandleObject mh = new MethodHandleObject(context,
                    MethodHandleObject.REF_getStatic, owner, name, desc, false);
                return returnValue(new StackObject(mh));
            }
        );

        // MethodType.methodType variants
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodType.methodType(Ljava/lang/Class;)Ljava/lang/invoke/MethodType;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject rtype = (ClassObject) ((StackObject) arguments[0]).value();
                Type returnType = rtype.getClassType().getType();
                MethodTypeObject mt = new MethodTypeObject(context, returnType, new Type[0]);
                return returnValue(new StackObject(mt));
            }
        );

        manager.registerMethodExecutor(
            "java/lang/invoke/MethodType.methodType(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject rtype = (ClassObject) ((StackObject) arguments[0]).value();
                ClassObject ptype0 = (ClassObject) ((StackObject) arguments[1]).value();
                Type returnType = rtype.getClassType().getType();
                Type[] paramTypes = new Type[]{ptype0.getClassType().getType()};
                MethodTypeObject mt = new MethodTypeObject(context, returnType, paramTypes);
                return returnValue(new StackObject(mt));
            }
        );

        manager.registerMethodExecutor(
            "java/lang/invoke/MethodType.methodType(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                ClassObject rtype = (ClassObject) ((StackObject) arguments[0]).value();
                StackObject ptypesArray = (StackObject) arguments[1];

                Type returnType = rtype.getClassType().getType();
                Type[] paramTypes;

                if (ptypesArray.isNull()) {
                    paramTypes = new Type[0];
                } else {
                    net.lenni0451.minijvm.object.types.ArrayObject arr =
                        (net.lenni0451.minijvm.object.types.ArrayObject) ptypesArray.value();
                    paramTypes = new Type[arr.getElements().length];
                    for (int i = 0; i < arr.getElements().length; i++) {
                        ClassObject ptype = (ClassObject) ((StackObject) arr.getElements()[i]).value();
                        paramTypes[i] = ptype.getClassType().getType();
                    }
                }

                MethodTypeObject mt = new MethodTypeObject(context, returnType, paramTypes);
                return returnValue(new StackObject(mt));
            }
        );

        // MethodHandle.invoke and invokeExact - delegate to our invoke implementation
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandle.invoke([Ljava/lang/Object;)Ljava/lang/Object;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                MethodHandleObject mh = (MethodHandleObject) instance;
                return mh.invoke(context, arguments);
            }
        );

        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandle.invokeExact([Ljava/lang/Object;)Ljava/lang/Object;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                MethodHandleObject mh = (MethodHandleObject) instance;
                return mh.invoke(context, arguments);
            }
        );

        // Lookup.lookupClass
        manager.registerMethodExecutor(
            "java/lang/invoke/MethodHandles$Lookup.lookupClass()Ljava/lang/Class;",
            (context, currentClass, currentMethod, instance, arguments) -> {
                BootstrapMethodResolver.LookupObject lookup = (BootstrapMethodResolver.LookupObject) instance;
                ExecutorClass lookupClass = lookup.getLookupClass();
                return returnValue(new StackObject(context.getExecutionManager().instantiateClass(context, lookupClass)));
            }
        );
    }

}
