package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

/**
 * Native method implementations for file system classes.
 */
public class FileSystemNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // UnixFileSystem.initIDs() - initializes IDs
        manager.registerMethodExecutor("java/io/UnixFileSystem.initIDs()V", MethodExecutor.NOOP_VOID);

        // WinNTFileSystem.initIDs() - Windows file system
        manager.registerMethodExecutor("java/io/WinNTFileSystem.initIDs()V", MethodExecutor.NOOP_VOID);

        // FileSystem.getFileSystem() - returns file system
        manager.registerMethodExecutor("java/io/FileSystem.getFileSystem()Ljava/io/FileSystem;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return a UnixFileSystem instance
            net.lenni0451.minijvm.object.ExecutorClass fsClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getObjectType("java/io/UnixFileSystem"));
            net.lenni0451.minijvm.object.ExecutorObject fs =
                context.getExecutionManager().instantiate(context, fsClass);
            return ExecutionResult.returnValue(new StackObject(fs));
        });

        // UnixFileSystem.canonicalize0(String) - canonicalize path
        manager.registerMethodExecutor("java/io/UnixFileSystem.canonicalize0(Ljava/lang/String;)Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return the input path as-is
            return ExecutionResult.returnValue(arguments[0]);
        });

        // UnixFileSystem.getBooleanAttributes0(String) - get file attributes
        manager.registerMethodExecutor("java/io/UnixFileSystem.getBooleanAttributes0(Ljava/lang/String;)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return exists flag (0x01)
            return ExecutionResult.returnValue(new StackInt(0x01));
        });

        // UnixFileSystem.getBooleanAttributes0(File) - get file attributes (different signature)
        manager.registerMethodExecutor("java/io/UnixFileSystem.getBooleanAttributes0(Ljava/io/File;)I", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return exists flag (0x01)
            return ExecutionResult.returnValue(new StackInt(0x01));
        });

        // UnixFileSystem.checkAccess(File, int) - check file access
        manager.registerMethodExecutor("java/io/UnixFileSystem.checkAccess(Ljava/io/File;I)Z", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return true - access allowed
            return ExecutionResult.returnValue(new StackInt(1));
        });

        // UnixFileSystem.getLength(File) - get file length
        manager.registerMethodExecutor("java/io/UnixFileSystem.getLength(Ljava/io/File;)J", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return 0
            return ExecutionResult.returnValue(new StackLong(0L));
        });

        // UnixFileSystem.getLastModifiedTime(File) - get last modified time
        manager.registerMethodExecutor("java/io/UnixFileSystem.getLastModifiedTime(Ljava/io/File;)J", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return current time
            return ExecutionResult.returnValue(new StackLong(System.currentTimeMillis()));
        });

        // UnixFileSystem.list(File) - list directory contents
        manager.registerMethodExecutor("java/io/UnixFileSystem.list(Ljava/io/File;)[Ljava/lang/String;", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return empty array
            net.lenni0451.minijvm.object.ExecutorClass arrayClass =
                context.getExecutionManager().loadClass(context, org.objectweb.asm.Type.getType("[Ljava/lang/String;"));
            net.lenni0451.minijvm.object.types.ArrayObject emptyArray =
                new net.lenni0451.minijvm.object.types.ArrayObject(context, arrayClass, new net.lenni0451.minijvm.stack.StackElement[0]);
            return ExecutionResult.returnValue(new StackObject(emptyArray));
        });

        // File.isInvalid() - check if file path is invalid
        manager.registerMethodExecutor("java/io/File.isInvalid()Z", (context, currentClass, currentMethod, instance, arguments) -> {
            // Return false - path is valid
            return ExecutionResult.returnValue(new StackInt(0));
        });
    }
}
