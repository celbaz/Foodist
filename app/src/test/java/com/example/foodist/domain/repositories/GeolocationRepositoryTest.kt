package com.example.foodist.domain.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.foodist.data.network.geolocation.GoogleMapsApiService
import com.example.foodist.data.network.geolocation.model.GeoLocation
import com.example.foodist.data.network.geolocation.model.GeolocationRequest
import com.example.foodist.data.network.geolocation.model.GeolocationResponse
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Telephony
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class GeolocationRepositoryTest {

  @InjectMocks
  private lateinit var repository: GeolocationRepository

  @Mock
  private lateinit var apiServiceMock: GoogleMapsApiService

  @Mock
  private lateinit var telephonyMock: Telephony

  private val mainThreadSurrogate = TestCoroutineDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(mainThreadSurrogate)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Test
  fun fetchLocation_success() {
    val latLng = LatLng(1.0, 1.0)
    val radioType = "gsm"
    val response = Response.success(GeolocationResponse(GeoLocation(latLng.latitude, latLng.longitude), 1))

    runBlocking {
      `when`(apiServiceMock.fetchGeolocation(GeolocationRequest(radioType, true))).thenReturn(response)
      `when`(telephonyMock.getTelephonyRadioType()).thenReturn(radioType)

      assertEquals(ResultWrapper.Success(latLng), repository.fetchLocation())
    }
  }

  @Test
  fun fetchLocation_error() {
    runBlocking {
      val radioType = "gsm"
      `when`(
        apiServiceMock.fetchGeolocation(GeolocationRequest(radioType, true))
      ).thenReturn(Response.error(500, "error".toResponseBody("plain/text".toMediaTypeOrNull())))
      `when`(telephonyMock.getTelephonyRadioType()).thenReturn(radioType)

      val result = repository.fetchLocation()

      assertEquals(ResultWrapper.GenericError(500), result)
    }
  }

  @Test
  fun fetchLocation_errorExceptionThrow() {
    runBlocking {
      val radioType = "gsm"
      `when`(
        apiServiceMock.fetchGeolocation(GeolocationRequest(radioType, true))
      ).thenThrow(IllegalArgumentException())
      `when`(telephonyMock.getTelephonyRadioType()).thenReturn(radioType)

      val result = repository.fetchLocation()

      assertEquals(ResultWrapper.GenericError(), result)
    }
  }
}