package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;

import com.bluelinelabs.conductor.Controller;

public abstract class LifecycleController extends Controller implements LifecycleRegistryOwner {

    private final ControllerLifecycleRegistryOwner lifecycleRegistryOwner = new ControllerLifecycleRegistryOwner(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistryOwner.getLifecycle();
    }

}
