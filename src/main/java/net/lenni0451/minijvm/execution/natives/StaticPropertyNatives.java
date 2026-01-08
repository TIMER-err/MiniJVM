package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

/**
 * Stub implementation for jdk.internal.util.StaticProperty.
 * Provides system property values without complex initialization.
 */
public class StaticPropertyNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass StaticProperty's static initializer
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.<clinit>()V", MethodExecutor.NOOP_VOID);

        // javaHome() - returns java.home property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.javaHome()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String javaHome = System.getProperty("java.home", "/usr/lib/jvm/default");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, javaHome));
        });

        // userHome() - returns user.home property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.userHome()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String userHome = System.getProperty("user.home", "/home/user");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, userHome));
        });

        // userDir() - returns user.dir property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.userDir()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String userDir = System.getProperty("user.dir", "/tmp");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, userDir));
        });

        // userName() - returns user.name property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.userName()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String userName = System.getProperty("user.name", "user");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, userName));
        });

        // javaLibraryPath() - returns java.library.path property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.javaLibraryPath()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String libraryPath = System.getProperty("java.library.path", "/usr/lib");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, libraryPath));
        });

        // sunBootLibraryPath() - returns sun.boot.library.path property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.sunBootLibraryPath()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String bootLibraryPath = System.getProperty("sun.boot.library.path", "/usr/lib");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, bootLibraryPath));
        });

        // jdkSerialFilter() - returns jdk.serialFilter property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.jdkSerialFilter()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no serial filter by default
            return ExecutionResult.returnValue(StackObject.NULL);
        });

        // nativeEncoding() - returns native.encoding property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.nativeEncoding()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String encoding = System.getProperty("native.encoding", "UTF-8");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, encoding));
        });

        // fileEncoding() - returns file.encoding property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.fileEncoding()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            String encoding = System.getProperty("file.encoding", "UTF-8");
            return ExecutionResult.returnValue(ExecutorTypeUtils.parse(context, encoding));
        });

        // javaPropertiesDate() - returns java.properties.date property
        manager.registerMethodExecutor("jdk/internal/util/StaticProperty.javaPropertiesDate()Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return null - no special date format
            return ExecutionResult.returnValue(StackObject.NULL);
        });
    }
}
