package com.marginallyclever.slavadukerani;

import java.util.EventListener;

/// FlagChangeListener is an interface for listening to flag count changes from SlavaDukerani.
public interface FlagChangeListener extends EventListener {
    void flagCountChanged(int flagCount);
}
