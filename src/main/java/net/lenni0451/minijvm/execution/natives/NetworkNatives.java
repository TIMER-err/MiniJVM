package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;

import java.util.function.Consumer;

/**
 * Stub implementation for networking classes.
 */
public class NetworkNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        // Bypass IPAddressUtil static initializer
        manager.registerMethodExecutor("sun/net/util/IPAddressUtil.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass InetAddress static initializer
        manager.registerMethodExecutor("java/net/InetAddress.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Inet4Address static initializer
        manager.registerMethodExecutor("java/net/Inet4Address.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass Inet6Address static initializer
        manager.registerMethodExecutor("java/net/Inet6Address.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass InetSocketAddress static initializer
        manager.registerMethodExecutor("java/net/InetSocketAddress.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass URL static initializer
        manager.registerMethodExecutor("java/net/URL.<clinit>()V", MethodExecutor.NOOP_VOID);

        // Bypass URI static initializer
        manager.registerMethodExecutor("java/net/URI.<clinit>()V", MethodExecutor.NOOP_VOID);
    }
}
