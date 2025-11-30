// RoomMenuDialogFragment.kt
package com.example.plango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment

class RoomMenuDialogFragment : DialogFragment() {

    private lateinit var roomName: String
    private var memberNicknames: List<String> = emptyList()
    private var images: List<Uri> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setStyle(STYLE_NORMAL, R.style.RightSheetDialogTheme)

        val args = requireArguments()
        roomName = args.getString(ARG_ROOM_NAME, "")
        memberNicknames =
            args.getStringArrayList(ARG_MEMBER_NICKNAMES)?.toList() ?: emptyList()
        images =
            args.getParcelableArrayList<Uri>(ARG_IMAGE_URIS) ?: emptyList()
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

        // 상단 제목은 "메뉴"
        tvTitle.text = "메뉴"

        // 참여자 수
        tvMemberTitle.text = "참여자 목록 (${memberNicknames.size}명)"

        // 이미지 섹션
        if (images.isEmpty()) {
            layoutImageSection.visibility = View.VISIBLE
        } else {
            layoutImageSection.visibility = View.VISIBLE
            tvImageCount.text = "(${images.size}개)"
            ivImagePreview.setImageURI(images.last())
        }

        // 참여자 이름 간단히 표시 (나중에 RecyclerView로 교체 가능)
        memberListLayout.removeAllViews()

        memberNicknames.forEachIndexed { index, nickname ->
            // 카드 레이아웃 inflate
            val itemView = layoutInflater.inflate(
                R.layout.item_room_member,
                memberListLayout,
                false
            )

            val tvNickname = itemView.findViewById<TextView>(R.id.tvMemberNickname)
            val tvRealName = itemView.findViewById<TextView>(R.id.tvMemberRealName)
            val tvHostBadge = itemView.findViewById<TextView>(R.id.tvHostBadge)
            val btnTransfer = itemView.findViewById<Button>(R.id.btnTransferHost)

            tvNickname.text = nickname

            // 아직 실명 정보 없으면 숨겨도 됨
            tvRealName.visibility = View.GONE

            // 첫 번째 멤버(예: "나")를 방장으로 가정 → 왕관 표시 + 버튼 숨기기
            val isHost = (index == 0)
            tvHostBadge.visibility = if (isHost) View.VISIBLE else View.GONE
            btnTransfer.visibility = if (isHost) View.GONE else View.VISIBLE

            btnTransfer.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "${nickname}님에게 방장을 양도하는 기능은 나중에 붙이자 😄",
                    Toast.LENGTH_SHORT
                ).show()
            }

            memberListLayout.addView(itemView)
        }

        btnClose.setOnClickListener { dismiss() }

        // TODO: switchAlarm 동작은 나중에
        switchAlarm.setOnCheckedChangeListener { _, _ ->
            // 나중에 구현
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
        private const val ARG_ROOM_NAME = "arg_room_name"
        private const val ARG_MEMBER_NICKNAMES = "arg_member_nicknames"
        private const val ARG_IMAGE_URIS = "arg_image_uris"

        fun newInstance(
            roomName: String,
            memberNicknames: List<String>,
            imageUris: List<Uri>
        ): RoomMenuDialogFragment {
            return RoomMenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_NAME, roomName)
                    putStringArrayList(
                        ARG_MEMBER_NICKNAMES,
                        ArrayList(memberNicknames)
                    )
                    putParcelableArrayList(ARG_IMAGE_URIS, ArrayList(imageUris))
                }
            }
        }
    }
}
