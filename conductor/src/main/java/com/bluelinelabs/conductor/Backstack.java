package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

class Backstack implements Iterable<RouterTransaction> {

    private static final String KEY_ENTRIES = "Backstack.entries";

    private final Deque<RouterTransaction> backStack = new ArrayDeque<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return backStack.isEmpty();
    }

    public int size() {
        return backStack.size();
    }

    @Nullable
    public RouterTransaction root() {
        return backStack.size() > 0 ? backStack.getLast() : null;
    }

    @Override @NonNull
    public Iterator<RouterTransaction> iterator() {
        return backStack.iterator();
    }

    @NonNull
    public Iterator<RouterTransaction> reverseIterator() {
        return backStack.descendingIterator();
    }

    @NonNull
    public List<RouterTransaction> popTo(@NonNull RouterTransaction transaction) {
        List<RouterTransaction> popped = new ArrayList<>();
        if (backStack.contains(transaction)) {
            while (backStack.peek() != transaction) {
                RouterTransaction poppedTransaction = pop();
                popped.add(poppedTransaction);
            }
        } else {
            throw new RuntimeException("Tried to pop to a transaction that was not on the back stack");
        }
        return popped;
    }

    @NonNull
    public RouterTransaction pop() {
        RouterTransaction popped = backStack.pop();
        popped.controller.destroy();
        return popped;
    }

    @Nullable
    public RouterTransaction peek() {
        return backStack.peek();
    }

    public void remove(@NonNull RouterTransaction transaction) {
        backStack.removeFirstOccurrence(transaction);
    }

    public void push(@NonNull RouterTransaction transaction) {
        backStack.push(transaction);
    }

    @NonNull
    public List<RouterTransaction> popAll() {
        List<RouterTransaction> list = new ArrayList<>();
        while (!isEmpty()) {
            list.add(pop());
        }
        return list;
    }

    public void setBackstack(@NonNull List<RouterTransaction> backstack) {
        for (RouterTransaction existingTransaction : backStack) {
            boolean contains = false;
            for (RouterTransaction newTransaction : backstack) {
                if (existingTransaction.controller == newTransaction.controller) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                existingTransaction.controller.destroy();
            }
        }

        backStack.clear();
        for (RouterTransaction transaction : backstack) {
            backStack.push(transaction);
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        ArrayList<Bundle> entryBundles = new ArrayList<>(backStack.size());
        for (RouterTransaction entry : backStack) {
            entryBundles.add(entry.saveInstanceState());
        }

        outState.putParcelableArrayList(KEY_ENTRIES, entryBundles);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        ArrayList<Bundle> entryBundles = savedInstanceState.getParcelableArrayList(KEY_ENTRIES);
        if (entryBundles != null) {
            Collections.reverse(entryBundles);
            for (Bundle transactionBundle : entryBundles) {
                backStack.push(new RouterTransaction(transactionBundle));
            }
        }
    }
}
