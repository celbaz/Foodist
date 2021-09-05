package com.example.foodist.services

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class PermissionsServiceTest {

  @get:Rule
  val rule = MockitoJUnit.rule()

  private lateinit var service: PermissionsService

  @Mock
  private lateinit var fragment: Fragment

  @Mock
  private lateinit var resultLauncher: ActivityResultLauncher<Array<String>>

  private lateinit var app: Application
  private lateinit var shadowApp: ShadowApplication
  private lateinit var permissionCallback: ActivityResultCallback<Map<String, Boolean>>

  @Before
  fun setUp() {
    app = ApplicationProvider.getApplicationContext()
    shadowApp = shadowOf(app)

    `when`(fragment.requireContext()).thenReturn(app)
    `when`(
      fragment.registerForActivityResult(
        any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
        any<ActivityResultCallback<Map<String, Boolean>>>()
      )
    )
      .thenAnswer { invocation ->
        permissionCallback = invocation.getArgument(1)
        resultLauncher
      }

    service = PermissionsService(fragment)
  }

  @Test
  fun checkGrantedPermissions_successPreviouslyHadPermissions() {
    val callback = mock<OnCompleteCallback>()
    shadowApp.grantPermissions(ACCESS_FINE_LOCATION)

    service.checkGrantedPermissions(PermissionsRequest.LOCATION, callback)

    verify(callback).invoke(true)
  }

  @Test
  fun checkGrantedPermissions_successPermissionsAreGranted() {
    val callback = mock<OnCompleteCallback>()
    shadowApp.denyPermissions(ACCESS_FINE_LOCATION)
    `when`(fragment.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)).thenReturn(false)

    service.checkGrantedPermissions(PermissionsRequest.LOCATION, callback)
    permissionCallback.onActivityResult(mapOf(ACCESS_FINE_LOCATION to true))

    verify(resultLauncher).launch(PermissionsRequest.LOCATION.getPermissionsArray())
    verify(callback).invoke(true)
  }

  @Test
  fun checkGrantedPermissions_errorPermissionsAreNotGranted() {
    val callback = mock<OnCompleteCallback>()
    shadowApp.denyPermissions(ACCESS_FINE_LOCATION)
    `when`(fragment.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)).thenReturn(false)

    service.checkGrantedPermissions(PermissionsRequest.LOCATION, callback)
    permissionCallback.onActivityResult(mapOf(ACCESS_FINE_LOCATION to false))

    verify(resultLauncher).launch(PermissionsRequest.LOCATION.getPermissionsArray())
    verify(callback).invoke(false)
  }
}
