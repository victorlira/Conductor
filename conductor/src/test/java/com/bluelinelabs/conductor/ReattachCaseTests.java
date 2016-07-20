package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReattachCaseTests {

    private ActivityController<TestActivity> activityController;
    private Router router;

    public void createActivityController(Bundle savedInstanceState) {
        activityController = Robolectric.buildActivity(TestActivity.class).create(savedInstanceState).start();

        @IdRes int containerId = 4;
        FrameLayout routerContainer = new FrameLayout(activityController.get());
        routerContainer.setId(containerId);

        router = Conductor.attachRouter(activityController.get(), routerContainer, savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null);
    }

    @Test
    public void testNeedsAttachingOnPauseAndOrientation() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        router.pushController(RouterTransaction.with(controllerA)
            .pushChangeHandler(new MockChangeHandler())
            .popChangeHandler(new MockChangeHandler()));

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());

        sleepWakeDevice();

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());

        router.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());

        rotateDevice();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
    }

    @Test
    public void testChildNeedsAttachOnPauseAndOrientation() {
        final TestController controllerA = new TestController();
        final TestController childController = new TestController();
        final TestController controllerB = new TestController();

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Router childRouter = controllerA.getChildRouter((ViewGroup)controllerA.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.pushController(RouterTransaction.with(childController)
            .pushChangeHandler(new MockChangeHandler())
            .popChangeHandler(new MockChangeHandler()));

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertTrue(childController.isAttached());
        Assert.assertFalse(controllerB.isAttached());

        sleepWakeDevice();

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertTrue(childController.isAttached());
        Assert.assertFalse(controllerB.isAttached());

        router.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertFalse(childController.isAttached());
        Assert.assertTrue(controllerB.isAttached());

        rotateDevice();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertFalse(childController.isAttached());
        Assert.assertTrue(childController.getNeedsAttach());
        Assert.assertTrue(controllerB.isAttached());
    }

    @Test
    public void testChildHandleBackOnOrientation() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();
        final TestController childController = new TestController();

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());

        router.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Router childRouter = controllerB.getChildRouter((ViewGroup)controllerB.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.setPopsLastView(true);
        childRouter.pushController(RouterTransaction.with(childController)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        rotateDevice();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        router.handleBack();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());

        router.handleBack();

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());
    }

    // Attempt to test https://github.com/bluelinelabs/Conductor/issues/86#issuecomment-231381271
    @Test
    public void testReusedChildRouterHandleBackOnOrientation() {
        TestController controllerA = new TestController();
        TestController controllerB = new TestController();
        TestController childController = new TestController();

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());

        router.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Router childRouter = controllerB.getChildRouter((ViewGroup)controllerB.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.setPopsLastView(true);
        childRouter.pushController(RouterTransaction.with(childController)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        router.handleBack();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());

        childController = new TestController();
        childRouter.pushController(RouterTransaction.with(childController)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        rotateDevice();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        router.handleBack();

        childController = new TestController();
        childRouter.pushController(RouterTransaction.with(childController)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertTrue(childController.isAttached());

        router.handleBack();

        Assert.assertFalse(controllerA.isAttached());
        Assert.assertTrue(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());

        router.handleBack();

        Assert.assertTrue(controllerA.isAttached());
        Assert.assertFalse(controllerB.isAttached());
        Assert.assertFalse(childController.isAttached());
    }

    private void sleepWakeDevice() {
        activityController.saveInstanceState(new Bundle()).pause();
        activityController.resume();
    }

    private void rotateDevice() {
        @IdRes int containerId = 4;
        FrameLayout routerContainer = new FrameLayout(activityController.get());
        routerContainer.setId(containerId);

        activityController.get().isChangingConfigurations = true;
        activityController.get().recreate();
        router.rebindIfNeeded();
    }

}
