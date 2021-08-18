package com.example.foodist.services

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.foodist.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * The **PermissionsService** is called in order to attempt to ascertain permissions
 * required by a feature.
 *
 * To use you must call the method **checkGrantedPermissions** with the following parameters:
 *    - permission: PermissionRequest
 *    - onComplete: (success: Boolean) -> Void
 */

typealias OnCompleteCallback = (success: Boolean) -> Unit

class PermissionsService constructor(private val fragment: Fragment) {
  private var onPermissionRequestComplete: OnCompleteCallback? = null
  private val registerForActivity = fragment.registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { result ->
    val isGranted = result.values.all { granted -> granted == true }
    onPermissionRequestComplete?.let { it(isGranted) }
    onPermissionRequestComplete = null
  }

  fun checkGrantedPermissions(permission: PermissionsRequest, onComplete: (success: Boolean) -> Unit) {
    if (arePermissionsGranted(permission)) {
      onComplete(true)
    } else {
      onPermissionRequestComplete = onComplete
      requestPermissionsReady(permission)
    }
  }

  private fun arePermissionsGranted(permission: PermissionsRequest): Boolean {
    val areAllPermissionsGranted = permission.getPermissionsArray().all {
      ActivityCompat.checkSelfPermission(
        fragment.requireContext(), it
      ) == PackageManager.PERMISSION_GRANTED
    }

    return areAllPermissionsGranted
  }

  private fun shouldShowPermissionRationaleDialog(permission: PermissionsRequest): Boolean {
    return permission.getPermissionsArray().any { permissionName ->
      fragment.shouldShowRequestPermissionRationale(permissionName)
    }
  }

  private fun showPermissionRationaleDialog(permission: PermissionsRequest) {
    val copy = getCopy(permission)
    MaterialAlertDialogBuilder(fragment.requireContext(), R.style.AppTheme_AlertDialog)
      .setTitle(copy.title)
      .setMessage(copy.message)
      .setCancelable(false)
      .setPositiveButton(copy.action) { _, _ ->
        requestPermission(permission)
      }
      .show()
  }

  private fun requestPermissionsReady(permission: PermissionsRequest) {
    if (shouldShowPermissionRationaleDialog(permission)) {
      showPermissionRationaleDialog(permission)
    } else {
      requestPermission(permission)
    }

  }

  private fun requestPermission(permission: PermissionsRequest) {
    val permissionsArray = permission.getPermissionsArray()
    registerForActivity.launch(permissionsArray)
  }

  private fun getCopy(permission: PermissionsRequest): PermissionsCopy {
    return when (permission) {
      PermissionsRequest.LOCATION -> PermissionsCopy(
        R.string.location_permission_title,
        R.string.location_permission_message,
      )
    }
  }
}


data class PermissionsCopy(
  @StringRes val title: Int,
  @StringRes val message: Int,
  @StringRes val action: Int = R.string.dialog_continue,
  @StringRes val cancel: Int = R.string.dialog_cancel,
)

enum class PermissionsRequest {
  LOCATION;

  fun getPermissionsArray(): Array<String> {
    return when (this) {
      LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }
}