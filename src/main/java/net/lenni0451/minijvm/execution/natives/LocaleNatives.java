package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

/**
 * Stub implementation for Locale-related classes.
 * Provides simplified locale support without complex initialization.
 */
public class LocaleNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass BaseLocale's static initializer
        manager.registerMethodExecutor("sun/util/locale/BaseLocale.<clinit>()V", MethodExecutor.NOOP_VOID);

        // BaseLocale.createInstance(String, String) - creates a BaseLocale instance
        manager.registerMethodExecutor("sun/util/locale/BaseLocale.createInstance(Ljava/lang/String;Ljava/lang/String;)Lsun/util/locale/BaseLocale;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a simple BaseLocale instance
            // In reality, this would create a cached instance
            net.lenni0451.minijvm.object.ExecutorClass baseLocaleClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("sun/util/locale/BaseLocale"));
            net.lenni0451.minijvm.object.ExecutorObject baseLocale =
                context.getExecutionManager().instantiate(context, baseLocaleClass);
            return ExecutionResult.returnValue(new StackObject(baseLocale));
        });

        // BaseLocale.getInstance(String, String, String, String) - gets a BaseLocale instance
        manager.registerMethodExecutor("sun/util/locale/BaseLocale.getInstance(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lsun/util/locale/BaseLocale;", (context, currentClass, currentMethod, instance, arguments) -> {
            net.lenni0451.minijvm.object.ExecutorClass baseLocaleClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("sun/util/locale/BaseLocale"));
            net.lenni0451.minijvm.object.ExecutorObject baseLocale =
                context.getExecutionManager().instantiate(context, baseLocaleClass);
            return ExecutionResult.returnValue(new StackObject(baseLocale));
        });

        // Bypass LocaleObjectCache static initializer
        manager.registerMethodExecutor("sun/util/locale/LocaleObjectCache.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Locale's static initializer but manually initialize ENGLISH field
        manager.registerMethodExecutor("java/util/Locale.<clinit>()V", (context, currentClass, currentMethod, instance, arguments) -> {
            // Create a Locale instance for ENGLISH
            net.lenni0451.minijvm.object.ExecutorClass localeClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/util/Locale"));
            net.lenni0451.minijvm.object.ExecutorObject englishLocale =
                context.getExecutionManager().instantiate(context, localeClass);

            // Set the ENGLISH static field
            net.lenni0451.minijvm.object.ExecutorClass.ResolvedField englishField =
                localeClass.findField(context, "ENGLISH", "Ljava/util/Locale;");
            if (englishField != null) {
                localeClass.setStaticField(englishField.field(), new net.lenni0451.minijvm.stack.StackObject(englishLocale));
            }

            return net.lenni0451.minijvm.execution.ExecutionResult.voidResult();
        });

        // Locale() constructor
        manager.registerMethodExecutor("java/util/Locale.<init>(Lsun/util/locale/BaseLocale;Lsun/util/locale/LocaleExtensions;)V", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.voidResult();
        });

        // Locale.getDefault() - returns default locale
        manager.registerMethodExecutor("java/util/Locale.getDefault()Ljava/util/Locale;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a simple US English locale
            net.lenni0451.minijvm.object.ExecutorClass localeClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/util/Locale"));
            net.lenni0451.minijvm.object.ExecutorObject locale =
                context.getExecutionManager().instantiate(context, localeClass);
            return ExecutionResult.returnValue(new StackObject(locale));
        });

        // Locale.getLanguage() - returns language code
        manager.registerMethodExecutor("java/util/Locale.getLanguage()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, "en"));
        });

        // Locale.getCountry() - returns country code
        manager.registerMethodExecutor("java/util/Locale.getCountry()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, "US"));
        });

        // Locale.toString() - returns string representation
        manager.registerMethodExecutor("java/util/Locale.toString()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, "en_US"));
        });

        // Locale.hashCode() - returns hash code
        manager.registerMethodExecutor("java/util/Locale.hashCode()I", (context, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackInt("en_US".hashCode()));
        });
    }
}
