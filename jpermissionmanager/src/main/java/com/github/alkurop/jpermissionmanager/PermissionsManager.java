package com.github.alkurop.jpermissionmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by alkurop on 9/29/16.
 */

public class PermissionsManager implements PermissionHandler {
    private int requestCode = 666;
    private static final String TAG = PermissionsManager.class.getName();
    private HashMap<String, Boolean> mCheckResult = new HashMap<>();
    private HashMap<String, PermissionOptionalDetails> mPermissions = new HashMap<>();
    private HashSet<PermissionListener> mListeners = new HashSet<>();
    private Activity mActivity;
    private Fragment mFragment;
    private Context mContext;

    public static Map<String, Boolean> checkPermissions(Context context, List<String> permissions) {
        HashMap<String, Boolean> mCheckResult = new HashMap<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                mCheckResult.put(permission, false);
            } else {
                mCheckResult.put(permission, true);
            }
        }
        return mCheckResult;
    }

    public PermissionsManager(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity;
    }

    public PermissionsManager(Fragment fragment) {
        this.mFragment = fragment;
        this.mContext = fragment.getContext();
    }

    public void setRequestCode(int requestCode){
        this.requestCode = requestCode;
    }

    @Override
    public void clearPermissionsListeners() {
        mListeners.clear();
    }

    @Override
    public void addPermissionsListener(PermissionListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void addPermissions(Map<String, PermissionOptionalDetails> permissionsWithDetails) {
        for (Map.Entry<String, PermissionOptionalDetails> it : permissionsWithDetails.entrySet()) {
            mPermissions.put(it.getKey(), it.getValue());
        }
    }

    @Override
    public void clearPermissions() {
        mPermissions.clear();
    }

    @Override
    public void makePermissionRequest() {
        makePermissionRequest(true);
    }

    @Override
    public void makePermissionRequest(boolean shouldClearResults) {
        if (shouldClearResults) {
            mCheckResult.clear();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mPermissions.size() > 0) {
            executePermissionRequest();
        } else {
            for (Map.Entry<String, PermissionOptionalDetails> it : mPermissions.entrySet()) {
                mCheckResult.put(it.getKey(), true);
            }
            notifyListeners();
        }
    }

    private void executePermissionRequest() {
        HashSet<String> askPermissions = new HashSet<>();
        for (Map.Entry<String, PermissionOptionalDetails> it : mPermissions.entrySet()) {
            if (ContextCompat.checkSelfPermission(mContext, it.getKey())
                    != PackageManager.PERMISSION_GRANTED) {
                askPermissions.add(it.getKey());
            } else {
                mCheckResult.put(it.getKey(), true);

            }
        }
        if (askPermissions.size() > 0) {
            String[] permissionsArray = askPermissions.toArray(new String[askPermissions.size()]);

            if (mActivity != null) {
                ActivityCompat.requestPermissions(mActivity, permissionsArray, requestCode);
            } else if (mFragment.isAdded()) {
                mFragment.requestPermissions(permissionsArray, requestCode);
            }
        } else {
            notifyListeners();
        }

    }

    private void notifyListeners() {
        for (PermissionListener mListener : mListeners) {
            mListener.onPermissionResult(mCheckResult);
        }
        log();
    }

    private void startSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        final String packageName = mContext.getPackageName();

        intent.setData(Uri.fromParts("package", packageName, null));
        if (mActivity != null) {
            mActivity.startActivityForResult(intent, requestCode);
        } else {
            mFragment.startActivityForResult(intent, requestCode);
        }
    }

    private boolean shouldShowRequestPermissionExplanation(String permission) {
        if (mActivity != null) {
            return ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission);
        } else if (mFragment.isAdded()) {
            return mFragment.shouldShowRequestPermissionRationale(permission);
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == this.requestCode && permissions.length > 0 && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                mCheckResult.put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    PermissionOptionalDetails details = mPermissions.get(permissions[i]);
                    if (details != null) {
                        if (shouldShowRequestPermissionExplanation(permissions[i])) {
                            showExplanation(permissions[i], details);
                            return;
                        } else {
                            if (!details.optional) {
                                showExplanationPermissionRequired(details);
                                return;
                            }
                        }
                    }
                }
            }
            notifyListeners();
        }
    }

    private void showExplanation(final String permission, PermissionOptionalDetails details) {
        new AlertDialog.Builder(mContext).setTitle(details.title)
                .setMessage(details.message)
                .setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makePermissionRequest(false);
                    }
                })
                .setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mPermissions.get(permission) instanceof PermissionRequiredDetails) {
                        } else {
                            mPermissions.remove(permission);
                        }
                        makePermissionRequest(false);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showExplanationPermissionRequired(PermissionOptionalDetails details) {
        final String message;
        if (details instanceof PermissionRequiredDetails) {
            message = ((PermissionRequiredDetails) details).requiredMessage;
        } else {
            message = details.message;
        }
        new AlertDialog.Builder(mContext).setTitle(details.title)
                .setMessage(message)
                .setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSettings();
                    }
                })
                .setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode) {
        if (requestCode == this.requestCode) {
            makePermissionRequest(false);
        }
    }

    private void log() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Boolean> it : mCheckResult.entrySet()) {
            stringBuilder.append("permission " + it.getKey() + " " + it.getValue() + "\n");
        }
        Log.d(TAG, stringBuilder.toString());
    }

    public interface PermissionListener {
        void onPermissionResult(HashMap<String, Boolean> result);
    }
}
