package com.example.plango

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.RoomAdapter
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.TravelRoom
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
class RoomSearchDialogFragment : DialogFragment() {

    private lateinit var rvResult: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvClose: TextView

    private lateinit var roomAdapter: RoomAdapter
    private var allRooms: List<TravelRoom> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Dialog_RoomSearch)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
            // ⚠️ 여기서는 이제 dim 관련 설정은 안 해도 됨
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_room_search, container, false)

        rvResult = view.findViewById(R.id.rv_search_result)
        etSearch = view.findViewById(R.id.et_search_room)
        tvClose = view.findViewById(R.id.tv_close)

        setupList()
        setupSearch()
        setupClose()

        loadRooms()

        return view
    }

    private fun setupList() {
        roomAdapter = RoomAdapter(
            emptyList(),
            onClick = { room ->
                val context = requireContext()
                val intent = Intent(context, RoomScheduleTestActivity::class.java).apply {
                    putExtra("ROOM_ID", room.id)
                    putExtra("ROOM_NAME", room.title)
                    putExtra("ROOM_MEMO", room.memo)
                    putExtra("START_DATE", room.startDate)
                    putExtra("END_DATE", room.endDate)
                    putStringArrayListExtra(
                        "MEMBER_NICKNAMES",
                        ArrayList(room.memberNicknames)
                    )
                }
                startActivity(intent)
                dismiss()
            },
            usePopupStyle = true      // 🔹 여기 한 줄만 추가!
        )

        rvResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }
    }


    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val query = s?.toString().orEmpty()
                filterRooms(query)
            }
        })
    }

    private fun setupClose() {
        tvClose.setOnClickListener { dismiss() }
    }

    private fun loadRooms() {
        val cached = TravelRoomRepository.getRooms()

        if (cached.isNotEmpty()) {
            // 이미 RoomFragment 등에서 한 번 로드된 상태면 그대로 사용
            allRooms = cached.distinctBy { it.id }   // 혹시 모를 중복 방지 (2번 문제도 같이 해결)
            roomAdapter.submitList(allRooms)
            return
        }

        // ❗ 아직 아무 방도 안 올라와 있으면 → 여기서 서버 호출
        viewLifecycleOwner.lifecycleScope.launch {
            val success = TravelRoomRepository.fetchRoomsFromServer()

            val loaded = TravelRoomRepository.getRooms()
            Log.d("RoomSearch", "loaded rooms size=${loaded.size}, ids=${loaded.map { it.id }}")

            allRooms = loaded.distinctBy { it.id }   // 여기서도 중복 제거
            roomAdapter.submitList(allRooms)
        }
    }


    private fun filterRooms(query: String) {
        if (allRooms.isEmpty()) {
            roomAdapter.submitList(emptyList())
            return
        }

        if (query.isBlank()) {
            roomAdapter.submitList(allRooms)
            return
        }

        val lowerQuery = query.lowercase()
        val filtered = allRooms.filter { room ->
            room.title.lowercase().contains(lowerQuery) ||
                    (room.memo ?: "").lowercase().contains(lowerQuery)
        }

        roomAdapter.submitList(filtered)
    }

}
