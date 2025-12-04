package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.FragmentProfileBinding
import com.example.plango.model.MemberProfileData
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(), NicknameEditDialogFragment.OnNicknameSavedListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 뒤로가기
        binding.layoutBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 🔹 닉네임 row 클릭 → 닉네임 변경 다이얼로그
        binding.rowNickname.setOnClickListener {
            val currentNickname = binding.tvProfileNickname.text.toString()
            NicknameEditDialogFragment
                .newInstance(currentNickname)
                .show(parentFragmentManager, "NicknameEditDialog")
        }

        // 🔹 프로필 로드
        loadProfileFromServer()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            showMainHeader(false)
            showAlarmIcon(false)
            showProfileButton(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showMainHeader(true)
        _binding = null
    }

    /**
     * 서버에서 받은 프로필 데이터를 UI에 바인딩
     */
    private fun bindProfile(profile: MemberProfileData) {
        // 이름 (name이 응답에 없다면 빈 문자열)
        binding.tvProfileName.text = profile.name ?: ""

        // 닉네임 / 이메일 / 아이디(loginId)
        binding.tvProfileNickname.text = profile.nickname
        binding.tvProfileEmail.text = profile.email
        binding.tvProfileId.text = profile.loginId

        // 카카오 계정 여부
        val isKakao = profile.loginType == "KAKAO"
        binding.tvKakaoBadge.visibility = if (isKakao) View.VISIBLE else View.GONE
        binding.rowChangePassword.visibility = if (isKakao) View.GONE else View.VISIBLE

        // 프로필 이미지
        if (!profile.profileImageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(profile.profileImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
    }

    /**
     * /api/members/{memberId} 호출해서 프로필 가져오기
     */
    private fun loadProfileFromServer() {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.getMemberProfile(memberId)

                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data

                    if (data != null) {
                        // 세션 업데이트
                        MemberSession.email = data.email
                        MemberSession.nickname = data.nickname
                        MemberSession.profileImageUrl = data.profileImageUrl
                        MemberSession.loginId = data.loginId
                        MemberSession.loginType = data.loginType

                        bindProfile(data)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 닉네임 변경 다이얼로그에서 저장 성공 시 콜백
     */
    override fun onNicknameSaved(newNickname: String) {
        binding.tvProfileNickname.text = newNickname
        // 이미 Dialog 쪽에서 MemberSession.nickname 업데이트 해줬지만,
        // 혹시 모를 경우를 위해 다시 한 번 맞춰줘도 됨.
        MemberSession.nickname = newNickname
    }
}
