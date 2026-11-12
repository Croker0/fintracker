package ru.fintracker.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun tint(color: Int): ColorStateList = ColorStateList.valueOf(color)

fun <T> Fragment.collectFlow(flow: Flow<T>, block: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value -> block(value) }
        }
    }
}

fun operationsCount(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "операция"
        mod10 in 2..4 && mod100 !in 12..14 -> "операции"
        else -> "операций"
    }
    return "$count $word"
}
