package com.example.falcato.wfdrouting;

import android.app.Application;

/**
 * Created by Falcato on 19/03/2017.
 */

public class MyApplication extends Application {
    private boolean hasNet;

    public boolean getHasNet() {
        return hasNet;
    }
    public void setHasNet(boolean hasNet) {
        this.hasNet = hasNet;
    }
}
