package com.bluelinelabs.conductor;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import com.bluelinelabs.conductor.Controller.LifecycleState;
import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.AttachFakingFrameLayout;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ControllerLifecycleStateTests {

    private Router router;

    private ActivityProxy activityProxy;

    public void createActivityController(Bundle savedInstanceState, boolean includeStartAndResume) {
        activityProxy = new ActivityProxy().create(savedInstanceState);

        if (includeStartAndResume) {
            activityProxy.start().resume();
        }

        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
    }

    @Before
    public void setup() {
        createActivityController(null, true);
    }

    @Test
    public void testNormalLifecycle() {
        TestController controller = new TestController();
        assertEquals(LifecycleState.INITIALIZED, controller.getCurrentState());

        activityProxy.getView().setAttached(false);

        router.pushController(RouterTransaction.with(controller));

        assertEquals(LifecycleState.INITIALIZED_WITH_CONTEXT, controller.getCurrentState());

        activityProxy.getView().setAttached(true);
        assertEquals(LifecycleState.ATTACHED, controller.getCurrentState());

        router.popController(controller);
        assertEquals(LifecycleState.DESTROYED, controller.getCurrentState());
    }

    @Test
    public void testLifecycleWithActivityStop() {
        TestController controller = new TestController();

        router.pushController(RouterTransaction.with(controller));

        activityProxy.getActivity().isDestroying = true;
        activityProxy.pause();
        assertEquals(LifecycleState.ATTACHED, controller.getCurrentState());

        activityProxy.stop(true);
        assertEquals(LifecycleState.INITIALIZED_WITH_CONTEXT, controller.getCurrentState());
    }

    @Test
    public void testLifecycleWithActivityConfigurationChange() {
        final String tag = "tag";
        final LifecycleHandler lifecycleHandler = LifecycleHandler.install(activityProxy.getActivity());
        TestController controller = new TestController();

        router.pushController(RouterTransaction.with(controller).tag(tag));

        activityProxy.getActivity().getApplication().registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            Router currentRouter = router;

            private Controller getController() {
                return currentRouter.getControllerWithTag(tag);
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                assertEquals(LifecycleState.INITIALIZED, getController().getCurrentState());

                currentRouter = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);

                assertEquals(LifecycleState.ATTACHED, getController().getCurrentState());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                assertEquals(LifecycleState.ATTACHED, getController().getCurrentState());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                assertEquals(LifecycleState.ATTACHED, getController().getCurrentState());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                assertEquals(LifecycleState.ATTACHED, getController().getCurrentState());
            }

            @Override
            public void onActivityStopped(Activity activity) {
                assertEquals(LifecycleState.INITIALIZED_WITH_CONTEXT, getController().getCurrentState());
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

            @Override
            public void onActivityDestroyed(Activity activity) {
                lifecycleHandler.onDetach();
                assertEquals(LifecycleState.INITIALIZED, getController().getCurrentState());
            }
        });

        activityProxy.rotate();
    }

    @Test
    public void testLifecycleWithActivityBackground() {
        TestController controller = new TestController();

        router.pushController(RouterTransaction.with(controller));

        activityProxy.pause();

        assertEquals(LifecycleState.ATTACHED, controller.getCurrentState());

        activityProxy.resume();

        assertEquals(LifecycleState.ATTACHED, controller.getCurrentState());
    }

    @Test
    public void testChildLifecycle() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent));

        TestController child = new TestController();
        assertEquals(LifecycleState.INITIALIZED, child.getCurrentState());

        AttachFakingFrameLayout childContainer = (AttachFakingFrameLayout)parent.getView().findViewById(TestController.VIEW_ID);
        childContainer.setAttached(false);

        Router childRouter = parent.getChildRouter(childContainer);
        childRouter.setRoot(RouterTransaction.with(child));

        assertEquals(LifecycleState.INITIALIZED_WITH_CONTEXT, child.getCurrentState());

        childContainer.setAttached(true);

        assertEquals(LifecycleState.ATTACHED, child.getCurrentState());

        parent.removeChildRouter(childRouter);

        assertEquals(LifecycleState.DESTROYED, child.getCurrentState());
    }

    @Test
    public void testChildLifecycle2() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent));

        TestController child = new TestController();

        AttachFakingFrameLayout childContainer = (AttachFakingFrameLayout)parent.getView().findViewById(TestController.VIEW_ID);
        Router childRouter = parent.getChildRouter(childContainer);
        childRouter.setRoot(RouterTransaction.with(child));

        assertEquals(LifecycleState.ATTACHED, child.getCurrentState());

        router.popCurrentController();
        assertEquals(LifecycleState.DESTROYED, child.getCurrentState());
    }

}
