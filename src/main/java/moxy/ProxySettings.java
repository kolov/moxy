package moxy;

import java.util.concurrent.Executor;

/**
 */
public class ProxySettings {
    private ProxyMapping mapping;
    private Executor executor;

    public ProxyMapping getMapping() {
        return mapping;
    }

    public void setMapping(ProxyMapping mapping) {
        this.mapping = mapping;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
