package com.bluelinelabs.conductor;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;

import org.robolectric.util.ReflectionHelpers;

import java.util.List;

public class ViewUtils {

    public static void setAttached(View view, boolean attached) {
        Object listenerInfo = ReflectionHelpers.callInstanceMethod(view, "getListenerInfo");
        List<OnAttachStateChangeListener> listeners = ReflectionHelpers.getField(listenerInfo, "mOnAttachStateChangeListeners");

        for (OnAttachStateChangeListener listener : listeners) {
            if (attached) {
                listener.onViewAttachedToWindow(view);
            } else {
                listener.onViewDetachedFromWindow(view);
            }
        }
    }

}
