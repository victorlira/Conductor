package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;

import com.bluelinelabs.conductor.RestoreViewOnCreateController;

public abstract class LifecycleRestoreViewOnCreateController extends RestoreViewOnCreateController implements LifecycleRegistryOwner {

    private final ControllerLifecycleRegistryOwner lifecycleRegistryOwner = new ControllerLifecycleRegistryOwner(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistryOwner.getLifecycle();
    }

}
