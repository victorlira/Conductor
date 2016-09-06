package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

import java.util.ArrayList;
import java.util.List;

class ControllerHostedRouter extends Router {

    private final String KEY_HOST_ID = "ControllerHostedRouter.hostId";
    private final String KEY_TAG = "ControllerHostedRouter.tag";

    private Controller hostController;

    @IdRes private int hostId;
    private String tag;

    ControllerHostedRouter() { }

    ControllerHostedRouter(int hostId, String tag) {
        this.hostId = hostId;
        this.tag = tag;
    }

    final void setHost(@NonNull Controller controller, @NonNull ViewGroup container) {
        if (hostController != controller || this.container != container) {
            removeHost();

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            hostController = controller;
            this.container = container;
        }
    }

    final void removeHost() {
        if (container != null && container instanceof ControllerChangeListener) {
            removeChangeListener((ControllerChangeListener)container);
        }

        for (Controller controller : destroyingControllers) {
            if (controller.getView() != null) {
                controller.detach(controller.getView(), true);
            }
        }
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller.getView() != null) {
                transaction.controller.detach(transaction.controller.getView(), true);
            }
        }

        prepareForContainerRemoval();
        hostController = null;
        container = null;
    }

    final void setDetachFrozen(boolean frozen) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.setDetachFrozen(frozen);
        }
    }

    @Override
    void destroy() {
        setDetachFrozen(false);
        super.destroy();
    }

    @Override
    public Activity getActivity() {
        return hostController != null ? hostController.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);

        removeHost();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().invalidateOptionsMenu();
        }
    }

    @Override
    void startActivity(Intent intent) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivity(intent);
        }
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode);
        }
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode, options);
        }
    }

    @Override
    void registerForActivityResult(String instanceId, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().registerForActivityResult(instanceId, requestCode);
        }
    }

    @Override
    void unregisterForActivityResults(String instanceId) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().unregisterForActivityResults(instanceId);
        }
    }

    @Override
    void requestPermissions(String instanceId, String[] permissions, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().requestPermissions(instanceId, permissions, requestCode);
        }
    }

    @Override
    boolean hasHost() {
        return hostController != null;
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt(KEY_HOST_ID, hostId);
        outState.putString(KEY_TAG, tag);
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        hostId = savedInstanceState.getInt(KEY_HOST_ID);
        tag = savedInstanceState.getString(KEY_TAG);
    }

    @Override
    void setControllerRouter(Controller controller) {
        super.setControllerRouter(controller);
        controller.setParentController(hostController);
    }

    int getHostId() {
        return hostId;
    }

    String getTag() {
        return tag;
    }

    @Override
    List<Router> getSiblingRouters() {
        List<Router> list = new ArrayList<>();
        list.addAll(hostController.getChildRouters());
        list.addAll(hostController.getRouter().getSiblingRouters());
        return list;
    }

    @Override
    Router getRootRouter() {
        if (hostController != null && hostController.getRouter() != null) {
            return hostController.getRouter().getRootRouter();
        } else {
            return this;
        }
    }
}
