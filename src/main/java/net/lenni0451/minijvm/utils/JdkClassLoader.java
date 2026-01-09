package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.ClassPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility to load JDK standard library classes from the runtime environment.
 * Uses a blacklist approach: loads all JDK classes except those we've replaced.
 */
public class JdkClassLoader {

    private static final Set<String> BLACKLIST = new HashSet<>();

    static {
        // Classes we've implemented with native methods - don't load from JDK
        // These are handled by our native implementations in execution/natives/

        // String and encoding
        BLACKLIST.add("java/lang/String");
        BLACKLIST.add("java/lang/StringUTF16");
        // StringLatin1 removed - use JDK implementation
        // BLACKLIST.add("java/lang/StringLatin1");

        // Core classes with special native handling
        BLACKLIST.add("java/lang/Class");
        BLACKLIST.add("java/lang/Object");
        BLACKLIST.add("java/lang/System");
        BLACKLIST.add("java/lang/Runtime");
        BLACKLIST.add("java/lang/Thread");
        BLACKLIST.add("java/lang/ThreadLocal");
        BLACKLIST.add("java/lang/ClassLoader");

        // Reflection classes with native stubs
        BLACKLIST.add("java/lang/reflect/Field");
        BLACKLIST.add("java/lang/reflect/Method");
        BLACKLIST.add("java/lang/reflect/Constructor");
        BLACKLIST.add("java/lang/reflect/Array");

        // Unsafe and low-level access
        BLACKLIST.add("sun/misc/Unsafe");
        BLACKLIST.add("jdk/internal/misc/Unsafe");
        BLACKLIST.add("jdk/internal/access/SharedSecrets");
        BLACKLIST.add("jdk/internal/access/JavaLangAccess");

        // I/O classes with native implementations
        BLACKLIST.add("java/io/FileDescriptor");
        BLACKLIST.add("java/io/FileInputStream");
        BLACKLIST.add("java/io/FileOutputStream");
        BLACKLIST.add("java/io/PrintStream");

        // Primitive wrappers with native methods
        BLACKLIST.add("java/lang/Float");
        BLACKLIST.add("java/lang/Double");

        // Annotation handling
        BLACKLIST.add("sun/reflect/annotation/AnnotationParser");

        // Other internal classes we've stubbed
        BLACKLIST.add("sun/misc/Signal");
        BLACKLIST.add("java/security/AccessController");
        BLACKLIST.add("sun/security/action/GetPropertyAction");
        BLACKLIST.add("jdk/internal/misc/VM");
        BLACKLIST.add("java/util/concurrent/atomic/AtomicInteger");
        BLACKLIST.add("java/util/concurrent/atomic/AtomicLong");
        BLACKLIST.add("java/util/Random");

        // Throwable with special stack trace handling
        BLACKLIST.add("java/lang/Throwable");

        // Network classes with native methods
        BLACKLIST.add("java/net/InetAddress");
        BLACKLIST.add("java/net/Inet4Address");
        BLACKLIST.add("java/net/Inet6Address");

        // Filesystem
        BLACKLIST.add("java/io/UnixFileSystem");
        BLACKLIST.add("java/io/Win32FileSystem");

        // NIO Buffer classes with native access
        BLACKLIST.add("java/nio/Buffer");
        BLACKLIST.add("java/nio/ByteBuffer");
        BLACKLIST.add("java/nio/DirectByteBuffer");

        // Charset with native encoding
        BLACKLIST.add("java/nio/charset/Charset");
        BLACKLIST.add("java/nio/charset/CharsetEncoder");
        BLACKLIST.add("java/nio/charset/CharsetDecoder");
        BLACKLIST.add("sun/nio/cs/UTF_8");
        BLACKLIST.add("sun/nio/cs/US_ASCII");
        BLACKLIST.add("sun/nio/cs/ISO_8859_1");

        // Constant pool access
        BLACKLIST.add("sun/reflect/ConstantPool");

        // CDS (Class Data Sharing)
        BLACKLIST.add("jdk/internal/misc/CDS");

        // Properties and locales
        BLACKLIST.add("sun/util/locale/provider/LocaleProviderAdapter");
        BLACKLIST.add("java/util/Properties");

        // MethodHandles internals
        BLACKLIST.add("java/lang/invoke/MethodHandleNatives");
        BLACKLIST.add("java/lang/invoke/LambdaMetafactory");
        BLACKLIST.add("java/lang/invoke/StringConcatFactory");

        // Scoped memory access
        BLACKLIST.add("jdk/internal/misc/ScopedMemoryAccess");
    }

    /**
     * Check if a class should be loaded from JDK or handled by our implementation.
     *
     * @param className The internal class name (e.g., "java/util/stream/Stream")
     * @return true if we should try loading from JDK
     */
    public static boolean shouldLoadFromJdk(String className) {
        return !BLACKLIST.contains(className);
    }

    /**
     * Load a JDK class from the runtime and add it to the ClassPool.
     *
     * @param classPool The ClassPool to add the class to
     * @param className The internal class name (e.g., "java/util/stream/Stream")
     * @return true if the class was loaded successfully
     */
    public static boolean loadJdkClass(ClassPool classPool, String className) {
        // Check blacklist first
        if (!shouldLoadFromJdk(className)) {
            return false;
        }

        try {
            // Get the class file as a resource from the bootstrap classloader
            String resourceName = "/" + className + ".class";
            InputStream is = Object.class.getResourceAsStream(resourceName);

            if (is == null) {
                // Try with the system classloader
                is = ClassLoader.getSystemResourceAsStream(className + ".class");
            }

            if (is == null) {
                return false;
            }

            // Read the bytecode
            byte[] bytecode = readAllBytes(is);
            is.close();

            // Load into ClassPool
            return classPool.loadClass(className, bytecode);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Add a class to the blacklist (prevent loading from JDK).
     */
    public static void addToBlacklist(String className) {
        BLACKLIST.add(className);
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}

