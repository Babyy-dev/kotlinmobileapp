package com.kappa.app.core.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Base Activity with ViewState pattern support.
 */
abstract class BaseActivity<VS : ViewState> : AppCompatActivity() {

    protected abstract val viewModel: BaseViewModel<VS>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupObservers()
        setupViews()
    }

    protected open fun setupViews() {
        // Override in subclasses
    }

    protected open fun setupObservers() {
        observeViewState(viewModel.viewState) { state ->
            handleViewState(state)
        }
    }

    protected fun <T> LifecycleOwner.observeViewState(flow: Flow<T>, action: (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(action)
            }
        }
    }

    protected abstract fun handleViewState(state: VS)
}

/**
 * Base ViewState interface.
 */
interface ViewState

/**
 * Base ViewModel with ViewState pattern.
 */
abstract class BaseViewModel<VS : ViewState> : androidx.lifecycle.ViewModel() {
    protected val _viewState = MutableStateFlow<VS?>(null)
    val viewState: kotlinx.coroutines.flow.Flow<VS> = _viewState.filterNotNull()
    
    init {
        // Initialize view state - subclasses should call setViewState() in init
    }
    
    protected fun setViewState(state: VS) {
        _viewState.value = state
    }
}
