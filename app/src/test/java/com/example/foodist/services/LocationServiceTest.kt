package com.example.foodist.services

import android.Manifest
import android.app.Application
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import com.example.foodist.domain.repositories.GeolocationRepository
import com.example.foodist.utils.CountryList
import com.example.foodist.utils.ResultWrapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class LocationServiceTest {

  @get:Rule
  val rule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var geolocationRepository: GeolocationRepository

  @Mock
  private lateinit var locationProviderClient: FusedLocationProviderClient

  private lateinit var app: Application
  private lateinit var shadowApp: ShadowApplication
  private lateinit var service: LocationService

  @Before
  fun setUp() {
    app = ApplicationProvider.getApplicationContext()
    shadowApp = Shadows.shadowOf(app)
    service = LocationService(geolocationRepository, locationProviderClient, app)
  }

  @After
  fun tearDown() {
  }

  @Test
  fun fetchLocation_successFetchedFromLocationProvider() {
    val location = Location(LocationManager.GPS_PROVIDER)
    location.latitude = 30.0
    location.longitude = 40.0
    location.accuracy = 10.0f
    location.time = 1

    val task = Tasks.forResult(location)
    `when`(locationProviderClient.lastLocation).thenReturn(task)

    shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

    runBlocking {
      val result = service.fetchLocation()

      Assert.assertEquals(LatLng(location.latitude, location.longitude), result)
    }
  }

  @Test
  fun fetchLocation_successFetchedFromGeolocationRepository() {
    val latLng = LatLng(11.11, 22.22)

    runBlocking {
      `when`(geolocationRepository.fetchLocation()).thenReturn(ResultWrapper.Success(latLng))
      shadowApp.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

      Assert.assertEquals(latLng, service.fetchLocation())
    }
  }

  @Test
  fun fetchLocation_successFetchedFromSharedPreferences() {
    val latLng = LatLng(11.21, 22.22)

    runBlocking {
      service.setLocation(latLng)
      shadowApp.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

      Assert.assertEquals(latLng, service.fetchLocation())
    }
  }

  @Test
  @Config(qualifiers = "fr-rFR")
  fun fetchLocation_successFetchedFromUserLocale() {
    val country = CountryList["FR"]!!
    val centroid = LatLng(country.latitude, country.longitude)

    runBlocking {
      val result = service.fetchLocation()

      Assert.assertEquals(centroid, result)
    }
  }
}