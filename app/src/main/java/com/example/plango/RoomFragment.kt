package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.plango.databinding.FragmentRoomBinding

class RoomFragment : Fragment() {
    lateinit var binding: FragmentRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoomBinding.inflate(inflater, container, false)

        return binding.root
    }
}