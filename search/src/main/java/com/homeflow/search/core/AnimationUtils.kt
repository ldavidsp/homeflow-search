package com.homeflow.search.core

import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.hypot

/**
 * Animation utils
 *
 * @constructor Create empty Animation utils
 */
object AnimationUtils {
  private const val ANIMATION_DURATION_SHORT = 250

  @JvmStatic
  @JvmOverloads
  fun circleRevealView(view: View, duration: Int = ANIMATION_DURATION_SHORT) {
    val cx = view.width
    val cy = view.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius)
    anim.duration = if (duration > 0) duration.toLong() else ANIMATION_DURATION_SHORT.toLong()
    view.visibility = View.VISIBLE
    anim.start()
  }

  @JvmStatic
  fun circleHideView(view: View, listenerAdapter: AnimatorListenerAdapter) {
    circleHideView(view, ANIMATION_DURATION_SHORT, listenerAdapter)
  }

  @JvmStatic
  fun circleHideView(view: View, duration: Int, listenerAdapter: AnimatorListenerAdapter) {
    val cx = view.width
    val cy = view.height / 2
    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0f)
    anim.addListener(listenerAdapter)
    anim.duration = if (duration > 0) duration.toLong() else ANIMATION_DURATION_SHORT.toLong()
    anim.start()
  }
}

