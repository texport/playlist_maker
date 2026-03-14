package com.mybrain.playlistmaker.presentation.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.res.ColorStateList
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.databinding.FragmentCreatePlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import java.io.File

class CreatePlaylistFragment : Fragment() {

    private val viewModel by viewModel<CreatePlaylistViewModel>()

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private var selectedCoverUri: Uri? = null
    private val args by navArgs<CreatePlaylistFragmentArgs>()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedCoverUri = uri
                renderCover()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreState(savedInstanceState)
        updateCreateButtonState()
        setupHintStyles()
        updateHintColors()
        setupEditMode(savedInstanceState == null)

        binding.toolbar.setNavigationOnClickListener {
            handleClose()
        }

        binding.ivCover.setOnClickListener { viewModel.onPickCoverClicked() }

        binding.contentContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }

        binding.scrollContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }

        binding.etName.doAfterTextChanged {
            updateCreateButtonState()
            updateHintColor(binding.tilName, it.isNullOrBlank())
        }

        binding.etDescription.doAfterTextChanged {
            updateHintColor(binding.tilDescription, it.isNullOrBlank())
        }

        binding.etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etDescription.requestFocus()
                true
            } else {
                false
            }
        }

        binding.etDescription.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.etName.setOnFocusChangeListener { _, _ ->
            updateHintColor(binding.tilName, binding.etName.text.isNullOrBlank())
        }

        binding.etDescription.setOnFocusChangeListener { _, _ ->
            updateHintColor(binding.tilDescription, binding.etDescription.text.isNullOrBlank())
        }

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim().ifBlank { null }
            val cover = selectedCoverUri?.toString()
            if (isEditMode()) {
                viewModel.updatePlaylist(args.playlistId, name, description, cover)
            } else {
                viewModel.createPlaylist(name, description, cover)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreatePlaylistState.Created -> {
                    if (!isEditMode()) {
                        parentFragmentManager.setFragmentResult(
                            PLAYLIST_CREATED_RESULT,
                            bundleOf(PLAYLIST_NAME_KEY to state.playlistName)
                        )
                    }
                    viewModel.resetState()
                    findNavController().navigateUp()
                }
                CreatePlaylistState.Updated -> {
                    viewModel.resetState()
                    findNavController().navigateUp()
                }
                CreatePlaylistState.Idle -> Unit
            }
        }

        viewModel.editPlaylist.observe(viewLifecycleOwner) { playlist ->
            if (playlist == null || !isEditMode()) return@observe
            binding.etName.setText(playlist.name)
            binding.etDescription.setText(playlist.description.orEmpty())
            selectedCoverUri = playlist.coverPath?.let { toCoverUri(it) }
            renderCover()
            updateCreateButtonState()
            updateHintColors()
        }

        viewModel.permissionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PermissionUiState.Granted -> {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    viewModel.resetPermissionState()
                }
                PermissionUiState.NeedsRationale -> showPermissionRationale()
                PermissionUiState.DeniedPermanently -> showPermissionSettingsDialog()
                PermissionUiState.Denied -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetPermissionState()
                }
                PermissionUiState.Idle -> Unit
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleClose()
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_NAME, binding.etName.text.toString())
        outState.putString(KEY_DESCRIPTION, binding.etDescription.text.toString())
        outState.putString(KEY_COVER_URI, selectedCoverUri?.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        binding.etName.setText(savedInstanceState.getString(KEY_NAME, ""))
        binding.etDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION, ""))
        val cover = savedInstanceState.getString(KEY_COVER_URI)
        if (!cover.isNullOrBlank()) {
            selectedCoverUri = toCoverUri(cover)
            renderCover()
        }
    }

    private fun renderCover() {
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)
        val cover = selectedCoverUri
        if (cover == null) {
            binding.ivCover.scaleType = android.widget.ImageView.ScaleType.CENTER
            binding.ivCover.setBackgroundResource(R.drawable.playlist_cover_dashed_background)
            val padding = resources.getDimensionPixelSize(R.dimen.fab_margin)
            binding.ivCover.setPadding(padding, padding, padding, padding)
            binding.ivCover.setImageResource(R.drawable.placeholder_photo_100)
            return
        }

        binding.ivCover.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        binding.ivCover.background = null
        binding.ivCover.setPadding(0, 0, 0, 0)
        Glide.with(binding.ivCover)
            .load(cover)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_photo_100)
            .error(R.drawable.placeholder_photo_100)
            .fallback(R.drawable.placeholder_photo_100)
            .into(binding.ivCover)
    }

    private fun updateCreateButtonState() {
        val isEnabled = binding.etName.text.toString().isNotBlank()
        binding.btnCreate.isEnabled = isEnabled
        binding.btnCreate.alpha = if (isEnabled) 1f else 0.5f
    }

    private fun setupHintStyles() {
        binding.tilName.setHintTextAppearance(R.style.CreatePlaylist_Hint_Collapsed)
        binding.tilDescription.setHintTextAppearance(R.style.CreatePlaylist_Hint_Collapsed)
        val expandedColor =
            android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.main_text_color)
            )
        binding.tilName.setDefaultHintTextColor(expandedColor)
        binding.tilDescription.setDefaultHintTextColor(expandedColor)
    }

    private fun updateHintColors() {
        updateHintColor(binding.tilName, binding.etName.text.isNullOrBlank())
        updateHintColor(binding.tilDescription, binding.etDescription.text.isNullOrBlank())
    }

    private fun updateHintColor(inputLayout: com.google.android.material.textfield.TextInputLayout, isEmpty: Boolean) {
        if (isEmpty) {
            val stateList = createExpandedHintColorStateList()
            inputLayout.setDefaultHintTextColor(stateList)
            inputLayout.hintTextColor = stateList
        } else {
            val gray = ContextCompat.getColor(requireContext(), R.color.create_playlist_text_edit_color)
            val colorState = ColorStateList.valueOf(gray)
            inputLayout.setDefaultHintTextColor(colorState)
            inputLayout.hintTextColor = colorState
        }
    }

    private fun createExpandedHintColorStateList(): ColorStateList {
        val mainColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val focusedColor = ContextCompat.getColor(requireContext(), R.color.create_playlist_text_edit_color)
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf()
            ),
            intArrayOf(
                focusedColor,
                mainColor
            )
        )
    }

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
        binding.root.requestFocus()
    }

    private fun handleClose() {
        if (isEditMode()) {
            findNavController().navigateUp()
            return
        }
        if (hasUnsavedChanges()) {
            showExitDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_rationale)
            .setNegativeButton(R.string.permission_cancel) { _, _ ->
                viewModel.resetPermissionState()
            }
            .setPositiveButton(R.string.permission_allow) { _, _ ->
                viewModel.resetPermissionState()
                viewModel.onPickCoverClicked()
            }
            .setOnDismissListener { viewModel.resetPermissionState() }
            .show()
    }

    private fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_settings_message)
            .setNegativeButton(R.string.permission_cancel) { _, _ ->
                viewModel.resetPermissionState()
            }
            .setPositiveButton(R.string.permission_settings) { _, _ ->
                viewModel.resetPermissionState()
                openAppSettings()
            }
            .setOnDismissListener { viewModel.resetPermissionState() }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }

    private fun hasUnsavedChanges(): Boolean {
        return binding.etName.text.toString().isNotBlank() ||
            binding.etDescription.text.toString().isNotBlank() ||
            selectedCoverUri != null
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exit_create_playlist_title)
            .setMessage(R.string.exit_create_playlist_message)
            .setNegativeButton(R.string.exit_create_playlist_cancel, null)
            .setPositiveButton(R.string.exit_create_playlist_confirm) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun toCoverUri(value: String): Uri {
        return if (value.contains("://")) {
            value.toUri()
        } else {
            File(value).toUri()
        }
    }

    companion object {
        private const val KEY_NAME = "playlist_name"
        private const val KEY_DESCRIPTION = "playlist_description"
        private const val KEY_COVER_URI = "playlist_cover_uri"
        const val PLAYLIST_CREATED_RESULT = "playlist_created_result"
        const val PLAYLIST_NAME_KEY = "playlist_name_key"
    }

    private fun setupEditMode(loadData: Boolean) {
        if (!isEditMode()) return
        binding.toolbar.setTitle(R.string.edit_playlist_title)
        binding.btnCreate.setText(R.string.save_playlist_button)
        if (loadData) {
            viewModel.loadPlaylistForEdit(args.playlistId)
        }
    }

    private fun isEditMode(): Boolean = args.playlistId > 0
}
