package cn.cheny.toolbox.asyncTask.poolmanager;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 存储结构为单向链表，通过定时任务清除过期资源
 *
 * @author cheney
 * @date 2021-08-13
 */
public class ExpiredResourceLinkManager<R> extends BaseResourceManager<R> {

    /**
     * status value
     */
    private static final int NORMAL_STATUS = 0;
    private static final int REVERSING_STATUS = 1;
    private static final int CLEARING_STATUS = 2;
    private static final int CLOSE_STATUS = -1;

    private final long defaultExpiredMillis;

    private volatile SimpleLinkList<ResourceWithExpired<R>> writeList;

    private volatile SimpleLinkList<ResourceWithExpired<R>> readList;

    private final AtomicInteger status = new AtomicInteger(0);

    public ExpiredResourceLinkManager(long defaultExpiredTime, TimeUnit timeUnit) {
        this(DEFAULT_DELAY_SECONDS, defaultExpiredTime, timeUnit);
    }

    public ExpiredResourceLinkManager(long checkPeriod, long defaultExpiredTime, TimeUnit timeUnit) {
        super(checkPeriod, timeUnit);
        this.defaultExpiredMillis = timeUnit.toMillis(defaultExpiredTime);
        this.writeList = new SimpleLinkList<>();
        this.readList = new SimpleLinkList<>();
    }

    @Override
    public boolean put(R resource) {
        checkStatus();
        if (resource == null) {
            throw new IllegalArgumentException();
        }
        ResourceWithExpired<R> rwe = new ResourceWithExpired<>(resource, getExpiredTime());
        writeList.push(rwe);
        return true;
    }

    @Override
    public R poll() {
        checkStatus();
        SimpleLinkList<ResourceWithExpired<R>> readList = this.readList;
        try {
            ResourceWithExpired<R> poll = tryPoll(readList);
            if (poll == null && tryReverse(readList)) {
                poll = tryPoll(this.readList);
            }
            if (poll != null) {
                return poll.getResource();
            }
        } catch (ToolboxRuntimeException e) {
            // do nothing
        }
        return null;
    }

    private ResourceWithExpired<R> tryPoll(SimpleLinkList<ResourceWithExpired<R>> linkList) {
        if (status.get() == REVERSING_STATUS) {
            return null;
        }
        ResourceWithExpired<R> poll;
        while ((poll = linkList.poll()) != null) {
            if (!poll.isPolled()) {
                break;
            }
        }
        return poll;
    }

    private boolean tryReverse(SimpleLinkList<ResourceWithExpired<R>> readList) {
        while (this.readList == readList) {
            SimpleLinkList<ResourceWithExpired<R>> writeList = this.writeList;
            if (writeList != readList) {
                if (writeList.head == null) {
                    return false;
                }
                if (status.get() != 0) {
                    Thread.yield();
                } else if (status.compareAndSet(NORMAL_STATUS, REVERSING_STATUS)) {
                    try {
                        this.writeList = readList;
                        this.readList = writeList;
                        break;
                    } finally {
                        status.compareAndSet(REVERSING_STATUS, NORMAL_STATUS);
                    }
                }
            }
        }
        // reverse success
        return true;
    }

    @Override
    public void clear() {
        if (status.get() == CLOSE_STATUS) {
            return;
        }
        int statusVal;
        while ((statusVal = status.get()) == NORMAL_STATUS || statusVal == REVERSING_STATUS) {
            if (status.compareAndSet(NORMAL_STATUS, CLEARING_STATUS)) {
                try {
                    long currentTimeMillis = System.currentTimeMillis();
                    clearExpiredNode(this.readList, currentTimeMillis);
                    clearExpiredNode(this.writeList, currentTimeMillis);
                } finally {
                    status.compareAndSet(CLEARING_STATUS, NORMAL_STATUS);
                }
                break;
            }
        }
    }

    @Override
    public void close() {
        status.set(CLOSE_STATUS);
        checkWorker.shutdownNow();
    }

    private void clearExpiredNode(SimpleLinkList<ResourceWithExpired<R>> linkList, long expiredTime) {
        VarHandle.acquireFence();
        SimpleLinkList.Node<ResourceWithExpired<R>> f;
        SimpleLinkList.Node<ResourceWithExpired<R>> n;
        ResourceWithExpired<R> item;
        out:
        while ((f = linkList.head) != null) {
            if ((item = f.item) != null) {
                if (item.getExpiredTime() <= expiredTime) {
                    if (SimpleLinkList.HEAD.compareAndSet(linkList, f, null)) {
                        SimpleLinkList.clearNodeLink(f);
                        break;
                    }
                } else {
                    for (n = f.next; n != null; f = n, n = f.next) {
                        if ((item = n.item) == null) {
                            break;
                        }
                        if (item.getExpiredTime() <= expiredTime) {
                            if (SimpleLinkList.NEXT_NODE.compareAndSet(f, n, null)) {
                                SimpleLinkList.clearNodeLink(n);
                                break out;
                            }
                            continue out;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void checkStatus() {
        if (status.get() == CLOSE_STATUS) {
            throw new ToolboxRuntimeException("Resource manager is close status");
        }
    }

    private long getExpiredTime() {
        return System.currentTimeMillis() + this.defaultExpiredMillis;
    }

    private static class SimpleLinkList<E> {

        private Node<E> head;
        private static VarHandle HEAD;
        private static VarHandle NEXT_NODE;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                HEAD = lookup.findVarHandle(SimpleLinkList.class, "head", Node.class);
                NEXT_NODE = lookup.findVarHandle(Node.class, "next", Node.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static class Node<E> {
            E item;
            Node<E> next;

            Node(E element, Node<E> next) {
                this.item = element;
                this.next = next;
            }
        }

        public void push(E e) {
            VarHandle.acquireFence();
            for (; ; ) {
                final Node<E> f = head;
                if (HEAD.compareAndSet(this, f, new Node<>(e, f))) {
                    return;
                }
            }
        }

        public E poll() {
            Node<E> f;
            VarHandle.acquireFence();
            while ((f = head) != null) {
                final Node<E> next = f.next;
                if (HEAD.compareAndSet(this, f, next)) {
                    E element = f.item;
                    f.item = null;
                    f.next = null;
                    return element;
                }
            }
            return null;
        }

        private static <E> void clearNodeLink(Node<E> node) {
            for (Node<E> cur = node; cur != null; ) {
                Node<E> next = cur.next;
                cur.item = null;
                cur.next = null;
                cur = next;
            }
        }

        public void clear() {
            for (Node<E> x = head; x != null; ) {
                Node<E> next = x.next;
                x.item = null;
                x.next = null;
                x = next;
            }
        }

    }
}
