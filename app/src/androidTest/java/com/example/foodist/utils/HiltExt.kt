package com.example.foodist.utils


import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.foodist.HiltTestActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
  fragmentArgs: Bundle? = null,
  crossinline action: Fragment.() -> Unit = {}
) {
  val startActivityIntent = Intent.makeMainActivity(
    ComponentName(
      ApplicationProvider.getApplicationContext(),
      HiltTestActivity::class.java
    )
  )

  ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
    val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
      Preconditions.checkNotNull(T::class.java.classLoader),
      T::class.java.name
    )
    fragment.arguments = fragmentArgs
    activity.supportFragmentManager
      .beginTransaction()
      .add(android.R.id.content, fragment, "")
      .commitNow()

    fragment.action()
  }
}


@ExperimentalCoroutinesApi
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
  fragmentArgs: Bundle? = null,
  factory: FragmentFactory,
  crossinline action: Fragment.() -> Unit = {}
) {
  val startActivityIntent = Intent.makeMainActivity(
    ComponentName(
      ApplicationProvider.getApplicationContext(),
      HiltTestActivity::class.java
    )
  )

  ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
    activity.supportFragmentManager.fragmentFactory = factory
    val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
      Preconditions.checkNotNull(T::class.java.classLoader),
      T::class.java.name
    )
    fragment.arguments = fragmentArgs

    activity.supportFragmentManager
      .beginTransaction()
      .add(android.R.id.content, fragment, "")
      .commit()

    fragment.action()
  }
}
