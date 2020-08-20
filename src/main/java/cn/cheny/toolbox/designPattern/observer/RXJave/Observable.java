package cn.cheny.toolbox.designPattern.observer.RXJave;

/**
 * 订阅源
 *
 * @param <T>
 */
public class Observable<T> {

    private OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public void subscribe(AbstractSubscriber<T> subscriber) {
        try {
            subscriber.onStart();
            onSubscribe.call(subscriber);
            subscriber.onCompleted();
        } catch (Exception e) {
            subscriber.onError(e);
        }
    }

    /**
     * 命令模式 命令subscriber执行
     *
     * @param <T>
     */
    public interface OnSubscribe<T> {
        void call(AbstractSubscriber<T> subscriber);
    }

    /**
     * 转换流
     *
     * @param transformer
     * @param <R>
     * @return
     */
    public <R> Observable<R> map(Transformer<T, R> transformer) {
        return new Observable<>(MapOnSubscribe.create(this.onSubscribe, transformer));
    }

    /**
     * 转换器
     *
     * @param <T>
     * @param <R>
     */
    public interface Transformer<T, R> {
        R transformer(T from);
    }

    /**
     * 转换OnSubscribe实现类
     *
     * @param <T>
     * @param <R>
     */
    private static class MapOnSubscribe<T, R> implements OnSubscribe<R> {

        private OnSubscribe<T> onSubscribe;

        private Transformer<T, R> transformer;

        private MapOnSubscribe(OnSubscribe<T> onSubscribe, Transformer<T, R> transformer) {
            this.onSubscribe = onSubscribe;
            this.transformer = transformer;
        }

        private static <T, R> MapOnSubscribe<T, R> create(OnSubscribe<T> onSubscribe, Transformer<T, R> transformer) {
            return new MapOnSubscribe<>(onSubscribe, transformer);
        }

        @Override
        public void call(AbstractSubscriber<R> subscriber) {
            onSubscribe.call(new AbstractSubscriber<T>() {

                @Override
                public void onError(Throwable t) {
                    subscriber.onError(t);
                }

                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onStart() {
                    subscriber.onStart();
                }

                @Override
                public void onExecute(T var1) {
                    subscriber.onExecute(transformer.transformer(var1));
                }

            });
        }

    }
}
