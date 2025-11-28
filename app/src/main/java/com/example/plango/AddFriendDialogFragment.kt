package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.FriendSearchResultAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendSearchRepository
import com.example.plango.databinding.DialogAddFriendBinding
import com.example.plango.model.Friend

class AddFriendDialogFragment : DialogFragment() {

    private var _binding: DialogAddFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: FriendSearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initUi()
        initClickListeners()
    }

    private fun initRecyclerView() {
        searchAdapter = FriendSearchResultAdapter { friend ->
            onAddFriendClicked(friend)
        }

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    private fun initUi() {
        // 처음에는 결과 리스트/빈 상태 모두 숨김
        binding.rvSearchResult.isVisible = false
        binding.tvEmptyResult.isVisible = false
    }

    private fun initClickListeners() {
        // 닫기 버튼
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // 검색 버튼
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // 키보드 검색 액션
        binding.etNickname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    /** 닉네임 검색 수행 (부분 일치, 여러 명 결과 가능) */
    private fun performSearch() {
        val keyword = binding.etNickname.text.toString().trim()

        if (keyword.isEmpty()) {
            Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // FriendSearchRepository에서 검색 (List<Friend> 반환하도록 구현)
        val results: List<Friend> = FriendSearchRepository.searchByNickname(keyword)

        if (results.isEmpty()) {
            // 검색 결과 없음
            searchAdapter.submitList(emptyList())
            binding.rvSearchResult.isVisible = false
            binding.tvEmptyResult.isVisible = true
        } else {
            // 검색 결과 리스트 출력
            binding.tvEmptyResult.isVisible = false
            binding.rvSearchResult.isVisible = true
            searchAdapter.submitList(results)
        }
    }

    /** 각 아이템의 "추가" 버튼 클릭 시 */
    private fun onAddFriendClicked(friend: Friend) {
        // 이미 친구인지 체크
        val currentFriends = FriendRepository.getFriends()
        if (currentFriends.contains(friend)) {
            Toast.makeText(requireContext(), "이미 친구인 사용자입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 지금은 서버 없는 단계라 단순히 UI 피드백만
        Toast.makeText(requireContext(), "친구 요청을 보냈습니다. (더미)", Toast.LENGTH_SHORT).show()

        dismiss()
    }

    override fun onStart() {
        super.onStart()
        // 팝업 바깥 배경을 투명하게 — 이게 핵심!
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 팝업 너비를 적당히 (친구요청 팝업과 동일하게)
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddFriendDialog"
    }
}
