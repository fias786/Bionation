package com.persistent.bionation.ui.camera;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs{

    public static int readSharedSetting(Context ctx, String settingName){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("badges",Context.MODE_PRIVATE);
        return sharedPreferences.getInt(settingName,0);
    }

    public static void saveSharedSetting(Context ctx,String settingName,int badgeValue){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("badges",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(settingName,badgeValue);
        editor.apply();
    }

}
