package payfun.lib.net.rx;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.disposables.ListCompositeDisposable;
import io.reactivex.rxjava3.internal.util.EndConsumerHelper;


/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/19 9:23
 * desc   : <p>Rx订阅回调封装
 */
public abstract class BaseObserver<T> implements Observer<T>, Disposable {
    /**
     * The active subscription.
     */
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /**
     * The resource composite, can never be null.
     */
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    /**
     * Adds a resource to this {@code ResourceObserver}.
     *
     * @param resource the resource to add
     * @throws NullPointerException if resource is {@code null}
     */
    public final void add(@NonNull Disposable resource) {
        Objects.requireNonNull(resource, "resource is null");
        resources.add(resource);
    }

    @Override
    public final void onSubscribe(Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    /**
     * Called once the upstream sets a {@link Disposable} on this {@code ResourceObserver}.
     *
     * <p>You can perform initialization at this moment. The default
     * implementation does nothing.
     */
    protected void onStart() {
    }

    /**
     * Cancels the main disposable (if any) and disposes the resources associated with
     * this {@code ResourceObserver} (if any).
     *
     * <p>This method can be called before the upstream calls {@link #onSubscribe(Disposable)} at which
     * case the main {@link Disposable} will be immediately disposed.
     */
    @Override
    public final void dispose() {
        if (DisposableHelper.dispose(upstream)) {
            resources.dispose();
        }
    }

    @Override
    public void onComplete() {

    }

    /**
     * Returns true if this {@code ResourceObserver} has been disposed/cancelled.
     *
     * @return true if this {@code ResourceObserver} has been disposed/cancelled
     */
    @Override
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(upstream.get());
    }

}
