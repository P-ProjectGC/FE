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
import com.example.plango.model.NotificationSettings
import com.example.plango.model.NotificationSettingsUpdateRequest
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

    // 🔔 알림 스위치 UI 업데이트 중인지 플래그 (서버 값 반영할 때 리스너 막기)
    private var isNotificationUiUpdating: Boolean = false

    // 🔔 마지막으로 서버에서 받은 알림 설정 (실패 시 롤백용)
    private var lastNotificationSettings: NotificationSettings? = null

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
        binding.btnBack.setOnClickListener {
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

        // 🔹 🔔 공지사항 row 클릭 → NoticeListActivity 이동 (★ 요거 추가 ★)
        binding.rowNotice.setOnClickListener {
            val intent = android.content.Intent(requireContext(), NoticeListActivity::class.java)
            startActivity(intent)
        }

        binding.rowReport.setOnClickListener {
            InconvenienceReportDialogFragment
                .newInstance()
                .show(parentFragmentManager, "InconvenienceReportDialog")
        }




        // 🔹 로그인 타입에 따른 UI 적용 (카카오 뱃지, 비밀번호 변경 row 노출 등)
        applyLoginTypeFromSession()

        // 🔹 세션에 저장된 프로필 이미지 먼저 적용
        loadProfileImage(MemberSession.profileImageUrl)

        // 🔹 서버에서 프로필 로드
        loadProfileFromServer()

        // 🔔 알림 스위치 리스너 설정
        setupNotificationSwitches()

        // 🔔 서버에서 알림 설정 불러오기
        loadNotificationSettingsFromServer()
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

        android.util.Log.d("PROFILE_IMAGE", "raw from server = ${profile.profileImageUrl}")

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
                        val oldUrl = MemberSession.profileImageUrl

                        // 세션 업데이트
                        MemberSession.email = data.email
                        MemberSession.nickname = data.nickname
                        MemberSession.profileImageUrl = data.profileImageUrl
                        MemberSession.loginId = data.loginId
                        MemberSession.loginType = data.loginType

                        // UI 텍스트/로그인 타입 바인딩
                        bindProfile(data)

                        // 이미지 URL이 바뀐 경우에만 다시 로드
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
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    Toast.makeText(requireContext(), "이미지 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "profile_${MemberSession.currentMemberId}.jpg",
                    body = requestBody
                )

                val response = RetrofitClient.fileApiService.uploadFile(part)

                if (response.isSuccessful) {
                    val body = response.body()
                    val uploadData = body?.data

                    if (body?.code == 0 && uploadData != null) {
                        val fileUrl = uploadData.fileUrl

                        MemberSession.profileImageUrl = fileUrl
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
     */
    private fun updateProfileImageOnServer(fileUrl: String) {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentNickname = MemberSession.nickname
            ?: binding.tvProfileNickname.text.toString()
                .takeIf { it.isNotBlank() }

        if (currentNickname.isNullOrBlank()) {
            Toast.makeText(requireContext(), "닉네임 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            nickname = currentNickname,
            profileImageUrl = fileUrl
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.updateProfile(memberId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 0) {
                        MemberSession.profileImageUrl = fileUrl
                        Toast.makeText(requireContext(), "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        // 아이콘 재로드
                        (activity as? MainActivity)?.refreshProfileIcon()
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
            binding.ivProfileImage.setImageResource(R.drawable.profile_basic)
            return
        }

        val imageUrl = if (path.startsWith("http")) {
            path
        } else {
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

    // ─────────────────────────────
    // 🔔 알림 설정 관련
    // ─────────────────────────────

    /**
     * 🔔 세 개 스위치에 공통 리스너 달기
     */
    private fun setupNotificationSwitches() {
        binding.swAlarmAllChat.setOnCheckedChangeListener { _, _ ->
            onNotificationSwitchChanged()
        }
        binding.swAlarmSchedule.setOnCheckedChangeListener { _, _ ->
            onNotificationSwitchChanged()
        }
        binding.swAlarmFriend.setOnCheckedChangeListener { _, _ ->
            onNotificationSwitchChanged()
        }
    }

    /**
     * 🔔 아무 스위치나 바뀌었을 때 호출되는 공통 처리
     */
    private fun onNotificationSwitchChanged() {
        // 서버 값 적용 중이면 PATCH 안 보냄
        if (isNotificationUiUpdating) return

        val allChatOn = binding.swAlarmAllChat.isChecked
        val tripReminderOn = binding.swAlarmSchedule.isChecked
        val friendReqOn = binding.swAlarmFriend.isChecked

        updateNotificationSettingsOnServer(
            allChatRoomEnabled = allChatOn,
            tripReminderEnabled = tripReminderOn,
            friendRequestEnabled = friendReqOn
        )
    }

    /**
     * 🔔 GET /api/v1/members/me/notifications
     *     현재 로그인 유저의 알림 설정 조회
     */
    private fun loadNotificationSettingsFromServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.getNotificationSettings()

                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data

                    android.util.Log.d("Notification", "GET settings = $body")

                    if (body?.code == 0 && data != null) {
                        lastNotificationSettings = data
                        applyNotificationSettingsToUi(data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "알림 설정을 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val msg = when (response.code()) {
                        401 -> "로그인 정보가 만료되었습니다. 다시 로그인해주세요."
                        404 -> "알림 설정 정보를 찾을 수 없습니다."
                        else -> "알림 설정 조회 실패 (${response.code()})"
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "알림 설정을 불러오는 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 🔔 서버에서 받은 값으로 스위치 상태 반영
     */
    private fun applyNotificationSettingsToUi(settings: NotificationSettings) {
        isNotificationUiUpdating = true

        binding.swAlarmAllChat.isChecked = settings.allChatRoomEnabled
        binding.swAlarmSchedule.isChecked = settings.tripReminderEnabled
        binding.swAlarmFriend.isChecked = settings.friendRequestEnabled

        isNotificationUiUpdating = false
    }

    /**
     * 🔔 PATCH /api/v1/members/me/notifications
     *     세 가지 알림 설정을 한 번에 서버에 반영
     */
    private fun updateNotificationSettingsOnServer(
        allChatRoomEnabled: Boolean,
        tripReminderEnabled: Boolean,
        friendRequestEnabled: Boolean
    ) {
        val request = NotificationSettingsUpdateRequest(
            allChatRoomEnabled = allChatRoomEnabled,
            tripReminderEnabled = tripReminderEnabled,
            friendRequestEnabled = friendRequestEnabled
        )

        android.util.Log.d("Notification", "PATCH request = $request")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response =
                    RetrofitClient.memberApiService.updateNotificationSettings(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data

                    android.util.Log.d("Notification", "PATCH response = $body")

                    if (body?.code == 0 && data != null) {
                        // 서버 반영 성공 → 로컬 상태 동기화
                        lastNotificationSettings = data
                        applyNotificationSettingsToUi(data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "알림 설정 저장에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 실패 시 이전 값으로 롤백
                        lastNotificationSettings?.let { applyNotificationSettingsToUi(it) }
                    }
                } else {
                    val msg = when (response.code()) {
                        400 -> "알림 설정 값이 올바르지 않습니다."
                        401 -> "로그인 정보가 만료되었습니다. 다시 로그인해주세요."
                        else -> "알림 설정 저장 실패 (${response.code()})"
                    }

                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                    // HTTP 에러 시에도 이전 값으로 되돌리기
                    lastNotificationSettings?.let { applyNotificationSettingsToUi(it) }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "알림 설정 저장 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                // 네트워크 예외 시 롤백
                lastNotificationSettings?.let { applyNotificationSettingsToUi(it) }
            }
        }
    }
}
