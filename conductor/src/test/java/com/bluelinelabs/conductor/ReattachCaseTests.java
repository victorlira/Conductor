package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.view.ViewGroup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReattachCaseTests {

    private ActivityProxy activityProxy;
    private Router router;

    public void createActivityController(Bundle savedInstanceState) {
        activityProxy = new ActivityProxy().create(savedInstanceState).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
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

        activityProxy.rotate();
        router.rebindIfNeeded();

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

        activityProxy.rotate();
        router.rebindIfNeeded();

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

        activityProxy.rotate();
        router.rebindIfNeeded();

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

        activityProxy.rotate();
        router.rebindIfNeeded();

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
        activityProxy.saveInstanceState(new Bundle()).pause();
        activityProxy.resume();
    }

}
