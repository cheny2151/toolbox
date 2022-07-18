package cn.cheny.toolbox.redis.lock.awaken.listener;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cheney
 */
@Getter
@Setter
public class LockListener {

    private String listenerChannel;

    private Handle handle;

    public LockListener(String listenerChannel, Handle handle) {
        this.listenerChannel = listenerChannel;
        this.handle = handle;
    }

    @FunctionalInterface
    public interface Handle {
        void handle();
    }

    public void handleListener() {
        handle.handle();
    }

}
