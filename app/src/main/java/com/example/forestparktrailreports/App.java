package com.example.forestparktrailreports;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    /**
     * This entire class is being used so I can get the context in a static method
     */
    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }
}
