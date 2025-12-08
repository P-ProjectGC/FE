package com.example.plango

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.plango.databinding.DialogFriendDeleteBinding

class FriendDeleteDialogFragment(
    private val onConfirmDelete: () -> Unit
) : DialogFragment() {

    private var _binding: DialogFriendDeleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFriendDeleteBinding.inflate(LayoutInflater.from(context))

        // 닫기 버튼
        binding.btnClose.setOnClickListener { dismiss() }

        // 취소 버튼
        binding.btnCancel.setOnClickListener { dismiss() }

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {
            onConfirmDelete()     // 삭제 실행
            dismiss()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}