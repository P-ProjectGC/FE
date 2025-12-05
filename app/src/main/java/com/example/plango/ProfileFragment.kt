package com.example.plango

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.FragmentProfileBinding
import com.example.plango.model.MemberProfileData
import com.example.plango.model.ProfileUpdateRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileFragment : Fragment(), NicknameEditDialogFragment.OnNicknameSavedListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 🔹 선택된 프로필 이미지 Uri (로컬)
    private var selectedProfileImageUri: Uri? = null

    // 🔹 갤러리에서 이미지 선택 런처
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedProfileImageUri = uri

                // 1) 바로 화면에 미리보기
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfileImage)

                // 2) 서버에 업로드 → fileUrl 얻기 → 프로필 PATCH
                uploadProfileImageToServer(uri)
            }
        }

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

        // 🔹 프로필 이미지 클릭 → 갤러리 열기
        binding.ivProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 🔹 닉네임 row 클릭 → 닉네임 변경 다이얼로그
        binding.rowNickname.setOnClickListener {
            val currentNickname = binding.tvProfileNickname.text.toString()
            NicknameEditDialogFragment
                .newInstance(currentNickname)
                .show(parentFragmentManager, "NicknameEditDialog")
        }

        // 🔹 비밀번호 변경 row 클릭 → 비밀번호 변경 다이얼로그
        binding.rowChangePassword.setOnClickListener {
            ChangePasswordDialogFragment.newInstance()
                .show(parentFragmentManager, "ChangePasswordDialog")
        }

        // 🔹 로그아웃 row 클릭
        binding.rowLogout.setOnClickListener {
            LogoutDialogFragment.newInstance()
                .show(parentFragmentManager, "LogoutDialog")
        }

        // 🔹 회원탈퇴 row 클릭
        binding.rowWithdraw.setOnClickListener {
            MemberWithdrawDialogFragment.newInstance()
                .show(parentFragmentManager, "MemberWithdrawDialog")
        }

        //세션에 저장된 프로필 정보 먼저 적용
        applyLoginTypeFromSession()

        // 🔹 세션에 저장된 프로필 이미지 먼저 적용
        loadProfileImage(MemberSession.profileImageUrl)

        // 🔹 서버에서 프로필 로드
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
     * ✅ 서버에서 받은 프로필 데이터를 UI에 바인딩
     */
    private fun bindProfile(profile: MemberProfileData) {

        binding.tvProfileName.text = profile.name ?: ""
        binding.tvProfileNickname.text = profile.nickname
        binding.tvProfileEmail.text = profile.email
        binding.tvProfileId.text = profile.loginId

        val isKakao = profile.loginType == "KAKAO"
        binding.tvKakaoBadge.visibility = if (isKakao) View.VISIBLE else View.GONE
        binding.rowChangePassword.visibility = if (isKakao) View.GONE else View.VISIBLE

        // 이미지 로그 찍는 건 남겨도 되고 빼도 됨
        android.util.Log.d("PROFILE_IMAGE", "raw from server = ${profile.profileImageUrl}")

        // ✅ 여기서도 공통 함수만 호출
        loadProfileImage(profile.profileImageUrl)
    }



    /**
     * ✅ /api/members/{memberId} 호출해서 프로필 가져오기
     */
    private fun loadProfileFromServer() {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.getMemberProfile(memberId)

                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("Profile", "GET profile response = $body")
                    val data = body?.data

                    if (data != null) {
                        // 이전 URL 저장
                        val oldUrl = MemberSession.profileImageUrl

                        // 세션 업데이트
                        MemberSession.email = data.email
                        MemberSession.nickname = data.nickname
                        MemberSession.profileImageUrl = data.profileImageUrl
                        MemberSession.loginId = data.loginId
                        MemberSession.loginType = data.loginType

                        // 텍스트/로그인 타입 바인딩
                        bindProfile(data)

                        // 🔥 이미지 URL이 바뀐 경우에만 다시 로드
                        if (oldUrl != data.profileImageUrl) {
                            loadProfileImage(data.profileImageUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * ✅ 갤러리에서 고른 Uri → /api/files/upload 로 업로드
     * 성공 시 fileUrl 받아서 updateProfileImageOnServer 호출
     */
    private fun uploadProfileImageToServer(uri: Uri) {
        val context = requireContext().applicationContext

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1) Uri → ByteArray 로 읽기
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    Toast.makeText(requireContext(), "이미지 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2) RequestBody & Multipart 파트 만들기
                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(
                    name = "file",                // 서버에서 받는 필드 이름
                    filename = "profile_${MemberSession.currentMemberId}.jpg",
                    body = requestBody
                )

                // 3) 업로드 API 호출
                val response = RetrofitClient.fileApiService.uploadFile(part)

                if (response.isSuccessful) {
                    val body = response.body()
                    val uploadData = body?.data

                    if (body?.code == 0 && uploadData != null) {
                        val fileUrl = uploadData.fileUrl

                        // 세션에 최신 이미지 URL 저장
                        MemberSession.profileImageUrl = fileUrl

                        // 4) 프로필 PATCH로 이미지 URL 저장
                        updateProfileImageOnServer(fileUrl)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "이미지 업로드 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "이미지 업로드 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "이미지 업로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ✅ fileUrl 을 프로필에 반영 (PATCH /api/members/{memberId})
     * 닉네임은 변경 안 하므로 null
     */
    private fun updateProfileImageOnServer(fileUrl: String) {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 현재 닉네임 가져오기 (세션 → 없으면 화면에서)
        val currentNickname = MemberSession.nickname
            ?: binding.tvProfileNickname.text.toString()
                .takeIf { it.isNotBlank() }

        if (currentNickname.isNullOrBlank()) {
            Toast.makeText(requireContext(), "닉네임 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            nickname = currentNickname,   // ⭐️ 서버가 필수로 요구
            profileImageUrl = fileUrl     // 새로 업로드된 이미지 URL
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.updateProfile(memberId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 0) {
                        // 세션에도 최신 이미지 반영
                        MemberSession.profileImageUrl = fileUrl
                        Toast.makeText(requireContext(), "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "프로필 이미지 변경 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "프로필 이미지 변경 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "프로필 이미지 변경 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * ✅ 닉네임 변경 다이얼로그에서 저장 성공 시 콜백
     */
    override fun onNicknameSaved(newNickname: String) {
        binding.tvProfileNickname.text = newNickname
        MemberSession.nickname = newNickname
    }

    private fun applyLoginTypeFromSession() {
        val isKakao = MemberSession.loginType == "KAKAO"

        binding.tvKakaoBadge.visibility = if (isKakao) View.VISIBLE else View.GONE
        binding.rowChangePassword.visibility = if (isKakao) View.GONE else View.VISIBLE
    }

    private fun loadProfileImage(path: String?) {
        if (path.isNullOrBlank()) {
            // 아무 것도 없으면 기본 이미지
            binding.ivProfileImage.setImageResource(R.drawable.profile_basic)
            return
        }

        // 서버에서 준 값이 "uploads/xxx.jpg" 같은 상대 경로 (S3 object key)
        val imageUrl = if (path.startsWith("http")) {
            path
        } else {
            // ✅ 이제는 API BASE_URL이 아니라 S3 IMAGE_BASE_URL 사용
            RetrofitClient.IMAGE_BASE_URL + path
        }

        android.util.Log.d("PROFILE_IMAGE", "path=$path, finalUrl=$imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.profile_basic)
            .error(R.drawable.profile_basic)
            .into(binding.ivProfileImage)
    }






}
