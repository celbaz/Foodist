package com.example.foodist.ui.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.foodist.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * The **PermissionsFragment** is a parent class that Fragments can inherit
 * in order to attempt to ascertain permissions required by a feature.
 *
 * To use you must call the method **checkGrantedPermissions** with the following parameters:
 *    - permission: PermissionRequest
 *    - onComplete: (success: Boolean) -> Void
 */
@AndroidEntryPoint
open class PermissionsFragment : Fragment() {
  private lateinit var onPermissionRequestComplete: (success: Boolean) -> Unit
  private val registerForActivity = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { result ->
    val isGranted = result.values.all { granted -> granted == true }
    onPermissionRequestComplete(isGranted)
  }

  fun checkGrantedPermissions(permission: PermissionRequest, onComplete: (success: Boolean) -> Unit) {
    onPermissionRequestComplete = onComplete

    if (arePermissionsGranted(permission)) {
      onPermissionRequestComplete(true)
    } else {
      requestPermissionsReady(permission)
    }
  }


  private fun arePermissionsGranted(permission: PermissionRequest): Boolean {
    val areAllPermissionsGranted = permission.getPermissionsArray().all {
      ActivityCompat.checkSelfPermission(
        requireContext(), it
      ) == PackageManager.PERMISSION_GRANTED
    }

    return areAllPermissionsGranted
  }

  private fun shouldShowPermissionRationaleDialog(permission: PermissionRequest): Boolean {
    return permission.getPermissionsArray().any { permissionName ->
      shouldShowRequestPermissionRationale(permissionName)
    }
  }

  private fun showPermissionRationaleDialog(permission: PermissionRequest) {
    val copy = getCopy(permission)
    MaterialAlertDialogBuilder(requireActivity(), R.style.AppTheme_AlertDialog)
      .setTitle(copy.title)
      .setMessage(copy.message)
      .setCancelable(false)
      .setPositiveButton(copy.action) { _, _ ->
        requestPermission(permission)
      }
      .show()
  }


  private fun requestPermissionsReady(permission: PermissionRequest) {
    if (shouldShowPermissionRationaleDialog(permission)) {
      showPermissionRationaleDialog(permission)
    } else {
      requestPermission(permission)
    }

  }

  private fun requestPermission(permission: PermissionRequest) {
    val permissionsArray = permission.getPermissionsArray()

    registerForActivity.launch(permissionsArray)

  }

  private fun getCopy(permission: PermissionRequest): PermissionCopy {
    return when (permission) {
      PermissionRequest.LOCATION -> PermissionCopy(
        R.string.location_permission_title,
        R.string.location_permission_message,
      )
    }

  }

  companion object {
    const val REQUEST_CODE = 1
  }
}


data class PermissionCopy(
  @StringRes val title: Int,
  @StringRes val message: Int,
  @StringRes val action: Int = R.string.dialog_continue,
  @StringRes val cancel: Int = R.string.dialog_cancel,
)

enum class PermissionRequest {
  LOCATION;

  fun getPermissionsArray(): Array<String> {
    return when (this) {
      LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }
}