package com.homeflow.search.core.interfaces

/**
 * On search query text listener.
 */
interface OnSearchQueryTextListener {

  /**
   * On query text changed.
   *
   * @param newText the new text
   */
  fun onQueryTextChange(newText: String): Boolean

}
