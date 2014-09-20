package fx_clj.binding;

import clojure.lang.AFn;
import clojure.lang.IInvalidates;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ObservableInvalidates implements ObservableValue<Object> {
    private final IInvalidates ref;

    public ObservableInvalidates(IInvalidates ref) {
        this.ref = ref;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        ref.addInvalidationWatch(listener, new AFn() {
            @Override
            public Object invoke(Object key, Object ref) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.invalidated(ObservableInvalidates.this);
                    }
                });
                return null;
            }
        });
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        ref.removeInvalidationWatch(listener);
    }

    @Override
    public void addListener(ChangeListener<? super Object> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(ChangeListener<? super Object> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
        return ref.deref();
    }
}
