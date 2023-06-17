package com.homeflow.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.homeflow.search.core.AnimationUtils.circleHideView
import com.homeflow.search.core.AnimationUtils.circleRevealView
import com.homeflow.search.core.interfaces.OnSearchClearTextClickListener
import com.homeflow.search.core.interfaces.OnSearchQueryTextListener
import com.homeflow.search.core.interfaces.OnSearchViewListener
import com.homeflow.search.core.interfaces.OnSearchVoiceClickedListener
import kotlin.math.roundToInt

/**
 * Homeflow search view.
 *
 * @constructor Create empty Homeflow search view
 * @param mContext Context
 * @param attributeSet AttributeSet?
 * @param defStyleAttributes Int
 * @constructor
 */
class HomeflowSearchView @JvmOverloads constructor(private val mContext: Context, attributeSet: AttributeSet? = null, defStyleAttributes: Int = 0) : FrameLayout(mContext, attributeSet) {

  companion object {
    /**
     * The maximum number of results we want to return from the voice recognition.
     */
    private const val MAX_RESULTS = 1

    /**
     * Number of suggestions to show.
     */
    private const val EMPTY_STRING = ""
  }

  init {
    init()
    initStyle(attributeSet, defStyleAttributes)
  }

  /**
   * Determines if the search view is opened or closed.
   * @return True if the search view is open, false if it is closed.
   */
  /**
   * Whether or not the search view is open right now.
   */
  var isOpen = false
    private set

  /**
   * Whether or not the HomeflowSearchView will animate into view or just appear.
   */
  private var mShouldAnimate = true

  /**
   * Whether or not the HomeflowSearchView will clonse under a click on the Tint View (Blank Area).
   */
  private var mShouldCloseOnTintClick = false

  /**
   * Flag for whether or not we are clearing focus.
   */
  private var mClearingFocus = false

  /**
   * Voice hint prompt text.
   */
  private lateinit var mHintPrompt: String

  /**
   * Allows user to decide whether to allow voice search.
   */
  private var isVoiceIconEnabled = false

  /**
   * The root of the search view.
   */
  private lateinit var mRoot: FrameLayout

  /**
   * The bar at the top of the SearchView containing the EditText and ImageButtons.
   */
  private lateinit var mSearchBar: LinearLayout

  /**
   * The EditText for entering a search.
   */
  private lateinit var mSearchEditText: EditText

  /**
   * The ImageButton for navigating back.
   */
  private lateinit var mBack: ImageButton

  /**
   * The ImageButton for initiating a voice search.
   */
  private lateinit var mVoice: ImageButton
  private lateinit var mVoiceResultIntent: ActivityResultLauncher<Intent>

  /**
   * The ImageButton for clearing the search text.
   */
  private lateinit var mClear: ImageButton

  /**
   * The previous query text.
   */
  private lateinit var mOldQuery: CharSequence

  /**
   * The current query text.
   */
  private lateinit var mCurrentQuery: CharSequence

  /**
   * Listener for when the query text is submitted or changed.
   */
  private var mOnSearchQueryTextListener: OnSearchQueryTextListener? = null

  /**
   * Listener for when the search view opens and closes.
   */
  private var mSearchViewListener: OnSearchViewListener? = null

  /**
   * Listener for interaction with the voice button.
   */
  private var mOnSearchVoiceClickedListener: OnSearchVoiceClickedListener? = null

  /**
   * Listener for interaction with the clear (X) button
   */
  private var mOnClearClickListener: OnSearchClearTextClickListener? = null

  /**
   * Preforms any required initializations for the search view.
   */
  private fun init() {
    LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true)
    mRoot = findViewById(R.id.searchLayout)
    mSearchBar = mRoot.findViewById(R.id.searchBar)
    mBack = mRoot.findViewById(R.id.searchBack)
    mSearchEditText = mRoot.findViewById(R.id.searchInput)
    mVoice = mRoot.findViewById(R.id.searchVoice)
    mClear = mRoot.findViewById(R.id.searchClear)
    mBack.setOnClickListener { closeSearch() }
    mVoice.setOnClickListener { onVoiceClicked() }
    mClear.setOnClickListener { onClearClicked() }
    initSearchView()
  }

  /**
   * Initializes the style of this view.
   * @param attributeSet The attributes to apply to the view.
   * @param defStyleAttribute An attribute to the style theme applied to this view.
   */
  private fun initStyle(attributeSet: AttributeSet?, defStyleAttribute: Int) {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    val typedArray = mContext.obtainStyledAttributes(
      attributeSet,
      R.styleable.HomeflowSearchView,
      defStyleAttribute,
      0
    )

    if (typedArray.hasValue(R.styleable.HomeflowSearchView_searchBackground)) {
      background = typedArray.getDrawable(R.styleable.HomeflowSearchView_searchBackground)!!
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_android_textColor)) {
      setTextColor(
        typedArray.getColor(
          R.styleable.HomeflowSearchView_android_textColor,
          ContextCompat.getColor(mContext, R.color.black)
        )
      )
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_android_textColorHint)) {
      setHintTextColor(
        typedArray.getColor(
          R.styleable.HomeflowSearchView_android_textColorHint,
          ContextCompat.getColor(mContext, R.color.search_gray_50)
        )
      )
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_android_hint)) {
      setHint(typedArray.getString(R.styleable.HomeflowSearchView_android_hint))
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_searchVoiceIcon)) {
      setVoiceIcon(
        typedArray.getResourceId(
          R.styleable.HomeflowSearchView_searchVoiceIcon,
          R.drawable.icon_microphone
        )
      )
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_searchCloseIcon)) {
      setClearIcon(
        typedArray.getResourceId(
          R.styleable.HomeflowSearchView_searchCloseIcon,
          R.drawable.icon_close_light
        )
      )
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_searchBackIcon)) {
      setBackIcon(
        typedArray.getResourceId(
          R.styleable.HomeflowSearchView_searchBackIcon,
          R.drawable.icon_back_light
        )
      )
    }

    if (typedArray.hasValue(R.styleable.HomeflowSearchView_android_inputType)) {
      setInputType(
        typedArray.getInteger(
          R.styleable.HomeflowSearchView_android_inputType,
          InputType.TYPE_CLASS_TEXT
        )
      )
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_searchBarHeight)) {
      setSearchBarHeight(
        typedArray.getDimensionPixelSize(
          R.styleable.HomeflowSearchView_searchBarHeight,
          appCompatActionBarHeight
        )
      )
    } else {
      setSearchBarHeight(appCompatActionBarHeight)
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_voiceHintPrompt)) {
      setVoiceHintPrompt(
        typedArray.getString(R.styleable.HomeflowSearchView_voiceHintPrompt) ?: EMPTY_STRING
      )
    } else {
      setVoiceHintPrompt(mContext.getString(R.string.search_voice_hint))
    }
    if (typedArray.hasValue(R.styleable.HomeflowSearchView_voiceIconEnabled)) {
      isVoiceIconEnabled =
        typedArray.getBoolean(R.styleable.HomeflowSearchView_voiceIconEnabled, true)
    }
    fitsSystemWindows = false
    typedArray.recycle()
    displayVoiceButton(true)
  }

  /**
   * Preforms necessary initializations on the SearchView.
   */
  private fun initSearchView() {
    mSearchEditText.setOnEditorActionListener { _, _, _ ->
      true
    }
    mSearchEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        this@HomeflowSearchView.onTextChanged(s)
      }

      override fun afterTextChanged(s: Editable) {}
    })
    mSearchEditText.onFocusChangeListener =
      OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
          showKeyboard(mSearchEditText)
        }
      }
  }

  /**
   * Displays the keyboard with a focus on the Search EditText.
   * @param view The view to attach the keyboard to.
   */
  private fun showKeyboard(view: View?) {
    view?.requestFocus()
    if (isHardKeyboardAvailable.not()) {
      val inputMethodManager =
        view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
      inputMethodManager?.showSoftInput(view, 0)
    }
  }

  /**
   * Method that checks if there's a physical keyboard on the phone.
   *
   * @return true if there's a physical keyboard connected, false otherwise.
   */
  private val isHardKeyboardAvailable: Boolean
    get() = mContext.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS

  /**
   * Changes the visibility of the voice button to VISIBLE or GONE.
   * @param display True to display the voice button, false to hide it.
   */
  private fun displayVoiceButton(display: Boolean) {
    // Only display voice if we pass in true, and it's available
    if (display) {
      mVoice.visibility = VISIBLE
    } else {
      mVoice.visibility = GONE
    }
  }

  /**
   * Changes the visibility of the clear button to VISIBLE or GONE.
   * @param display True to display the clear button, false to hide it.
   */
  private fun displayClearButton(display: Boolean) {
    mClear.visibility = if (display) VISIBLE else GONE
  }

  /**
   * Displays the SearchView.
   */
  fun openSearch() {
    if (isOpen) {
      return
    }

    // Get focus
    mSearchEditText.setText(EMPTY_STRING)
    mSearchEditText.requestFocus()
    if (mShouldAnimate) {
      mRoot.isVisible = true
      circleRevealView(mSearchBar)
    } else {
      mRoot.isVisible = true
    }

    mSearchViewListener?.onSearchViewOpened()
    isOpen = true
  }

  /**
   * Hides the keyboard displayed for the SearchEditText.
   * @param view The view to detach the keyboard from.
   */
  private fun hideKeyboard(view: View) {
    val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
  }

  /**
   * Closes the search view if necessary.
   */
  fun closeSearch() {
    if (!isOpen) {
      return
    }

    // Clear text, values, and focus.
    mSearchEditText.setText(EMPTY_STRING)
    clearFocus()
    if (mShouldAnimate) {
      val v: View = mRoot
      val listenerAdapter: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          super.onAnimationEnd(animation)
          v.isVisible = false
        }
      }

      circleHideView(mSearchBar, listenerAdapter)
    } else {
      mRoot.isVisible = false
    }

    mSearchViewListener?.onSearchViewClosed()
    isOpen = false
  }

  /**
   * Filters and updates the buttons when text is changed.
   * @param newText The new text.
   */
  private fun onTextChanged(newText: CharSequence) {
    mCurrentQuery = mSearchEditText.text
    if (!TextUtils.isEmpty(mCurrentQuery)) {
      displayVoiceButton(false)
      displayClearButton(true)
    } else {
      displayClearButton(false)
      displayVoiceButton(true)
    }

    mOnSearchQueryTextListener?.onQueryTextChange(newText.toString())
    mOldQuery = mCurrentQuery
  }

  /**
   * Handles when the voice button is clicked and starts listening, then calls activity with voice search.
   */
  private fun onVoiceClicked() {
    if (mOnSearchVoiceClickedListener != null) {
      mOnSearchVoiceClickedListener?.onVoiceClicked()
    } else {
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mHintPrompt)
      intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
      )
      intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
      if (mContext is Activity) {
        mVoiceResultIntent.launch(intent)
      }
    }
  }

  /**
   * Handles when the clear (X) button is clicked.
   */
  private fun onClearClicked() {
    mOnClearClickListener?.onClearClicked()
    mSearchEditText.setText(EMPTY_STRING)
  }

  fun setOnQueryTextListener(mOnQueryTextListener: OnSearchQueryTextListener?) {
    this.mOnSearchQueryTextListener = mOnQueryTextListener
  }

  fun setSearchViewListener(mSearchViewListener: OnSearchViewListener?) {
    this.mSearchViewListener = mSearchViewListener
  }

  /**
   * Toggles the Tint click action.
   *
   * @param shouldClose - Whether the tint click should close the search view or not.
   */
  fun setCloseOnTintClick(shouldClose: Boolean) {
    mShouldCloseOnTintClick = shouldClose
  }

  /**
   * Set the query to search view. If submit is set to true, it'll submit the query.
   *
   * @param query - The Query value.
   * @param submit - Whether to submit or not the query or not.
   */
  fun setQuery(query: CharSequence?) {
    mSearchEditText.setText(query)
    if (query != null) {
      mSearchEditText.setSelection(mSearchEditText.length())
      mCurrentQuery = query
    }
  }

  fun setSearchBarColor(color: Int) {
    mSearchEditText.setBackgroundColor(color)
  }

  /**
   * Adjust the alpha of a color based on a percent factor.
   *
   * @param color - The color you want to change the alpha value.
   * @param factor - The factor of the alpha, from 0% to 100%.
   * @return The color with the adjusted alpha value.
   */
  private fun adjustAlpha(color: Int, factor: Float): Int {
    if (factor < 0) return color
    val alpha = (Color.alpha(color) * factor).roundToInt()
    return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
  }

  /**
   * Sets the text color of the EditText.
   * @param color The color to use for the EditText.
   */
  fun setTextColor(color: Int) {
    mSearchEditText.setTextColor(color)
  }

  /**
   * Sets the text color of the search hint.
   * @param color The color to be used for the hint text.
   */
  fun setHintTextColor(color: Int) {
    mSearchEditText.setHintTextColor(color)
  }

  /**
   * Sets the hint to be used for the search EditText.
   * @param hint The hint to be displayed in the search EditText.
   */
  fun setHint(hint: CharSequence?) {
    mSearchEditText.hint = hint
  }

  /**
   * Sets the icon for the voice action.
   * @param resourceId The drawable to represent the voice action.
   */
  fun setVoiceIcon(resourceId: Int) {
    mVoice.setImageResource(resourceId)
  }

  /**
   * Set intent voice
   * @param voiceResultIntent The intent to be used for the voice action.
   */
  fun setVoiceResultIntent(voiceResultIntent: ActivityResultLauncher<Intent>) {
    mVoiceResultIntent = voiceResultIntent
  }

  /**
   * Sets the icon for the clear action.
   * @param resourceId The resource ID of drawable that will represent the clear action.
   */
  fun setClearIcon(resourceId: Int) {
    mClear.setImageResource(resourceId)
  }

  /**
   * Sets the icon for the back action.
   * @param resourceId The resource Id of the drawable that will represent the back action.
   */
  fun setBackIcon(resourceId: Int) {
    mBack.setImageResource(resourceId)
  }

  /**
   * Sets the input type of the SearchEditText.
   *
   * @param inputType The input type to set to the EditText.
   */
  fun setInputType(inputType: Int) {
    mSearchEditText.inputType = inputType
  }

  /**
   * Sets a click listener for the voice button.
   */
  fun setOnVoiceClickedListener(listener: OnSearchVoiceClickedListener?) {
    mOnSearchVoiceClickedListener = listener
  }

  fun setOnVoiceClickedListener(listener: () -> Unit) {
    setOnVoiceClickedListener(object : OnSearchVoiceClickedListener {
      override fun onVoiceClicked() {
        listener.invoke()
      }
    })
  }

  /**
   * Sets a click listener for the clear (X) button.
   */
  fun setOnClearClickListener(listener: OnSearchClearTextClickListener) {
    mOnClearClickListener = listener
  }

  /**
   *
   * @param listener () -> Unit
   */
  fun setOnClearClickListener(listener: () -> Unit) {
    setOnClearClickListener(object : OnSearchClearTextClickListener {
      override fun onClearClicked() {
        listener.invoke()
      }
    })
  }

  /**
   * Set search bar height.
   *
   * @param height Int
   */
  fun setSearchBarHeight(height: Int) {
    mSearchBar.minimumHeight = height
    mSearchBar.layoutParams.height = height
  }

  /**
   * Set Voice Hint Prompt
   *
   * @param hintPrompt String
   */
  fun setVoiceHintPrompt(hintPrompt: String) {
    mHintPrompt = if (!TextUtils.isEmpty(hintPrompt)) {
      hintPrompt
    } else {
      mContext.getString(R.string.search_voice_hint)
    }
  }

  /**
   * App compat action bar height
   */
  private val appCompatActionBarHeight: Int
    get() {
      val tv = TypedValue()
      context.theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true)
      return resources.getDimensionPixelSize(tv.resourceId)
    }

  /**
   * Current query
   */
  val currentQuery: String
    get() = if (!TextUtils.isEmpty(mCurrentQuery)) {
      mCurrentQuery.toString()
    } else EMPTY_STRING

  /**
   * Is voice available
   */
  private val isVoiceAvailable: Boolean
    get() {
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      val activities = mContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
      return activities.size > 0
    }

  /**
   * Clear focus
   */
  override fun clearFocus() {
    mClearingFocus = true
    hideKeyboard(this)
    super.clearFocus()
    mSearchEditText.clearFocus()
    mClearingFocus = false
  }

  /**
   * On focus change
   *
   * @param direction Int
   * @param previouslyFocusedRect Rect?
   * @return Boolean
   */
  override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
    return !(mClearingFocus || !isFocusable) && mSearchEditText.requestFocus(
      direction,
      previouslyFocusedRect
    )
  }

}
