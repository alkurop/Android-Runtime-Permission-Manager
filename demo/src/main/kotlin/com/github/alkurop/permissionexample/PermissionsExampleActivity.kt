package com.github.alkurop.permissionexample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.alkurop.permissionmanager.*

class PermissionsExampleActivity : AppCompatActivity() {

    lateinit var permissionManager: PermissionsManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_activity_main)
        permissionManager = PermissionsManager(this)
        permissionManager.addPermissionsListener { }

        val permission1 = Pair(Manifest.permission.READ_CONTACTS, PermissionOptionalDetails(title = "Read Contacts",
                  message = "This permission is optional. I can live without it"))

        val permission2 = Pair(Manifest.permission.READ_PHONE_STATE, PermissionRequiredDetails(
                  title = "Read Phone State", message = "This permission is required. You have to turn it on",
                  requiredMessage = "I really need this permission. Go to settings and turn it on"))

        val permission3 = Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionRequiredDetails(title = "Write Storage",
                  message = "This permission is required. You have to turn it on",
                  requiredMessage = "I really need this permission. Go to settings and turn it on"))

        val permissionWithDetails = mapOf(permission1, permission2, permission3)
        permissionManager.addPermissions(permissionWithDetails)
            permissionManager.makePermissionRequest()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        permissionManager.onActivityResult(requestCode)
        super.onActivityResult(requestCode, resultCode, data)
    }
}