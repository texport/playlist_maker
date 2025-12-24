package com.mybrain.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mybrain.playlistmaker.databinding.FragmentFavoriteTracksBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<FavoriteTracksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}
