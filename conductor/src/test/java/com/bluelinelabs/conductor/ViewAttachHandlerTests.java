package com.bluelinelabs.conductor;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bluelinelabs.conductor.internal.ViewAttachHandler;
import com.bluelinelabs.conductor.internal.ViewAttachHandler.ViewAttachListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewAttachHandlerTests {

    private Activity activity;
    private ViewAttachHandler viewAttachHandler;
    private CountingViewAttachListener viewAttachListener;

    @Before
    public void setup() {
        activity = new ActivityProxy().create(null).getActivity();
        viewAttachListener = new CountingViewAttachListener();
        viewAttachHandler = new ViewAttachHandler(viewAttachListener);
    }

    @Test
    public void testSimpleViewAttachDetach() {
        View view = new View(activity);
        viewAttachHandler.listenForAttach(view);

        Assert.assertEquals(0, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(2, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);
    }

    @Test
    public void testSimpleViewGroupAttachDetach() {
        View view = new LinearLayout(activity);
        viewAttachHandler.listenForAttach(view);

        Assert.assertEquals(0, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true);
        Assert.assertEquals(2, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);
    }

    @Test
    public void testNestedViewGroupAttachDetach() {
        ViewGroup view = new LinearLayout(activity);
        View child = new LinearLayout(activity);
        view.addView(child);
        viewAttachHandler.listenForAttach(view);

        Assert.assertEquals(0, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true, false);
        Assert.assertEquals(0, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(child, true, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true, false);
        ViewUtils.reportAttached(child, true, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(0, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, false, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(view, true, false);
        Assert.assertEquals(1, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);

        ViewUtils.reportAttached(child, true, false);
        Assert.assertEquals(2, viewAttachListener.attaches);
        Assert.assertEquals(1, viewAttachListener.detaches);
    }

    private static class CountingViewAttachListener implements ViewAttachListener {
        int attaches;
        int detaches;

        @Override
        public void onAttached(View view) {
            attaches++;
        }

        @Override
        public void onDetached(View view) {
            detaches++;
        }
    }

}
