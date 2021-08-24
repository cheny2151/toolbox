package cn.cheny.toolbox.asyncTask.poolmanager;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cheney
 * @date 2021-08-14
 */
@Data
public class ResourceWithExpired<R> {

    public final static int NORMAL = 0;
    public final static int POLLED = 1;
    public final static int EXPIRED = 2;

    private R resource;

    private long expiredTime;

    private volatile int status;

    private volatile AtomicInteger useTime = new AtomicInteger();

    public ResourceWithExpired(R resource, long expiredTime) {
        this.resource = resource;
        this.expiredTime = expiredTime;
        this.status = NORMAL;
    }

    public R getResource() {
        this.useTime.incrementAndGet();
        this.status = POLLED;
        return resource;
    }

    public boolean isPolled(){
        return this.status == POLLED;
    }

    public boolean isExpired(){
        return this.status == EXPIRED;
    }

    public int getUseTime() {
        return useTime.get();
    }
}
