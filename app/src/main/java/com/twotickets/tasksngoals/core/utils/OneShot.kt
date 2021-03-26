package com.twotickets.tasksngoals.core.utils

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents a one shot event e.g. snackbar message.
 */
class OneShot<out T>(private val content: T?) {

    private var handled = false

    /**
     * Returns the content and prevents its use again.
     */
    val contentIfNotHandled: T?
        @Synchronized get() = content.takeUnless { handled }.apply { handled = true }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peek(): T? = content
}