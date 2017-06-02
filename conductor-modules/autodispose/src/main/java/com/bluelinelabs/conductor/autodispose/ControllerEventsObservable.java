package com.bluelinelabs.conductor.autodispose;

import android.support.annotation.NonNull;
import android.view.View;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.uber.autodispose.OutsideLifecycleException;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

final class ControllerEventsObservable extends Observable<ControllerEvent> {
    @NonNull private final Controller controller;

    ControllerEventsObservable(@NonNull Controller controller) {
        this.controller = controller;
    }

    @Override
    protected void subscribeActual(Observer<? super ControllerEvent> observer) {
        if (controller.isBeingDestroyed() || controller.isDestroyed()) {
            throw new OutsideLifecycleException("Cannot bind to Controller lifecycle when outside of it.");
        } else if (controller.isAttached()) {
            observer.onNext(ControllerEvent.ATTACH);
        } else if (controller.getView() != null) {
            observer.onNext(ControllerEvent.CREATE_VIEW);
        } else {
            observer.onNext(ControllerEvent.CREATE);
        }

        Listener listener = new Listener(controller, observer);
        observer.onSubscribe(listener);
        controller.addLifecycleListener(listener.getLifecycleListener());
    }

    private static final class Listener implements Disposable {
        private final AtomicBoolean unsubscribed = new AtomicBoolean();
        private final Controller controller;
        private final Observer<? super ControllerEvent> observer;
        private final LifecycleListener lifecycleListener = new LifecycleListener() {
            @Override
            public void preCreateView(@NonNull Controller controller) {
                observer.onNext(ControllerEvent.CREATE_VIEW);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                observer.onNext(ControllerEvent.ATTACH);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                observer.onNext(ControllerEvent.DETACH);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                observer.onNext(ControllerEvent.DESTROY_VIEW);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                observer.onNext(ControllerEvent.DESTROY);
            }
        };

        Listener(Controller controller, final Observer<? super ControllerEvent> observer) {
            this.controller = controller;
            this.observer = observer;
        }

        public LifecycleListener getLifecycleListener() {
            return lifecycleListener;
        }

        @Override
        public final boolean isDisposed() {
            return unsubscribed.get();
        }

        @Override
        public void dispose() {
            if (unsubscribed.compareAndSet(false, true)) {
                controller.removeLifecycleListener(lifecycleListener);
            }
        }
    }
}