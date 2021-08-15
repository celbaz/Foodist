package com.example.foodist.utils

import android.content.res.Resources
import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.pow


class MapMeasurements {

  fun getRadius(latLng: LatLng, zoom: Double): Double {
    var width = getScreenWidth().toDouble()
    var perPixelSize = metersPerPx(latLng, zoom)
    return width * perPixelSize
  }

  fun metersPerPx(latLng: LatLng, zoom: Double): Double {
    return GOOGLE_MAPS_ZOOM_CONSTANT * cos(latLng.latitude * Math.PI / 180) / (2.0).pow(zoom)
  }

  private fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
  }

  private fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
  }

  companion object {
    val GOOGLE_MAPS_ZOOM_CONSTANT = 156543.03392
  }
}