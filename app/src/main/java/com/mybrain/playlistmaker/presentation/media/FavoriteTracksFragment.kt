package com.mybrain.playlistmaker.presentation.media

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.search.SearchTrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<FavoriteTracksViewModel>()

    private val openPlayerDebounceHandler = Handler(Looper.getMainLooper())
    private var canOpenPlayer = true
    private lateinit var trackAdapter: SearchTrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackAdapter = SearchTrackAdapter(mutableListOf()) { track ->
            openPlayer(track)
        }

        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.setHasFixedSize(true)
        binding.rvTracks.adapter = trackAdapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: FavoriteTracksState) {
        when (state) {
            is FavoriteTracksState.Empty -> {
                binding.rvTracks.visibility = View.GONE
                binding.emptyPlaceholder.visibility = View.VISIBLE
            }
            is FavoriteTracksState.Content -> {
                binding.rvTracks.visibility = View.VISIBLE
                binding.emptyPlaceholder.visibility = View.GONE
                trackAdapter.updateData(state.tracks)
            }
        }
    }

    private fun openPlayer(track: TrackUI) {
        if (!canOpenPlayer) return

        canOpenPlayer = false

        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, bundle)

        openPlayerDebounceHandler.postDelayed(
            { canOpenPlayer = true },
            500L
        )
    }

    override fun onResume() {
        super.onResume()
        canOpenPlayer = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}
