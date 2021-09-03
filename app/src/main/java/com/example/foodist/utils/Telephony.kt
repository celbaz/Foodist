package com.example.foodist.utils

import android.content.Context
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Telephony @Inject constructor(@ApplicationContext val context: Context) {
  fun getTelephonyRadioType(): String {
    val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    return when (telephony.phoneType) {
      TelephonyManager.PHONE_TYPE_NONE -> "nr"
      TelephonyManager.PHONE_TYPE_CDMA -> "cdma"
      TelephonyManager.PHONE_TYPE_GSM -> "gsm"
      else -> "lte"
    }
  }
}