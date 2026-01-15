package com.kappa.app.core.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Base Fragment with ViewState pattern support.
 */
abstract class BaseFragment<VS : ViewState> : Fragment() {

    protected abstract val viewModel: BaseViewModel<VS>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
