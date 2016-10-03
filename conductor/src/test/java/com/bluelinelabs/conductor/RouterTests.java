package com.bluelinelabs.conductor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RouterTests {

    private Router router;

    @Before
    public void setup() {
        ActivityProxy activityProxy = new ActivityProxy().create(null).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), null);
    }

    @Test
    public void testSetRoot() {
        String rootTag = "root";

        Controller rootController = new TestController();

        Assert.assertFalse(router.hasRootController());

        router.setRoot(RouterTransaction.with(rootController).tag(rootTag));

        Assert.assertTrue(router.hasRootController());

        Assert.assertEquals(rootController, router.getControllerWithTag(rootTag));
    }

    @Test
    public void testSetNewRoot() {
        String oldRootTag = "oldRoot";
        String newRootTag = "newRoot";

        Controller oldRootController = new TestController();
        Controller newRootController = new TestController();

        router.setRoot(RouterTransaction.with(oldRootController).tag(oldRootTag));
        router.setRoot(RouterTransaction.with(newRootController).tag(newRootTag));

        Assert.assertNull(router.getControllerWithTag(oldRootTag));
        Assert.assertEquals(newRootController, router.getControllerWithTag(newRootTag));
    }

    @Test
    public void testGetByInstanceId() {
        Controller controller = new TestController();

        router.pushController(RouterTransaction.with(controller));

        Assert.assertEquals(controller, router.getControllerWithInstanceId(controller.getInstanceId()));
        Assert.assertNull(router.getControllerWithInstanceId("fake id"));
    }

    @Test
    public void testGetByTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPushPopControllers() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        Assert.assertEquals(1, router.getBackstackSize());

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(2, router.getBackstackSize());

        router.popCurrentController();

        Assert.assertEquals(1, router.getBackstackSize());

        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));

        router.popCurrentController();

        Assert.assertEquals(0, router.getBackstackSize());

        Assert.assertNull(router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPopToTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";
        String controller4Tag = "controller4";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();
        Controller controller4 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.pushController(RouterTransaction.with(controller4)
                .tag(controller4Tag));

        router.popToTag(controller2Tag);

        Assert.assertEquals(2, router.getBackstackSize());
        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag));
        Assert.assertNull(router.getControllerWithTag(controller3Tag));
        Assert.assertNull(router.getControllerWithTag(controller4Tag));
    }

    @Test
    public void testPopNonCurrent() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.popController(controller2);

        Assert.assertEquals(2, router.getBackstackSize());
        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));
        Assert.assertEquals(controller3, router.getControllerWithTag(controller3Tag));
    }

    @Test
    public void testSetBackstack() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(rootTransaction);
        backstack.add(middleTransaction);
        backstack.add(topTransaction);

        router.setBackstack(backstack, null);

        Assert.assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(middleTransaction, fetchedBackstack.get(1));
        Assert.assertEquals(topTransaction, fetchedBackstack.get(2));
    }

    @Test
    public void testNewSetBackstack() {
        router.setRoot(RouterTransaction.with(new TestController()));

        Assert.assertEquals(1, router.getBackstackSize());

        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(rootTransaction);
        backstack.add(middleTransaction);
        backstack.add(topTransaction);

        router.setBackstack(backstack, null);

        Assert.assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(middleTransaction, fetchedBackstack.get(1));
        Assert.assertEquals(topTransaction, fetchedBackstack.get(2));
    }

    @Test
    public void testNewSetBackstackWithNoRemoveViewOnPush() {
        RouterTransaction oldRootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction oldTopTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new MockChangeHandler(false));

        router.setRoot(oldRootTransaction);
        router.pushController(oldTopTransaction);
        Assert.assertEquals(2, router.getBackstackSize());

        Assert.assertTrue(oldRootTransaction.controller.isAttached());
        Assert.assertTrue(oldTopTransaction.controller.isAttached());

        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new MockChangeHandler(false));
        RouterTransaction topTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new MockChangeHandler(false));

        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(rootTransaction);
        backstack.add(middleTransaction);
        backstack.add(topTransaction);

        router.setBackstack(backstack, null);

        Assert.assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(middleTransaction, fetchedBackstack.get(1));
        Assert.assertEquals(topTransaction, fetchedBackstack.get(2));

        Assert.assertFalse(oldRootTransaction.controller.isAttached());
        Assert.assertFalse(oldTopTransaction.controller.isAttached());
        Assert.assertTrue(rootTransaction.controller.isAttached());
        Assert.assertTrue(middleTransaction.controller.isAttached());
        Assert.assertTrue(topTransaction.controller.isAttached());
    }

    @Test
    public void testReplaceTopController() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(rootTransaction);
        backstack.add(topTransaction);

        router.setBackstack(backstack, null);

        Assert.assertEquals(2, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(topTransaction, fetchedBackstack.get(1));

        RouterTransaction newTopTransaction = RouterTransaction.with(new TestController());
        router.replaceTopController(newTopTransaction);

        Assert.assertEquals(2, router.getBackstackSize());

        fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(newTopTransaction, fetchedBackstack.get(1));
    }

    @Test
    public void testReplaceTopControllerWithNoRemoveViewOnPush() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new MockChangeHandler(false));

        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(rootTransaction);
        backstack.add(topTransaction);

        router.setBackstack(backstack, null);

        Assert.assertEquals(2, router.getBackstackSize());

        Assert.assertTrue(rootTransaction.controller.isAttached());
        Assert.assertTrue(topTransaction.controller.isAttached());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(topTransaction, fetchedBackstack.get(1));

        RouterTransaction newTopTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new MockChangeHandler(false));
        router.replaceTopController(newTopTransaction);
        newTopTransaction.pushChangeHandler().completeImmediately();

        Assert.assertEquals(2, router.getBackstackSize());

        fetchedBackstack = router.getBackstack();
        Assert.assertEquals(rootTransaction, fetchedBackstack.get(0));
        Assert.assertEquals(newTopTransaction, fetchedBackstack.get(1));

        Assert.assertTrue(rootTransaction.controller.isAttached());
        Assert.assertFalse(topTransaction.controller.isAttached());
        Assert.assertTrue(newTopTransaction.controller.isAttached());
    }

}
