package com.mybrain.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.util.TypedValue
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mybrain.playlistmaker.databinding.FragmentPlaylistsBinding
import com.mybrain.playlistmaker.presentation.utils.GridHorizontalSpacingItemDecoration
import com.mybrain.playlistmaker.presentation.utils.GridVerticalSpacingItemDecoration
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.material.snackbar.Snackbar
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistFragment
import com.mybrain.playlistmaker.presentation.root.RootActivity

class PlaylistsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistsViewModel>()
    private val playlistsAdapter = PlaylistsAdapter()

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playlistsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecycler.adapter = playlistsAdapter
        val spacing = resources.getDimensionPixelSize(com.mybrain.playlistmaker.R.dimen.spacing_8)
        binding.playlistsRecycler.addItemDecoration(
            GridHorizontalSpacingItemDecoration(spacing)
        )
        val verticalSpacing = resources.getDimensionPixelSize(com.mybrain.playlistmaker.R.dimen.spacing_16)
        binding.playlistsRecycler.addItemDecoration(
            GridVerticalSpacingItemDecoration(verticalSpacing)
        )

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(
                com.mybrain.playlistmaker.R.id.action_mediaFragment_to_createPlaylistFragment
            )
        }

        requireParentFragment().parentFragmentManager.setFragmentResultListener(
            CreatePlaylistFragment.PLAYLIST_CREATED_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(CreatePlaylistFragment.PLAYLIST_NAME_KEY).orEmpty()
            if (name.isNotBlank()) {
                showPlaylistCreatedMessage(name)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Content -> {
                binding.placeholderEmpty.visibility = View.GONE
                binding.playlistsRecycler.visibility = View.VISIBLE
                playlistsAdapter.submitList(state.playlists)
            }
            PlaylistsState.Empty -> {
                playlistsAdapter.submitList(emptyList())
                binding.playlistsRecycler.visibility = View.GONE
                binding.placeholderEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun showPlaylistCreatedMessage(playlistName: String) {
        val message = getString(com.mybrain.playlistmaker.R.string.playlist_created, playlistName)
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(com.mybrain.playlistmaker.R.drawable.snackbar_black_rounded)
        snackbar.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                com.mybrain.playlistmaker.R.color.second_background
            )
        )
        val snackbarHeight =
            resources.getDimensionPixelSize(com.mybrain.playlistmaker.R.dimen.snackbar_max_height)
        snackbarView.layoutParams.height = snackbarHeight
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(com.mybrain.playlistmaker.R.dimen.text_size_14))
            gravity = Gravity.CENTER
        }
        val params = snackbarView.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            val horizontal = resources.getDimensionPixelSize(com.mybrain.playlistmaker.R.dimen.snackbar_margin_horizontal)
            val bottom = resources.getDimensionPixelSize(com.mybrain.playlistmaker.R.dimen.snackbar_margin_bottom)
            params.setMargins(horizontal, params.topMargin, horizontal, bottom)
            snackbarView.layoutParams = params
        }
        (activity as? RootActivity)?.setBottomNavVisible(false)
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                (activity as? RootActivity)?.setBottomNavVisible(true)
            }
        })
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
