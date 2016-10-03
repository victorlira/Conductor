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
public class TargetControllerTests {

    private Router router;

    public void createActivityController(Bundle savedInstanceState) {
        ActivityProxy activityProxy = new ActivityProxy().create(savedInstanceState).start().resume();
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
    public void testSiblingTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        Assert.assertNull(controllerA.getTargetController());
        Assert.assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        controllerB.setTargetController(controllerA);

        router.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertNull(controllerA.getTargetController());
        Assert.assertEquals(controllerA, controllerB.getTargetController());
    }

    @Test
    public void testParentChildTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        Assert.assertNull(controllerA.getTargetController());
        Assert.assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        controllerB.setTargetController(controllerA);

        Router childRouter = controllerA.getChildRouter((ViewGroup)controllerA.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertNull(controllerA.getTargetController());
        Assert.assertEquals(controllerA, controllerB.getTargetController());
    }

    @Test
    public void testChildParentTarget() {
        final TestController controllerA = new TestController();
        final TestController controllerB = new TestController();

        Assert.assertNull(controllerA.getTargetController());
        Assert.assertNull(controllerB.getTargetController());

        router.pushController(RouterTransaction.with(controllerA)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        controllerA.setTargetController(controllerB);

        Router childRouter = controllerA.getChildRouter((ViewGroup)controllerA.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.pushController(RouterTransaction.with(controllerB)
                .pushChangeHandler(new MockChangeHandler())
                .popChangeHandler(new MockChangeHandler()));

        Assert.assertNull(controllerB.getTargetController());
        Assert.assertEquals(controllerB, controllerA.getTargetController());
    }

}
