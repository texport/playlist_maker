package com.mybrain.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.view.Gravity
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivArtwork: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnAddToPlaylist: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var itemDuration: View
    private lateinit var itemAlbum: View
    private lateinit var itemYear: View
    private lateinit var itemGenre: View
    private lateinit var itemCountry: View
    private lateinit var overlay: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheetRecycler: RecyclerView
    private lateinit var bottomSheetNewPlaylist: View
    private val bottomSheetAdapter = PlayerPlaylistsAdapter { playlist ->
        viewModel.onPlaylistClicked(playlist)
    }

    private val args by navArgs<PlayerFragmentArgs>()

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(args.track)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initToolbar()
        initBottomSheet(view)
        bindStaticTrackInfo(args.track)
        observeViewModel()

        btnPlayPause.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }

        btnFavorite.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        btnAddToPlaylist.setOnClickListener {
            viewModel.loadPlaylists()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onScreenPaused()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.releasePlayer()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        ivArtwork = view.findViewById(R.id.ivArtwork)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvAuthor = view.findViewById(R.id.tvAuthor)
        tvProgress = view.findViewById(R.id.tvPreviewTime)
        btnPlayPause = view.findViewById(R.id.btnPlay)
        btnAddToPlaylist = view.findViewById(R.id.btnAddToPlaylist)
        btnFavorite = view.findViewById(R.id.btnFavorite)

        itemDuration = view.findViewById(R.id.itemDuration)
        itemAlbum = view.findViewById(R.id.itemAlbum)
        itemYear = view.findViewById(R.id.itemYear)
        itemGenre = view.findViewById(R.id.itemGenre)
        itemCountry = view.findViewById(R.id.itemCountry)
        overlay = view.findViewById(R.id.overlay)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initBottomSheet(view: View) {
        val bottomSheet = view.findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isFitToContents = false
            halfExpandedRatio = 2f / 3f
            skipCollapsed = true
        }

        bottomSheetRecycler = view.findViewById(R.id.bottom_sheet_playlists)
        bottomSheetRecycler.layoutManager = LinearLayoutManager(requireContext())
        bottomSheetRecycler.adapter = bottomSheetAdapter

        bottomSheetNewPlaylist = view.findViewById(R.id.bottom_sheet_new_playlist)
        bottomSheetNewPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                overlay.visibility =
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = slideOffset.coerceIn(0f, 1f)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            tvProgress.text = state.progress

            btnPlayPause.isEnabled = state.isPlayButtonEnabled
            btnPlayPause.setBackgroundResource(
                if (state.isPlaying) R.drawable.ic_pause_button_100
                else R.drawable.ic_play_button_100
            )

            btnFavorite.setBackgroundResource(
                if (state.isFavorite) R.drawable.ic_like_button_active_51
                else R.drawable.ic_like_button_51
            )
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.submitList(playlists)
        }

        viewModel.addToPlaylistState.observe(viewLifecycleOwner) { state ->
            if (state == null) return@observe

            when (state) {
                is AddToPlaylistState.Added -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    showPlaylistMessage(getString(R.string.playlist_added, state.playlistName))
                }
                is AddToPlaylistState.AlreadyAdded -> {
                    showPlaylistMessage(getString(R.string.playlist_already_added, state.playlistName))
                }
            }

            viewModel.resetAddToPlaylistState()
        }
    }

    private fun showPlaylistMessage(message: String) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            rootView,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(R.drawable.snackbar_black_rounded)
        snackbar.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                R.color.second_background
            )
        )
        val snackbarHeight = resources.getDimensionPixelSize(R.dimen.snackbar_max_height)
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
        snackbar.show()
    }

    private fun bindStaticTrackInfo(track: TrackUI) {
        tvTitle.text = track.trackName
        tvAuthor.text = track.artistName

        val bigArtworkUrl = track.artworkUrl100.replace("100x100bb", "512x512bb")
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)

        Glide.with(requireContext()).clear(ivArtwork)
        ivArtwork.setImageResource(R.drawable.placeholder_track)
        Glide.with(requireContext())
            .load(bigArtworkUrl)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .thumbnail(0.25f)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(ivArtwork)

        itemDuration.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_duration)
        itemAlbum.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_album)
        itemYear.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_year)
        itemGenre.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_genre)
        itemCountry.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_country)

        bindInfoRow(
            rowView = itemDuration,
            value = Utils.formatTime(track.trackTime.toInt())
        )
        bindInfoRow(itemAlbum, track.collectionName)
        bindInfoRow(itemYear, extractYear(track.releaseDate))
        bindInfoRow(itemGenre, track.primaryGenreName)
        bindInfoRow(itemCountry, track.country)
    }

    private fun bindInfoRow(rowView: View, value: String?) {
        val tvValue = rowView.findViewById<TextView>(R.id.tvValue)
        if (value.isNullOrBlank()) {
            rowView.visibility = View.GONE
        } else {
            rowView.visibility = View.VISIBLE
            tvValue.text = value
        }
    }

    private fun extractYear(releaseDate: String?): String? {
        if (releaseDate.isNullOrBlank()) return null
        return if (releaseDate.length >= 4 && releaseDate[0].isDigit()) {
            releaseDate.substring(0, 4)
        } else {
            null
        }
    }
}
