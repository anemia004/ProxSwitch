package com.anemia004.proxswitch.xposed;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ProxSwitchHook implements IXposedHookLoadPackage {

    private static final String STATE_FILE = "/data/local/proxswitch_state";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android")) return;

        XposedHelpers.findAndHookMethod(
            "android.hardware.SystemSensorManager",
            lpparam.classLoader,
            "registerListenerImpl",
            SensorEventListener.class,
            Sensor.class,
            int.class,
            android.os.Handler.class,
            int.class,
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isProxDisabled()) {
                        Sensor sensor = (Sensor) param.args[1];
                        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
                            param.setResult(null);
                        }
                    }
                }
            }
        );
    }

    private boolean isProxDisabled() {
        try {
            File f = new File(STATE_FILE);
            if (!f.exists()) return false;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            br.close();
            return "1".equals(line);
        } catch (Exception ignored) {
            return false;
        }
    }
}
