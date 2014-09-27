package fx_clj.binding;

import clojure.lang.AFn;
import clojure.lang.IReactiveRef;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;

public class ReactiveRefObservable extends RefObservable {
    private final IReactiveRef ref;

    public ReactiveRefObservable(IReactiveRef ref) {
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
                        listener.invalidated(ReactiveRefObservable.this);
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
