package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.object.types.CallSiteObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for CallSite objects resolved from invokedynamic instructions.
 * Each invokedynamic instruction is uniquely identified by (class, method, instruction index).
 */
public class InvokeDynamicCache {

    private final Map<CallSiteKey, CallSiteObject> cache;

    public InvokeDynamicCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Get a cached CallSite for the given invokedynamic instruction.
     *
     * @param className       The internal name of the class containing the instruction
     * @param methodSignature The method name + descriptor (e.g., "main([Ljava/lang/String;)V")
     * @param instructionIndex The index of the invokedynamic instruction in the method
     * @return The cached CallSite, or null if not cached
     */
    public CallSiteObject get(final String className, final String methodSignature, final int instructionIndex) {
        return this.cache.get(new CallSiteKey(className, methodSignature, instructionIndex));
    }

    /**
     * Cache a CallSite for the given invokedynamic instruction.
     *
     * @param className       The internal name of the class containing the instruction
     * @param methodSignature The method name + descriptor
     * @param instructionIndex The index of the invokedynamic instruction in the method
     * @param callSite        The CallSite to cache
     */
    public void put(final String className, final String methodSignature, final int instructionIndex,
                    final CallSiteObject callSite) {
        this.cache.put(new CallSiteKey(className, methodSignature, instructionIndex), callSite);
    }

    /**
     * Check if a CallSite is cached for the given invokedynamic instruction.
     */
    public boolean contains(final String className, final String methodSignature, final int instructionIndex) {
        return this.cache.containsKey(new CallSiteKey(className, methodSignature, instructionIndex));
    }

    /**
     * Clear all cached CallSites.
     */
    public void clear() {
        this.cache.clear();
    }

    /**
     * Get the number of cached CallSites.
     */
    public int size() {
        return this.cache.size();
    }

    /**
     * Key for identifying a unique invokedynamic instruction.
     */
    private record CallSiteKey(String className, String methodSignature, int instructionIndex) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CallSiteKey that)) return false;
            return instructionIndex == that.instructionIndex
                    && Objects.equals(className, that.className)
                    && Objects.equals(methodSignature, that.methodSignature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodSignature, instructionIndex);
        }
    }

}
