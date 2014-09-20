package fx_clj.binding;

import clojure.lang.AFn;
import clojure.lang.IReactiveRef;
import clojure.lang.IRef;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;

public class ObservableReactiveRef extends ObservableRef {
    private final IReactiveRef ref;

    public ObservableReactiveRef(IReactiveRef ref) {
        super(ref);
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
                        listener.invalidated(ObservableReactiveRef.this);
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
}
