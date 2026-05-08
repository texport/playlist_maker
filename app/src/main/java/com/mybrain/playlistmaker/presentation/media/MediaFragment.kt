package com.mybrain.playlistmaker.presentation.media

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.media.ui.MediaRoute
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistFragment
import com.mybrain.playlistmaker.presentation.playlist.PlaylistFragment
import com.mybrain.playlistmaker.presentation.root.RootActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {

    private val favoriteViewModel: FavoriteTracksViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    private val openPlayerDebounceHandler = Handler(Looper.getMainLooper())
    private var canOpenPlayer = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    MediaRoute(
                        favoriteViewModel = favoriteViewModel,
                        playlistsViewModel = playlistsViewModel,
                        onTrackClick = { track -> openPlayer(track) },
                        onOpenPlaylist = { playlistId ->
                            val action =
                                MediaFragmentDirections.actionMediaFragmentToPlaylistFragment(playlistId)
                            findNavController().navigate(action)
                        },
                        onCreatePlaylist = {
                            findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
                        },
                        onEditPlaylist = { playlistId ->
                            findNavController().navigate(
                                MediaFragmentDirections.actionMediaFragmentToCreatePlaylistFragment(
                                    playlistId,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            CreatePlaylistFragment.PLAYLIST_CREATED_RESULT,
            viewLifecycleOwner,
        ) { _, bundle ->
            val name = bundle.getString(CreatePlaylistFragment.PLAYLIST_NAME_KEY).orEmpty()
            if (name.isNotBlank()) {
                showSnackbarMessage(getString(R.string.playlist_created, name))
            }
        }

        parentFragmentManager.setFragmentResultListener(
            PlaylistFragment.PLAYLIST_DELETED_RESULT,
            viewLifecycleOwner,
        ) { _, bundle ->
            val name =
                bundle.getString(PlaylistFragment.PLAYLIST_DELETED_NAME).orEmpty()
            if (name.isNotBlank()) {
                showSnackbarMessage(getString(R.string.playlist_deleted_message, name))
            }
        }

        playlistsViewModel.shareEvent.observe(viewLifecycleOwner) { event ->
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
            playlistsViewModel.clearShareEvent()
        }

        playlistsViewModel.playlistDeleted.observe(viewLifecycleOwner) { name ->
            if (name.isNullOrBlank()) return@observe
            showSnackbarMessage(getString(R.string.playlist_deleted_message, name))
            playlistsViewModel.clearPlaylistDeleted()
        }
    }

    private fun openPlayer(track: TrackUI) {
        if (!canOpenPlayer) return
        canOpenPlayer = false
        val bundle = bundleOf("track" to track)
        findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, bundle)
        openPlayerDebounceHandler.postDelayed(
            { canOpenPlayer = true },
            500L,
        )
    }

    override fun onResume() {
        super.onResume()
        canOpenPlayer = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    private fun showSnackbarMessage(message: String) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(R.drawable.snackbar_black_rounded)
        snackbar.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                R.color.second_background,
            ),
        )
        val snackbarHeight =
            resources.getDimensionPixelSize(R.dimen.snackbar_max_height)
        snackbarView.layoutParams.height = snackbarHeight
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_14))
            gravity = Gravity.CENTER
        }
        val params = snackbarView.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            val horizontal = resources.getDimensionPixelSize(R.dimen.snackbar_margin_horizontal)
            val bottom = resources.getDimensionPixelSize(R.dimen.snackbar_margin_bottom)
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

    private fun buildShareText(playlist: PlaylistUI, tracks: List<TrackUI>): String {
        val builder = StringBuilder()
        builder.append(playlist.name).append('\n')
        if (!playlist.description.isNullOrBlank()) {
            builder.append(playlist.description).append('\n')
        }
        builder.append(
            resources.getQuantityString(R.plurals.tracks_count, tracks.size, tracks.size),
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
}
