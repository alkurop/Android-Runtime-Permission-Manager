package com.github.alkurop.jpermissionmanager;

import java.util.Map;

/**
 * Created by alkurop on 2/3/17.
 */

public interface PermissionHandler {
    void clearPermissionsListeners ();

    void addPermissionsListener (PermissionsManager.PermissionListener listener);

    void addPermissions (Map<String, PermissionOptionalDetails> permissionsWithDetails);

    void clearPermissions ();

    void makePermissionRequest ();

    void makePermissionRequest (boolean shouldClearResults);

    void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults);

    void onActivityResult (int requestCode);
}
