package com.mybrain.playlistmaker.presentation.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.search.ui.SearchRoute
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val openPlayerDebounceHandler = Handler(Looper.getMainLooper())
    private var canOpenPlayer = true
    private val viewModel: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SearchRoute(
                        viewModel = viewModel,
                        onOpenPlayer = { track -> openPlayer(track) },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        canOpenPlayer = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    private fun openPlayer(track: TrackUI) {
        if (!canOpenPlayer) return

        canOpenPlayer = false

        val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)
        findNavController().navigate(action)

        openPlayerDebounceHandler.postDelayed(
            { canOpenPlayer = true },
            500L,
        )
    }
}
