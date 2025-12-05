// app/src/main/java/com/example/plango/RoomMenuDialogFragment.kt
package com.example.plango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.plango.data.RetrofitClient
import com.example.plango.model.RoomMemberDetail    // ⭐ 이 import 꼭 있어야 함
import com.bumptech.glide.Glide

class RoomMenuDialogFragment : DialogFragment() {

    private var roomId: Long = -1L
    private var roomName: String = ""
    private var memberNicknames: List<String> = emptyList()   // 원래 쓰던 것 그대로 유지
    private var images: List<Uri> = emptyList()




    // ⭐ Activity가 상세조회 응답에서 직접 넣어주는 실제 멤버 리스트
    private var members: List<RoomMemberDetail> = emptyList()

    fun setMembers(list: List<RoomMemberDetail>) {
        members = list
    }

    // ⭐ 방장 위임 콜백 (Activity에서 delegateHostTo(...) 연결)
    private var onTransferHostListener: ((Long, String) -> Unit)? = null

    fun setOnTransferHostListener(listener: (Long, String) -> Unit) {
        onTransferHostListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            roomId = args.getLong(ARG_ROOM_ID, -1L)
            roomName = args.getString(ARG_ROOM_NAME, "") ?: ""

            memberNicknames =
                args.getStringArrayList(ARG_MEMBER_NICKNAMES)?.toList() ?: emptyList()

            val imageStrs = args.getStringArrayList(ARG_IMAGE_URIS) ?: arrayListOf()
            images = imageStrs.map { Uri.parse(it) }
        } else {
            roomId = -1L
            roomName = ""
            memberNicknames = emptyList()
            images = emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_room_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseMenu)
        val tvTitle = view.findViewById<TextView>(R.id.tvMenuTitle)
        val tvMemberTitle = view.findViewById<TextView>(R.id.tvMenuMemberTitle)

        val switchAlarm = view.findViewById<Switch>(R.id.switchAlarm)
        val layoutImageSection = view.findViewById<View>(R.id.layoutImageSection)
        val tvImageCount = view.findViewById<TextView>(R.id.tvImageCount)
        val ivImagePreview = view.findViewById<ImageView>(R.id.ivImagePreview)
        val memberListLayout = view.findViewById<LinearLayout>(R.id.layoutMemberList)

        // 상단 제목
        tvTitle.text = "메뉴"

        // 참여자 수: members가 있으면 그걸 기준, 없으면 기존 닉네임 리스트 기준
        val memberCount = if (members.isNotEmpty()) members.size else memberNicknames.size
        tvMemberTitle.text = "참여자 목록 (${memberCount}명)"

        // 이미지 섹션
        if (images.isEmpty()) {
            layoutImageSection.visibility = View.VISIBLE
            tvImageCount.text = "(0개)"
            ivImagePreview.setImageDrawable(null)
        } else {
            layoutImageSection.visibility = View.VISIBLE
            tvImageCount.text = "(${images.size}개)"

            val lastUri = images.last()
            try {
                ivImagePreview.setImageURI(lastUri)
            } catch (e: SecurityException) {
                layoutImageSection.visibility = View.GONE
            }
        }

        // 참여자 리스트
        memberListLayout.removeAllViews()

        // ⭐ 실제 멤버 리스트가 있으면 그걸 우선 사용, 없으면 예전 방식 fallback
        val displayMembers: List<RoomMemberDetail> =
            if (members.isNotEmpty()) {
                members
            } else {
                memberNicknames.mapIndexed { index, name ->
                    RoomMemberDetail(
                        memberId = -1L,          // 실제 ID 모를 때는 -1
                        nickname = name,
                        profileImageUrl = null,
                        host = (index == 0)      // 예전처럼 첫 번째를 임시 방장으로
                    )
                }
            }

        displayMembers.forEach { member ->
            val itemView = layoutInflater.inflate(
                R.layout.item_room_member,
                memberListLayout,
                false
            )

            val ivProfile = itemView.findViewById<ImageView>(R.id.ivMemberProfile)
            val tvNickname = itemView.findViewById<TextView>(R.id.tvMemberNickname)
            val tvRealName = itemView.findViewById<TextView>(R.id.tvMemberRealName)
            val tvHostBadge = itemView.findViewById<TextView>(R.id.tvHostBadge)
            val btnTransfer = itemView.findViewById<Button>(R.id.btnTransferHost)

            tvNickname.text = member.nickname
            tvRealName.visibility = View.GONE

            val isHost = member.host
            tvHostBadge.visibility = if (isHost) View.VISIBLE else View.GONE
            btnTransfer.visibility = if (isHost) View.GONE else View.VISIBLE

            // 🔹 프로필 이미지 로딩
            val fullUrl = buildFullImageUrl(member.profileImageUrl)

            if (fullUrl == null) {
                // 이미지 없으면 기본 아이콘
                ivProfile.setImageResource(R.drawable.profile_basic)
            } else {
                Glide.with(requireContext())
                    .load(fullUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_basic)
                    .error(R.drawable.profile_basic)
                    .into(ivProfile)
            }


            btnTransfer.setOnClickListener {
                // 실제 멤버 id가 -1이 아니면 위임 콜백 호출
                if (member.memberId != -1L) {
                    onTransferHostListener?.invoke(member.memberId, member.nickname)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "${member.nickname}님의 ID 정보를 알 수 없어 위임할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dismiss()
            }

            memberListLayout.addView(itemView)
        }

        btnClose.setOnClickListener { dismiss() }

        // 🔔 알림 스위치: 방별 on/off 설정과 연결
        val isEnabled = NotificationPrefs.isChatNotificationEnabled(requireContext(), roomId)
        switchAlarm.isChecked = isEnabled

        switchAlarm.setOnCheckedChangeListener { _, checked ->
            NotificationPrefs.setChatNotificationEnabled(requireContext(), roomId, checked)

            val msg = if (checked) {
                "이 방의 채팅 알림을 켰어요."
            } else {
                "이 방의 채팅 알림을 껐어요."
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // 📸 이미지 섹션 클릭 → 전체보기로 이동
        layoutImageSection.setOnClickListener {
            if (images.isEmpty()) {
                Toast.makeText(requireContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = android.content.Intent(requireContext(), ImageGalleryActivity::class.java)
            intent.putStringArrayListExtra(
                "IMAGE_URIS",
                ArrayList(images.map { it.toString() })
            )
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            val displayMetrics = resources.displayMetrics

            params.width = (displayMetrics.widthPixels * 0.8f).toInt()
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.END

            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    companion object {
        private const val ARG_ROOM_ID = "arg_room_id"
        private const val ARG_ROOM_NAME = "arg_room_name"
        private const val ARG_MEMBER_NICKNAMES = "arg_member_nicknames"
        private const val ARG_IMAGE_URIS = "arg_image_uris"

        fun newInstance(
            roomId: Long,
            roomName: String,
            memberNicknames: List<String>,
            imageUris: List<Uri>,
        ): RoomMenuDialogFragment {
            val args = Bundle().apply {
                putLong(ARG_ROOM_ID, roomId)
                putString(ARG_ROOM_NAME, roomName)
                putStringArrayList(ARG_MEMBER_NICKNAMES, ArrayList(memberNicknames))
                putStringArrayList(
                    ARG_IMAGE_URIS,
                    ArrayList(imageUris.map { it.toString() })
                )
            }

            return RoomMenuDialogFragment().apply {
                arguments = args
            }
        }
    }

    private fun buildFullImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null

        return if (path.startsWith("http")) {
            path
        } else {
            val base = RetrofitClient.IMAGE_BASE_URL.trimEnd('/')
            val cleaned = path.trimStart('/')
            "$base/$cleaned"
        }
    }

}
