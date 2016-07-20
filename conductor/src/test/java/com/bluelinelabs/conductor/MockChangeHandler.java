package com.bluelinelabs.conductor;


import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class MockChangeHandler extends ControllerChangeHandler {

    static class ChangeHandlerListener {
        void willStartChange() { }
        void didAttachOrDetach() { }
        void didEndChange() { }
    }

    final ChangeHandlerListener listener;

    public MockChangeHandler() {
        this(new ChangeHandlerListener() { });
    }

    public MockChangeHandler(@NonNull ChangeHandlerListener listener) {
        this.listener = listener;
    }

    @Override
    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
        listener.willStartChange();

        if (isPush) {
            container.addView(to);
            ViewUtils.setAttached(to, true);

            listener.didAttachOrDetach();

            if (from != null) {
                container.removeView(from);
                ViewUtils.setAttached(from, false);
            }
        } else {
            container.removeView(from);
            ViewUtils.setAttached(from, false);

            listener.didAttachOrDetach();

            if (to != null) {
                container.addView(to);
                ViewUtils.setAttached(to, true);
            }

        }

        changeListener.onChangeCompleted();
        listener.didEndChange();
    }

}
