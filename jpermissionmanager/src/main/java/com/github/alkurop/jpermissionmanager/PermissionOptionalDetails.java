package com.github.alkurop.jpermissionmanager;

/**
 * Created by alkurop on 9/29/16.
 */

public class PermissionOptionalDetails {
    public final String title;
    public final String message;
    public final boolean optional;

    public PermissionOptionalDetails (String title, String message, boolean optional) {
        this.title = title;
        this.message = message;
        this.optional = optional;
    }

    public PermissionOptionalDetails (String title, String message) {
        this.message = message;
        this.title = title;
        this.optional = true;
    }
}
