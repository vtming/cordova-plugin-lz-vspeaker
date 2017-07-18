package com.longzheng.speaker;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * wyf
 */
public class VSpeaker extends CordovaPlugin {

    private String appid = "";
    private String mAuthId = "";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("register")) {
            if (args != null && args.length() != 0) {
                appid = args.getString(0);
                mAuthId = args.getString(1);
            }
            onRegister(callbackContext);
            return true;
        }
        if (action.equals("verify")) {
            if (args != null && args.length() != 0) {
                appid = args.getString(0);
                mAuthId = args.getString(1);
            }
            onVerify(callbackContext);
            return true;
        }
        return false;
    }

    private void onRegister(final CallbackContext callbackContext) {
        new IsvUtil().register(cordova.getActivity(), appid, mAuthId, new IsvUtil.OnReceived() {
            @Override
            public void onReceived(String json) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
//                callbackContext.success(json);
            }
        });
    }

    private void onVerify(final CallbackContext callbackContext) {
        new IsvUtil().verify(cordova.getActivity(), appid, mAuthId, new IsvUtil.OnReceived() {
            @Override
            public void onReceived(String json) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        });
    }

}
