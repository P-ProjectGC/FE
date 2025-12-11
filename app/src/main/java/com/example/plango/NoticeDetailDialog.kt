package com.example.plango

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.plango.databinding.DialogNoticeDetailBinding

class NoticeDetailDialog(
    private val title: String,
    private val content: String,
    private val date: String,
    private val type: String
) : DialogFragment() {

    private lateinit var binding: DialogNoticeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true   // ← 뒤로가기 버튼으로 닫힘 허용
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNoticeDetailBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvTitle.text = title
        binding.tvContent.text = content
        binding.tvDate.text = date

        val typeLabel = when (type) {
            "UPDATE" -> "업데이트"
            "EMERGENCY" -> "긴급"
            "ERROR" -> "오류"
            else -> type
        }
        binding.tvType.text = typeLabel

        val bgRes = when (type) {
            "UPDATE" -> R.drawable.bg_notice_type_update
            "EMERGENCY" -> R.drawable.bg_notice_type_urgent
            "ERROR" -> R.drawable.bg_notice_type_error
            else -> R.drawable.bg_notice_type_update
        }
        binding.tvType.setBackgroundResource(bgRes)

        binding.btnClose.setOnClickListener { dismiss() }

        return dialog
    }
}
