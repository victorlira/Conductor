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
public class TargetControllerTests {

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
