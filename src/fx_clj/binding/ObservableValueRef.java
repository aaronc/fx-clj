package fx_clj.binding;

import clojure.lang.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ObservableValueRef<T> implements IReactiveRef {
    private final ObservableValue<T> value;

    private volatile IPersistentMap invalidationWatches = PersistentHashMap.EMPTY;

    private volatile IPersistentMap watches = PersistentHashMap.EMPTY;

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
        invalidationWatches = invalidationWatches.assoc(key, listener);
        value.addListener(listener);
        return this;
    }

    @Override
    public clojure.lang.IInvalidates removeInvalidationWatch(Object key) {
        Object listener = invalidationWatches.valAt(key);
        if(listener != null) {
            value.removeListener((InvalidationListener)listener);
            invalidationWatches = invalidationWatches.without(key);
        }
        return this;
    }

    @Override
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

    @Override
    public IRef addWatch(Object key, IFn callback) {
        ChangeListener<? super T> changeListener = new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                callback.invoke(key, ObservableValueRef.this, oldValue, newValue);
            }
        };
        watches = watches.assoc(key, changeListener);
        value.addListener(changeListener);
        return this;
    }

    @Override
    public IRef removeWatch(Object key) {
        Object listener = watches.valAt(key);
        if(listener != null) {
            value.removeListener((ChangeListener<? super T>)listener);
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
