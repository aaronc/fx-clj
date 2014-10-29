package fx_clj.binding;

import freactive.IReactiveAtom;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

import java.lang.ref.WeakReference;

public class ReactiveAtomObservable extends ReactiveRefObservable implements Property<Object> {
    private final IReactiveAtom atom;
    private ObservableValue<?> other;
    private InvalidationListener listener;

    public ReactiveAtomObservable(IReactiveAtom atom) {
        super(atom);
        this.atom = atom;
    }

    @Override
    public void setValue(Object value) {
        atom.reset(value);
    }

    @Override
    public void bind(ObservableValue<?> other) {
        if (other == null) {
            throw new NullPointerException("Cannot bind to null");
        }

        if (!other.equals(this.other)) {
            unbind();
            this.other = other;
            if (listener == null) {
                listener = new Listener(this);
            }
            other.addListener(listener);
            markInvalid();
        }
    }

    @Override
    public void unbind() {
        if(other != null) {
            other.removeListener(listener);
            other = null;
        }
    }

    @Override
    public boolean isBound() {
        return other != null;
    }

    @Override
    public void bindBidirectional(Property<Object> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<Object> other) {
        Bindings.unbindBidirectional(this, other);
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    private static class Listener implements InvalidationListener {

        private final WeakReference<ReactiveAtomObservable> wref;

        public Listener(ReactiveAtomObservable ref) {
            this.wref = new WeakReference<ReactiveAtomObservable>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ReactiveAtomObservable ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.markInvalid();
            }
        }
    }

    private void markInvalid() {
        setValue(other.getValue());
    }
}
