package cn.cheny.toolbox.asyncTask.poolmanager;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cheney
 * @date 2021-08-13
 */
public class ExpiredResourceManager<R> extends BaseResourceManager<R> {

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

    private final Lock writeLock = new ReentrantLock();

    private final Lock readLock = new ReentrantLock();

    private final AtomicInteger status = new AtomicInteger(0);

    public ExpiredResourceManager(long defaultExpiredTime, TimeUnit timeUnit) {
        this(DEFAULT_DELAY_SECONDS, defaultExpiredTime, timeUnit);
    }

    public ExpiredResourceManager(long checkPeriod, long defaultExpiredTime, TimeUnit timeUnit) {
        super(checkPeriod);
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
        boolean locked = false;
        try {
            if (locked = writeLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                writeList.push(rwe);
                return true;
            }
        } catch (InterruptedException interruptedException) {
            // do nothing
        } finally {
            if (locked) {
                writeLock.unlock();
            }
        }
        return false;
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
        boolean locked = false;
        try {
            if (locked = readLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                ResourceWithExpired<R> poll;
                while ((poll = linkList.poll()) != null) {
                    if (poll.isExpired()) {
                        // 补偿过期逻辑
                        linkList.clear();
                    }
                    if (!poll.isPolled()) {
                        break;
                    }
                }
                return poll;
            }
        } catch (InterruptedException interruptedException) {
            // do nothing
        } finally {
            if (locked) {
                readLock.unlock();
            }
        }
        throw new ToolboxRuntimeException();
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
        long currentTimeMillis = System.currentTimeMillis();
        SimpleLinkList<ResourceWithExpired<R>> readList = this.readList;
        SimpleLinkList<ResourceWithExpired<R>> writeList = this.writeList;
        SimpleLinkList.Node<ResourceWithExpired<R>> expiredStartNodeInRead = findExpiredStartNode(readList, currentTimeMillis);
        SimpleLinkList.Node<ResourceWithExpired<R>> expiredStartNodeInWrite = findExpiredStartNode(writeList, currentTimeMillis);
        if (expiredStartNodeInRead != null) {
            expiredStartNodeInRead.item.setStatus(ResourceWithExpired.EXPIRED);
            readList.clearAfterNode(expiredStartNodeInRead);
        }
        if (expiredStartNodeInWrite != null) {
            expiredStartNodeInWrite.item.setStatus(ResourceWithExpired.EXPIRED);
            writeList.clearAfterNode(expiredStartNodeInWrite);
        }
    }

    @Override
    public void close() {
        status.set(CLOSE_STATUS);
        checkWorker.shutdownNow();
    }

    private SimpleLinkList.Node<ResourceWithExpired<R>> findExpiredStartNode(SimpleLinkList<ResourceWithExpired<R>> linkList, long expiredTime) {
        SimpleLinkList.Node<ResourceWithExpired<R>> cur = linkList.head;
        while (cur != null) {
            if (cur.item.getExpiredTime() <= expiredTime) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
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

        private static class Node<E> {
            E item;
            Node<E> next;
            Node<E> prev;

            Node(Node<E> prev, E element, Node<E> next) {
                this.item = element;
                this.next = next;
                this.prev = prev;
            }
        }

        public void push(E e) {
            final Node<E> f = head;
            final Node<E> newNode = new Node<>(null, e, f);
            head = newNode;
            if (f != null) f.prev = newNode;
        }

        public E poll() {
            final Node<E> f = head;
            return (f == null) ? null : unlinkFirst(f);
        }

        public void clear() {
            for (Node<E> x = head; x != null; ) {
                Node<E> next = x.next;
                x.item = null;
                x.next = null;
                x.prev = null;
                x = next;
            }
        }

        public void clearAfterNode(Node<E> node) {
            Node<E> prev = node.prev;
            if (node == head) {
                head = null;
            } else if (prev != null) {
                prev.next = null;
            }
        }

        private E unlinkFirst(Node<E> f) {
            final E element = f.item;
            final Node<E> next = f.next;
            f.item = null;
            f.next = null;
            head = next;
            if (next != null) next.prev = null;
            return element;
        }

    }
}
