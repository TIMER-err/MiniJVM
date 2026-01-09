package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.ClassPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to load JDK standard library classes from the runtime environment.
 */
public class JdkClassLoader {

    /**
     * Load a JDK class from the runtime and add it to the ClassPool.
     * 
     * @param classPool The ClassPool to add the class to
     * @param className The internal class name (e.g., "java/util/stream/Stream")
     * @return true if the class was loaded successfully
     */
    public static boolean loadJdkClass(ClassPool classPool, String className) {
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
     * Load multiple JDK classes at once.
     */
    public static void loadJdkClasses(ClassPool classPool, String... classNames) {
        for (String className : classNames) {
            loadJdkClass(classPool, className);
        }
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
