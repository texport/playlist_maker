package com.mybrain.playlistmaker.presentation.playlist

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.databinding.FragmentPlaylistBinding
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import kotlin.math.roundToInt

class PlaylistFragment : Fragment() {

    private val args by navArgs<PlaylistFragmentArgs>()
    private val viewModel: PlaylistViewModel by viewModel {
        parametersOf(args.playlistId)
    }

    private val tracksAdapter = PlaylistTracksAdapter(
        tracks = mutableListOf(),
        onItemClick = { track -> openPlayer(track) },
        onItemLongClick = { track -> showDeleteTrackDialog(track) }
    )

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var tracksBottomSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var menuBottomSheet: BottomSheetBehavior<LinearLayout>
    private val actionsLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            adjustTracksPeekHeight()
        }
    private val menuBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val binding = _binding ?: return
            binding.overlay.visibility =
                if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            if (newState == BottomSheetBehavior.STATE_HIDDEN && pendingShare) {
                pendingShare = false
                sharePlaylist()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val binding = _binding ?: return
            binding.overlay.alpha = slideOffset.coerceIn(0f, 1f)
        }
    }

    private var currentPlaylist: PlaylistUI? = null
    private var currentTracks: List<TrackUI> = emptyList()
    private var pendingShare: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tracksRecycler.adapter = tracksAdapter

        initBottomSheets()
        initListeners()
        observeViewModel()

        binding.actionsRow.addOnLayoutChangeListener(actionsLayoutChangeListener)
        binding.actionsRow.doOnLayout {
            adjustTracksPeekHeight()
            adjustMenuHeight()
        }
    }

    private fun initBottomSheets() {
        tracksBottomSheet = BottomSheetBehavior.from(binding.bottomSheetTracks).apply {
            isFitToContents = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        menuBottomSheet = BottomSheetBehavior.from(binding.bottomSheetMenu).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isFitToContents = true
            skipCollapsed = true
        }

        menuBottomSheet.addBottomSheetCallback(menuBottomSheetCallback)
    }

    private fun adjustTracksPeekHeight() {
        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_16) +
            resources.getDimensionPixelSize(R.dimen.spacing_8)
        val topOffset = binding.actionsRow.bottom + spacing
        val peek = (binding.root.height - topOffset).coerceAtLeast(0)
        if (peek > 0) {
            tracksBottomSheet.peekHeight = peek
            tracksBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun adjustMenuHeight() {
        val height = (binding.root.height * MENU_HEIGHT_RATIO).roundToInt()
        if (height <= 0) return
        val params = binding.bottomSheetMenu.layoutParams
        if (params.height != height) {
            params.height = height
            binding.bottomSheetMenu.layoutParams = params
        }
    }

    private fun initListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnShare.setOnClickListener { sharePlaylist() }
        binding.btnMenu.setOnClickListener { openMenu() }
        binding.overlay.setOnClickListener { closeMenu() }

        binding.menuShare.setOnClickListener {
            pendingShare = true
            closeMenu()
        }
        binding.menuEdit.setOnClickListener {
            closeMenu()
            val action =
                PlaylistFragmentDirections.actionPlaylistFragmentToCreatePlaylistFragment(args.playlistId)
            findNavController().navigate(action)
        }
        binding.menuDelete.setOnClickListener {
            closeMenu()
            showDeletePlaylistDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            if (playlist == null) {
                findNavController().navigateUp()
                return@observe
            }
            currentPlaylist = playlist
            renderPlaylistInfo(playlist)
        }

        viewModel.durationMinutes.observe(viewLifecycleOwner) { minutes ->
            binding.tvDuration.text = getString(R.string.playlist_duration_minutes, minutes)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            currentTracks = tracks
            tracksAdapter.updateData(tracks)
            binding.tvEmptyTracks.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
            binding.tracksRecycler.visibility = if (tracks.isEmpty()) View.GONE else View.VISIBLE
            binding.tvTracksCount.text = formatTracksCount(tracks.size)
            binding.menuCount.text = formatTracksCount(tracks.size)
        }

        viewModel.playlistDeleted.observe(viewLifecycleOwner) { name ->
            if (name.isNullOrBlank()) return@observe
            parentFragmentManager.setFragmentResult(
                PLAYLIST_DELETED_RESULT,
                bundleOf(PLAYLIST_DELETED_NAME to name)
            )
            viewModel.clearPlaylistDeleted()
            findNavController().navigateUp()
        }
    }

    private fun renderPlaylistInfo(playlist: PlaylistUI) {
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)
        val coverFile = playlist.coverPath?.let { File(it) }
        Glide.with(binding.ivCover)
            .load(coverFile)
            .transform(CenterCrop())
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .into(binding.ivCover)

        Glide.with(binding.menuCover)
            .load(coverFile)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .into(binding.menuCover)

        binding.tvName.text = playlist.name
        binding.menuTitle.text = playlist.name

        val description = playlist.description.orEmpty()
        if (description.isBlank()) {
            binding.tvDescription.visibility = View.GONE
        } else {
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvDescription.text = description
        }
        binding.actionsRow.doOnLayout {
            adjustTracksPeekHeight()
        }
    }

    private fun formatTracksCount(count: Int): String {
        return resources.getQuantityString(R.plurals.tracks_count, count, count)
    }

    private fun sharePlaylist() {
        val playlist = currentPlaylist ?: return
        if (currentTracks.isEmpty()) {
            showMessage(getString(R.string.playlist_share_empty))
            return
        }

        val text = buildShareText(playlist, currentTracks)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
    }

    private fun showMessage(message: String) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            rootView,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(R.drawable.snackbar_black_rounded)
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.second_background))
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_14))
            gravity = Gravity.CENTER
            maxLines = if (message.length > 40) 2 else 1
        }
        val params = snackbarView.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            val horizontal = resources.getDimensionPixelSize(R.dimen.snackbar_margin_horizontal)
            val bottom = resources.getDimensionPixelSize(R.dimen.snackbar_margin_bottom)
            params.setMargins(horizontal, params.topMargin, horizontal, bottom)
            snackbarView.layoutParams = params
        }
        if (message.length <= 40) {
            val snackbarHeight = resources.getDimensionPixelSize(R.dimen.snackbar_max_height)
            snackbarView.layoutParams.height = snackbarHeight
        }
        snackbar.show()
    }

    private fun buildShareText(playlist: PlaylistUI, tracks: List<TrackUI>): String {
        val builder = StringBuilder()
        builder.append(playlist.name).append('\n')
        if (!playlist.description.isNullOrBlank()) {
            builder.append(playlist.description).append('\n')
        }
        builder.append(formatTracksCount(tracks.size)).append('\n')
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

    private fun showDeleteTrackDialog(track: TrackUI) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_track_message)
            .setNegativeButton(R.string.delete_track_cancel, null)
            .setPositiveButton(R.string.delete_track_confirm) { _, _ ->
                viewModel.deleteTrack(track.trackId)
                showMessage(getString(R.string.track_deleted_message, track.trackName))
            }
            .create()
        dialog.setOnShowListener {
            val color = ContextCompat.getColor(requireContext(), R.color.blue)
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(color)
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)?.setTextColor(color)
        }
        dialog.show()
    }

    private fun showDeletePlaylistDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(R.string.delete_playlist_message)
            .setNegativeButton(R.string.delete_playlist_cancel, null)
            .setPositiveButton(R.string.delete_playlist_confirm) { _, _ ->
                val name = currentPlaylist?.name.orEmpty()
                viewModel.deletePlaylist(name)
            }
            .create()
        dialog.setOnShowListener {
            val color = ContextCompat.getColor(requireContext(), R.color.blue)
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(color)
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)?.setTextColor(color)
        }
        dialog.show()
    }

    private fun openMenu() {
        menuBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeMenu() {
        menuBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun openPlayer(track: TrackUI) {
        val action = PlaylistFragmentDirections.actionPlaylistFragmentToPlayerFragment(track)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::menuBottomSheet.isInitialized) {
            menuBottomSheet.removeBottomSheetCallback(menuBottomSheetCallback)
        }
        binding.actionsRow.removeOnLayoutChangeListener(actionsLayoutChangeListener)
        _binding = null
    }

    companion object {
        private const val MENU_HEIGHT_RATIO = 0.479f
        const val PLAYLIST_DELETED_RESULT = "playlist_deleted_result"
        const val PLAYLIST_DELETED_NAME = "playlist_deleted_name"
    }
}
