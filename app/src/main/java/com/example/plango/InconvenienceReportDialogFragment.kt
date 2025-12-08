package com.example.plango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.DialogInconvenienceReportBinding
import com.example.plango.model.InconvenienceReportRequest
import kotlinx.coroutines.launch

class InconvenienceReportDialogFragment : DialogFragment() {

    private var _binding: DialogInconvenienceReportBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = InconvenienceReportDialogFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogInconvenienceReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 / 취소
        binding.tvClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // 🔥 버튼 tint 완전 제거 + 초기 상태 비활성(회색)
        binding.btnSubmit.backgroundTintList = null
        setSubmitButtonEnabled(false)

        // 🔥 입력 감지 → 회색/검정 토글
        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrBlank()
                setSubmitButtonEnabled(hasText)
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) { }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) { }
        })

        // 신고 버튼 클릭
        binding.btnSubmit.setOnClickListener {
            val content = binding.etContent.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "불편사항을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitReport(content)
        }
    }

    /**
     * 버튼 상태 토글
     *  - enabled=false : 연회색 배경
     *  - enabled=true  : 검정 배경
     */
    private fun setSubmitButtonEnabled(enabled: Boolean) {
        // 여기서도 tint 한 번 더 끊어줌 (혹시 모를 재적용 방지)
        binding.btnSubmit.backgroundTintList = null

        if (enabled) {
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_report_submit_enabled)
            binding.btnSubmit.setTextColor(Color.WHITE)
        } else {
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_report_submit_disabled)
            binding.btnSubmit.setTextColor(Color.WHITE)
        }
    }

    /**
     * 불편사항 신고 API 호출
     */
    private fun submitReport(content: String) {
        binding.btnSubmit.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = InconvenienceReportRequest(content)
                val response =
                    RetrofitClient.reportApiService.submitInconvenienceReport(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 0) {
                        Toast.makeText(
                            requireContext(),
                            "불편사항이 접수되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "불편사항 접수에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "불편사항 접수 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "불편사항 접수 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // 현재 내용 여부에 따라 다시 enable/disable
                val hasText = !binding.etContent.text.isNullOrBlank()
                setSubmitButtonEnabled(hasText)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
