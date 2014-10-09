package fx_clj.binding;

import clojure.lang.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ObservableValueRef<T> implements IReactiveRef {
    private final ObservableValue<T> value;

    private volatile IPersistentMap invalidationWatches = PersistentHashMap.EMPTY;

    private volatile IPersistentMap watches = PersistentHashMap.EMPTY;

    private static class WeakInvalidationListenerWrapper {
        private WeakInvalidationListenerWrapper(InvalidationListener listener, WeakInvalidationListener weakListener) {
            this.listener = listener;
            this.weakListener = weakListener;
        }

        InvalidationListener listener;
        WeakInvalidationListener weakListener;
    }

    public ObservableValueRef(ObservableValue<T> value) {
        this.value = value;
    }

    @Override
    public clojure.lang.IInvalidates addInvalidationWatch(Object key, IFn callback) {
        InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                callback.invoke(key, ObservableValueRef.this);
            }
        };
        WeakInvalidationListener weakListener = new WeakInvalidationListener(listener);
        WeakInvalidationListenerWrapper wrapper = new WeakInvalidationListenerWrapper(listener, weakListener);
        invalidationWatches = invalidationWatches.assoc(key, wrapper);
        value.addListener(weakListener);
        return this;
    }

    @Override
    public clojure.lang.IInvalidates removeInvalidationWatch(Object key) {
        Object wrapper = invalidationWatches.valAt(key);
        if(wrapper != null) {
            value.removeListener(((WeakInvalidationListenerWrapper)wrapper).weakListener);
            invalidationWatches = invalidationWatches.without(key);
        }
        return this;
    }

    //@Override
    public IPersistentMap getInvalidationWatches() {
        return invalidationWatches;
    }

    @Override
    public void setValidator(IFn vf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFn getValidator() {
        return null;
    }

    @Override
    public IPersistentMap getWatches() {
        return watches;
    }

    private static class WeakChangeListenerWrapper<T> {
        private WeakChangeListenerWrapper(ChangeListener<? super T> listener, WeakChangeListener<? super T> weakListener) {
            this.listener = listener;
            this.weakListener = weakListener;
        }

        private ChangeListener<? super T> listener;
        private WeakChangeListener<? super T> weakListener;
    }

    @Override
    public IRef addWatch(Object key, IFn callback) {
        ChangeListener<? super T> changeListener = new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                callback.invoke(key, ObservableValueRef.this, oldValue, newValue);
            }
        };
        WeakChangeListener<? super T> weakChangeListener = new WeakChangeListener<>(changeListener);
        WeakChangeListenerWrapper<T> wrapper = new WeakChangeListenerWrapper<>(changeListener, weakChangeListener);
        watches = watches.assoc(key, wrapper);
        value.addListener(weakChangeListener);
        return this;
    }

    @Override
    public IRef removeWatch(Object key) {
        Object wrapper = watches.valAt(key);
        if(wrapper != null) {
            value.removeListener(((WeakChangeListenerWrapper<T>)wrapper).weakListener);
            watches = watches.without(key);
        }
        return this;
    }

    @Override
    public Object deref() {
        Reactive.registerDep(this);
        if(Platform.isFxApplicationThread())
            return value.getValue();
        else {
            CompletableFuture<T> future = new CompletableFuture<>();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    future.complete(value.getValue());
                }
            });
            try {
                return future.get();
            } catch (InterruptedException e) {
                throw new UndeclaredThrowableException(e);
            } catch (ExecutionException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}
