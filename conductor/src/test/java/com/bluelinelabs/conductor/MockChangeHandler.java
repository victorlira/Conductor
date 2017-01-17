package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class MockChangeHandler extends ControllerChangeHandler {

    private static final String KEY_REMOVES_FROM_VIEW_ON_PUSH = "MockChangeHandler.removesFromViewOnPush";

    static class ChangeHandlerListener {
        void willStartChange() { }
        void didAttachOrDetach() { }
        void didEndChange() { }
    }

    final ChangeHandlerListener listener;
    boolean removesFromViewOnPush;

    public static MockChangeHandler defaultHandler() {
        return new MockChangeHandler(true, null);
    }

    public static MockChangeHandler noRemoveViewOnPushHandler() {
        return new MockChangeHandler(false, null);
    }

    public static MockChangeHandler listeningChangeHandler(@NonNull ChangeHandlerListener listener) {
        return new MockChangeHandler(true , listener);
    }

    public MockChangeHandler() {
        listener = null;
    }

    private MockChangeHandler(boolean removesFromViewOnPush, ChangeHandlerListener listener) {
        this.removesFromViewOnPush = removesFromViewOnPush;

        if (listener == null) {
            this.listener = new ChangeHandlerListener() { };
        } else {
            this.listener = listener;
        }
    }

    @Override
    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
        listener.willStartChange();

        if (isPush) {
            container.addView(to);
            listener.didAttachOrDetach();

            if (removesFromViewOnPush && from != null) {
                container.removeView(from);
            }
        } else {
            container.removeView(from);
            listener.didAttachOrDetach();

            if (to != null) {
                container.addView(to);
            }

        }

        changeListener.onChangeCompleted();
        listener.didEndChange();
    }

    @Override
    public boolean removesFromViewOnPush() {
        return removesFromViewOnPush;
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);
        bundle.putBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH, removesFromViewOnPush);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);
        removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH);
    }

    @NonNull
    @Override
    public ControllerChangeHandler copy() {
        return new MockChangeHandler(removesFromViewOnPush, listener);
    }

    @Override
    public boolean isReusable() {
        return true;
    }
}
