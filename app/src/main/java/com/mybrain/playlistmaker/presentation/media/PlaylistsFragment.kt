package com.mybrain.playlistmaker.presentation.media

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.databinding.FragmentPlaylistsBinding
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistFragment
import com.mybrain.playlistmaker.presentation.root.RootActivity
import com.mybrain.playlistmaker.presentation.utils.GridHorizontalSpacingItemDecoration
import com.mybrain.playlistmaker.presentation.utils.GridVerticalSpacingItemDecoration
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistsViewModel>()
    private val playlistsAdapter = PlaylistsAdapter(
        onPlaylistClick = { playlist ->
            val action = MediaFragmentDirections.actionMediaFragmentToPlaylistFragment(playlist.playlistId)
            findNavController().navigate(action)
        },
        onPlaylistLongClick = { anchor, playlist ->
            showPlaylistMenu(anchor, playlist)
        }
    )

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
                showSnackbarMessage(getString(com.mybrain.playlistmaker.R.string.playlist_created, name))
            }
        }

        requireParentFragment().parentFragmentManager.setFragmentResultListener(
            com.mybrain.playlistmaker.presentation.playlist.PlaylistFragment.PLAYLIST_DELETED_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            val name =
                bundle.getString(
                    com.mybrain.playlistmaker.presentation.playlist.PlaylistFragment.PLAYLIST_DELETED_NAME
                ).orEmpty()
            if (name.isNotBlank()) {
                showSnackbarMessage(
                    getString(com.mybrain.playlistmaker.R.string.playlist_deleted_message, name)
                )
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        viewModel.shareEvent.observe(viewLifecycleOwner) { event ->
            if (event == null) return@observe
            when (event) {
                PlaylistsShareEvent.Empty -> {
                    showSnackbarMessage(getString(R.string.playlist_share_empty))
                }
                is PlaylistsShareEvent.Ready -> {
                    val text = buildShareText(event.playlist, event.tracks)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                }
            }
            viewModel.clearShareEvent()
        }

        viewModel.playlistDeleted.observe(viewLifecycleOwner) { name ->
            if (name.isNullOrBlank()) return@observe
            showSnackbarMessage(getString(R.string.playlist_deleted_message, name))
            viewModel.clearPlaylistDeleted()
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

    private fun showSnackbarMessage(message: String) {
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

    private fun showPlaylistMenu(anchor: View, playlist: PlaylistUI) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.playlist_context_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_share -> viewModel.requestShare(playlist)
                R.id.menu_edit -> {
                    val action =
                        MediaFragmentDirections.actionMediaFragmentToCreatePlaylistFragment(playlist.playlistId)
                    findNavController().navigate(action)
                }
                R.id.menu_delete -> showDeletePlaylistDialog(playlist)
            }
            true
        }
        popup.show()
    }

    private fun showDeletePlaylistDialog(playlist: PlaylistUI) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(R.string.delete_playlist_message)
            .setNegativeButton(R.string.delete_playlist_cancel, null)
            .setPositiveButton(R.string.delete_playlist_confirm) { _, _ ->
                viewModel.deletePlaylist(playlist.playlistId, playlist.name)
            }
            .create()
        dialog.setOnShowListener {
            val color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.blue)
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(color)
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)?.setTextColor(color)
        }
        dialog.show()
    }

    private fun buildShareText(playlist: PlaylistUI, tracks: List<TrackUI>): String {
        val builder = StringBuilder()
        builder.append(playlist.name).append('\n')
        if (!playlist.description.isNullOrBlank()) {
            builder.append(playlist.description).append('\n')
        }
        builder.append(
            resources.getQuantityString(R.plurals.tracks_count, tracks.size, tracks.size)
        ).append('\n')
        tracks.forEachIndexed { index, track ->
            val duration = Utils.formatTime(track.trackTime.toInt())
            builder.append(index + 1)
                .append(". ")
                .append(track.artistName)
                .append(" - ")
                .append(track.trackName)
                .append(" (")
                .append(duration)
                .append(')')
            if (index != tracks.lastIndex) {
                builder.append('\n')
            }
        }
        return builder.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
