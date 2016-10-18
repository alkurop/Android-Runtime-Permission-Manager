package com.github.alkurop.jpermissionmanager;

/**
 * Created by alkurop on 9/29/16.
 */

public class PermissionRequiredDetails extends PermissionOptionalDetails {
    final String requiredMessage;

    public PermissionRequiredDetails(String title, String message, String requiredMessage) {
        super(message, title, false);
        this.requiredMessage = requiredMessage;
    }
}
