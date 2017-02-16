package com.bluelinelabs.conductor.demo.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.transitions.FabTransform;
import com.bluelinelabs.conductor.demo.util.AnimUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FabToDialogTransitionChangeHandler extends TransitionChangeHandler {

    private View fab;
    private ViewGroup fabParent;

    @NonNull @Override
    protected Transition getTransition(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, boolean isPush) {
        Transition backgroundFade = new Fade();
        backgroundFade.addTarget(R.id.dialog_background);

        Transition fabTransform = new FabTransform(ContextCompat.getColor(container.getContext(), R.color.colorAccent), R.drawable.ic_add_dark);

        TransitionSet set = new TransitionSet();
        set.addTransition(backgroundFade);
        set.addTransition(fabTransform);

        return set;
    }

    @Override
    public void prepareForTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @NonNull Transition transition, boolean isPush) {
        fab = isPush ? from.findViewById(R.id.fab) : to.findViewById(R.id.fab);
        fabParent = (ViewGroup)fab.getParent();

        if (!isPush) {
             /*
             * Before we transition back we want to remove the fab
             * in order to add it again for the TransitionManager to be able to detect the change
             */
            fabParent.removeView(fab);
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void executePropertyChanges(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @NonNull Transition transition, boolean isPush) {
        if (isPush) {
            fabParent.removeView(fab);
            container.addView(to);

            /*
             * After the transition is finished we have to add the fab back to the original container.
             * Because otherwise we will be lost when trying to transition back.
             * Set it to invisible because we don't want it to jump back after the transition
             */
            transition.addListener(new AnimUtils.TransitionListenerWrapper() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    fab.setVisibility(View.GONE);
                    fabParent.addView(fab);
                    fab = null;
                    fabParent = null;
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    fab.setVisibility(View.GONE);
                    fabParent.addView(fab);
                    fab = null;
                    fabParent = null;
                }
            });
        } else {
            fabParent.addView(fab);
            container.removeView(from);
        }
    }

}
