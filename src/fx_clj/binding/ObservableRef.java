package fx_clj.binding;

import clojure.lang.AFn;
import clojure.lang.IRef;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ObservableRef implements ObservableValue<Object> {
    private final IRef ref;

    public ObservableRef(IRef ref) {
        this.ref = ref;
    }

    @Override
    public void addListener(ChangeListener<? super Object> listener) {
        ref.addWatch(listener, new AFn() {
            @Override
            public Object invoke(final Object key, final Object ref, final Object oldV, final Object newV) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.changed(ObservableRef.this, oldV, newV);
                    }
                });
                return null;
            }
        });
    }

    @Override
    public void removeListener(ChangeListener<? super Object> listener) {
        ref.removeWatch(listener);
    }

    @Override
    public Object getValue() {
        return ref.deref();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        ref.addWatch(listener, new AFn() {
            @Override
            public Object invoke(final Object key, final Object ref) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.invalidated(ObservableRef.this);
                    }
                });
                return null;
            }
        });
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        ref.removeWatch(listener);
    }
}
