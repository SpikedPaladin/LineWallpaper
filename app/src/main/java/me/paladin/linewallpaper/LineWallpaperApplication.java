package me.paladin.linewallpaper;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LineWallpaperApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        setupPreferences();
    }
    
    private void setupPreferences() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.contains("lineWidth")) return;
        int mLineWidth = (int) (getResources().getDisplayMetrics().density * 1.5);
        if (mLineWidth < 1) mLineWidth = 1;
        float mMinStep = mLineWidth * 2;
        float mMaxStep = mMinStep * 3;
        pref.edit()
                .putString("lineWidth", "" + mLineWidth)
                .putString("minStep", "" + mMinStep)
                .putString("maxStep", "" + mMaxStep)
                .apply();
    }
}
