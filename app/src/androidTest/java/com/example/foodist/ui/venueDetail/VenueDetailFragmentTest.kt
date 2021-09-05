package com.example.foodist.ui.venueDetail

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.foodist.R
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.launchFragmentInHiltContainer
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class VenueDetailFragmentTest {
  @get:Rule
  var hiltRule = HiltAndroidRule(this)


  @BindValue
  @Mock
  lateinit var venueRepository: VenueRepository


  @Before
  fun setUp() {
    hiltRule.inject()

    venueRepository = mock(VenueRepository::class.java)
  }

  @Test
  fun detailViewFragment_errorNoDataReceived() {
    val fragmentArgs = bundleOf("venueId" to "testVenueId")

    launchFragmentInHiltContainer<VenueDetailFragment>(fragmentArgs) {
      Assert.assertNotNull(this)
    }

    onView(withId(R.id.businessDetailTitle)).check(matches(withText("")))
    onView(withId(R.id.rating)).check(matches(isDisplayed()))
    onView(withId(R.id.infoContainer)).check(matches(not(isDisplayed())))
  }

  @Test
  fun detailViewFragment_successVenueDataReceived() {
    val venue = Venue("123", "restaurant", Location("Nowhere", 0.0, 0.0, "", "", "", "", listOf()))
    val fragmentArgs = bundleOf("venueId" to "testVenueId")

    runBlocking {
      `when`(venueRepository.getVenueFromCache("testVenueId")).thenReturn(venue)
      `when`(venueRepository.getVenueDetails("testVenueId")).thenReturn(ResultWrapper.NetworkError)
    }

    launchFragmentInHiltContainer<VenueDetailFragment>(fragmentArgs)

    onView(withId(R.id.businessDetailTitle)).check(matches(withText(venue.name)))
    onView(withId(R.id.rating)).check(matches(isDisplayed()))
    onView(withId(R.id.infoContainer)).check(matches(not(isDisplayed())))
  }

  @Test
  fun detailViewFragment_successVenueDataAndVenueDetailsReceived() {
    val fragmentArgs = bundleOf("venueId" to "testVenueId")
    val venue = Venue("testVenueId", "restaurant", Location("Nowhere", 0.0, 0.0, "", "", "", "", listOf()))
    val venueDetails = VenueDetails(
      "restaurant",
      null,
      Location("Nowhere", 0.0, 0.0, "", "City", "State", null, listOf()),
      null,
      4.5,
      null,
      null
    )

    runBlocking {
      `when`(venueRepository.getVenueFromCache("testVenueId")).thenReturn(venue)
      `when`(venueRepository.getVenueDetails("testVenueId")).thenReturn(ResultWrapper.Success(venueDetails))
    }

    launchFragmentInHiltContainer<VenueDetailFragment>(fragmentArgs)

    onView(withId(R.id.businessDetailTitle)).check(matches(withText(venue.name)))
    onView(withId(R.id.rating)).check(matches(withText(venueDetails.rating.toString())))
    onView(withId(R.id.infoContainer)).check(matches(isDisplayed()))
    onView(withId(R.id.venueAddressLine1)).check(matches(withText(venueDetails.location?.address)))
    onView(withId(R.id.venueAddressLine2)).check(matches(withText(venueDetails.location?.city)))
  }

  @Test
  fun detailViewFragment_successDataMissingAddress() {
    val fragmentArgs = bundleOf("venueId" to "testVenueId")
    val venue = Venue("testVenueId", "restaurant", Location("Nowhere", 0.0, 0.0, "", "", "", "", listOf()))
    val venueDetails = VenueDetails(
      "restaurant",
      null,
      null,
      null,
      4.5,
      null,
      null
    )

    runBlocking {
      `when`(venueRepository.getVenueFromCache("testVenueId")).thenReturn(venue)
      `when`(venueRepository.getVenueDetails("testVenueId")).thenReturn(ResultWrapper.Success(venueDetails))
    }

    launchFragmentInHiltContainer<VenueDetailFragment>(fragmentArgs)

    onView(withId(R.id.businessDetailTitle)).check(matches(withText(venue.name)))
    onView(withId(R.id.rating)).check(matches(withText(venueDetails.rating.toString())))
    onView(withId(R.id.infoContainer)).check(matches(isDisplayed()))
    onView(withId(R.id.locationContainer)).check(matches(not(isDisplayed())))
  }
}