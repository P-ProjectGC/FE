package com.example.plango

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.ChatAdapter
import com.example.plango.data.ChatRepository
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage
import com.example.plango.model.TravelScheduleItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.tabs.TabLayout
import com.example.plango.RoomMenuDialogFragment
import com.example.plango.data.RetrofitClient
import com.example.plango.model.CreateWishlistPlaceRequest
import com.example.plango.model.TravelRoom
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.appcompat.app.AlertDialog


class RoomScheduleTestActivity :
    AppCompatActivity(),
    OnMapReadyCallback {

    // ⭐ 여행방 기본 정보
    private var roomId: Long = -1L
    private lateinit var roomName: String
    private var roomMemo: String? = null
    private lateinit var startDate: String
    private lateinit var endDate: String
    private var memberNicknames: List<String> = emptyList()

    // 지도
    private lateinit var googleMap: GoogleMap
    private val markerList = mutableListOf<Marker>()
    private var routePolyline: Polyline? = null

    // 일정 / 위시리스트 데이터
    private lateinit var dailySchedules: MutableList<TravelDailySchedule>
    private var currentDayIndex: Int = 0
    private lateinit var wishlistItems: MutableList<WishlistPlaceItem>

    // RecyclerView + 어댑터
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleTimelineAdapter
    private lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var chatAdapter: ChatAdapter

    // 상단 / 지도 / 버튼 / 헤더
    private lateinit var tabLayoutDay: TabLayout
    private lateinit var mapContainer: View
    private lateinit var dividerTop: View
    private lateinit var btnEditSchedule: View
    private lateinit var wishlistHeader: View
    private lateinit var btnAddWishlistPlace: Button
    private lateinit var layoutRoomHeader: LinearLayout

    // 헤더 내 텍스트/버튼
    private lateinit var tvRoomTitle: TextView
    private lateinit var tvRoomMemberCount: TextView
    private lateinit var btnRoomMenu: ImageButton

    // 바텀 내비 (텍스트 + 부모 레이아웃 + 아이콘)
    private lateinit var tabWishlistText: TextView
    private lateinit var tabScheduleText: TextView
    private lateinit var tabChatText: TextView
    private lateinit var layoutTabWishlist: View
    private lateinit var layoutTabSchedule: View
    private lateinit var layoutTabChat: View
    private lateinit var iconWishlist: ImageView
    private lateinit var iconSchedule: ImageView
    private lateinit var iconChat: ImageView

    // 채팅 입력
    private lateinit var layoutChatInput: View
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendChat: ImageButton
    private lateinit var btnPickPhoto: ImageButton

    // 편집 모드 플래그
    private var isEditMode: Boolean = false

    private enum class BottomTab { WISHLIST, SCHEDULE, CHAT }

    // 초기값을 WISHLIST로 두고, onCreate에서 SCHEDULE로 전환
    private var currentBottomTab: BottomTab = BottomTab.WISHLIST

    // Places Autocomplete 결과
    private val placeSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            handlePlaceSelected(place)
        }
    }

    // 이미지 픽커
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleImagePicked(uri)
        }
    }



    // ------------------------------------------------------------
    // onCreate
    // ------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_room_schedule)

        // 1) 인텐트로 넘어온 방 정보 받기
        roomId = intent.getLongExtra("ROOM_ID", -1L)
        roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        roomMemo = intent.getStringExtra("ROOM_MEMO")
        startDate = intent.getStringExtra("START_DATE") ?: ""
        endDate = intent.getStringExtra("END_DATE") ?: ""
        memberNicknames =
            intent.getStringArrayListExtra("MEMBER_NICKNAMES")?.toList() ?: emptyList()

        // 🔍 ROOM_ID 기준으로 레포에서 방 정보 보정
        if (roomId != -1L) {
            val room = TravelRoomRepository.getRooms().find { it.id == roomId }

            if (room != null) {
                if (roomName.isBlank()) roomName = room.title
                if (startDate.isBlank()) startDate = room.startDate
                if (endDate.isBlank()) endDate = room.endDate
            }
        }

        // 2) Toolbar 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbarRoomTitle)
        toolbar.title = roomName
        toolbar.setNavigationOnClickListener { finish() }

        // 3) Places 초기화
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // 4) 데이터 초기화
        dailySchedules = createInitialDailySchedules()
        wishlistItems = mutableListOf()

        // ===== View 바인딩 =====
        recyclerView = findViewById(R.id.recyclerTimeline)
        tabLayoutDay = findViewById(R.id.tabLayoutDay)
        mapContainer = findViewById(R.id.mapContainer)
        dividerTop = findViewById(R.id.dividerTop)
        btnEditSchedule = findViewById(R.id.btnEditSchedule)
        wishlistHeader = findViewById(R.id.layoutWishlistHeader)
        btnAddWishlistPlace = findViewById(R.id.btnAddWishlistPlace)

        // 헤더
        layoutRoomHeader = findViewById(R.id.layoutRoomHeader)
        tvRoomTitle = findViewById(R.id.tvRoomTitle)
        tvRoomMemberCount = findViewById(R.id.tvRoomMemberCount)
        btnRoomMenu = findViewById(R.id.btnRoomMenu)

        // 바텀 내비 텍스트/레이아웃/아이콘
        tabWishlistText = findViewById(R.id.tabWishlist)
        tabScheduleText = findViewById(R.id.tabSchedule)
        tabChatText = findViewById(R.id.tabChat)
        layoutTabWishlist = findViewById(R.id.layoutTabWishlist)
        layoutTabSchedule = findViewById(R.id.layoutTabSchedule)
        layoutTabChat = findViewById(R.id.layoutTabChat)
        iconWishlist = findViewById(R.id.iconWishlist)
        iconSchedule = findViewById(R.id.iconSchedule)
        iconChat = findViewById(R.id.iconChat)

        // 채팅 입력바
        layoutChatInput = findViewById(R.id.layoutChatInput)
        etChatMessage = findViewById(R.id.etChatMessage)
        btnSendChat = findViewById(R.id.btnSendChat)
        btnPickPhoto = findViewById(R.id.btnPickPhoto)

        // ===== 헤더 내용 세팅 =====
        tvRoomTitle.text = roomName

        val memberCountFromList = memberNicknames.size
        val roomFromRepo = if (roomId != -1L) {
            TravelRoomRepository.getRooms().find { it.id == roomId }
        } else null

        val memberCount = when {
            memberCountFromList > 0 -> memberCountFromList
            roomFromRepo?.memberCount != null && roomFromRepo.memberCount > 0 -> roomFromRepo.memberCount
            else -> 1
        }
        tvRoomMemberCount.text = "${memberCount}명"

        // 메뉴 버튼
        btnRoomMenu.setOnClickListener {
            openRoomMenu()
        }

        // ===== 나머지 셋업 =====
        setupRecyclerView()
        setupMap()
        setupTabLayout()
        setupBottomNav()
        setupWishlistHeader()
        setupEditButton()

        // 기본: 일정 탭 + 1일차
        switchBottomTab(BottomTab.SCHEDULE)
        showDay(0)

        // 🔹 1) 이 기기의 ID 가져오기
        val deviceId = DeviceIdManager.getDeviceId(this)

        // 🔔 채팅 알림 채널 생성 (여러 번 호출해도 괜찮음)
        NotificationHelper.createChatNotificationChannel(this)

        // 🔹 2) 현재 방 정보 가져오기 (예시: Repository에서)
        val roomId = intent.getLongExtra("ROOM_ID", -1L)
        val room: TravelRoom? = TravelRoomRepository.getRoomById(roomId) // 네 구조에 맞게 수정

        // 🔹 3) 방장 여부 판단
        val isHost: Boolean = if (room == null) {
            true // 혹시 null이면 일단 막지 말고 모두 허용
        } else {
            // hostId가 비어 있으면 옛 데이터 → 일단 모두 방장 취급
            room.hostId.isBlank() || room.hostId == deviceId
        }

        // 🔹 4) 위시리스트 어댑터 생성 시 isHost 넘기기

        wishlistAdapter = WishlistAdapter(
            items = wishlistItems,
            isHost = isHost,
            onConfirmClick = { item ->
                openConfirmScheduleBottomSheet(item)
            },
            onDeleteClick = { item ->
                openDeleteWishlistConfirmDialog(item)
            }
        )

        // ✅ 어댑터 세팅 끝난 뒤에 호출
        loadWishlistFromServer()

    }

    // ------------------------------------------------------------
    // 이미지 선택 / 채팅 이미지 메시지
    // ------------------------------------------------------------
    private fun handleImagePicked(originalUri: Uri) {
        val currentMillis = System.currentTimeMillis()
        val timeText = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date(currentMillis))

        // 🔹 1) 포토 피커 URI → 앱 내부(cacheDir) 파일로 복사
        val localUri: Uri = try {
            val inputStream = contentResolver.openInputStream(originalUri)
                ?: throw Exception("Cannot open input stream")

            val file = java.io.File(
                cacheDir,
                "chat_img_${System.currentTimeMillis()}.jpg"
            )

            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(file)   // ← 이 Uri는 앱 재실행해도 권한 안 사라짐
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시 일단 원본 URI라도 사용 (앱 죽지 않게)
            originalUri
        }

        // 🔹 2) 메시지 객체 생성
        val message = ChatMessage(
            id = System.currentTimeMillis(),
            senderName = "나",
            message = null,
            timeText = timeText,
            isMe = true,
            imageUri = localUri,    // ⭐ picker URI 대신 로컬 파일 Uri 저장
            type = ChatContentType.IMAGE
        )

        // 🔹 3) UI에 메시지 추가
        chatAdapter.addMessage(message)

        // 🔹 4) 로컬 저장소에도 추가
        if (roomId != -1L) {
            ChatRepository.addMessage(roomId, message)
        }

        // 🔹 5) 스크롤을 맨 아래로
        recyclerView.post {
            recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }


    // ------------------------------------------------------------
    // RecyclerView / 어댑터
    // ------------------------------------------------------------
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 일정 어댑터
        scheduleAdapter = ScheduleTimelineAdapter(
            onItemClick = { item ->
                if (currentBottomTab == BottomTab.SCHEDULE) {
                    focusMapOnItem(item)
                }
            },
            onItemEditClick = { item ->
                val day = dailySchedules[currentDayIndex]
                val indexInDay = day.items.indexOf(item)
                if (indexInDay == -1) {
                    Toast.makeText(this, "일정을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@ScheduleTimelineAdapter
                }

                val bottomSheet = EditScheduleBottomSheet(
                    schedule = item,
                    onUpdated = { newStart, newEnd ->
                        val old = day.items[indexInDay]
                        val updated = old.copy(
                            timeLabel = newStart,
                            timeRange = "$newStart ~ $newEnd"
                        )
                        day.items[indexInDay] = updated
                        showDay(currentDayIndex)
                        Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onDeleted = {
                        val removed = day.items.removeAt(indexInDay)
                        val wishlistItem = WishlistPlaceItem(
                            placeName = removed.placeName,
                            address = removed.address,
                            lat = removed.lat,
                            lng = removed.lng,
                            addedBy = "나"
                        )
                        wishlistItems.add(wishlistItem)
                        showDay(currentDayIndex)

                        if (currentBottomTab == BottomTab.WISHLIST) {
                            wishlistAdapter.refresh()
                        }

                        Toast.makeText(
                            this,
                            "일정이 삭제되고 위시리스트로 이동했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                bottomSheet.show(supportFragmentManager, "EditScheduleBottomSheet")
            }
        )



        // 채팅 어댑터
        chatAdapter = ChatAdapter()

        // roomId 기준으로 저장된 채팅 불러오기
        if (roomId != -1L) {
            val savedMessages = ChatRepository.getMessages(roomId)
            if (savedMessages.isNotEmpty()) {
                chatAdapter.submitList(savedMessages.toList())
            }
        }

        recyclerView.adapter = scheduleAdapter

        // 채팅 입력
        btnSendChat.setOnClickListener {
            sendChatMessage()
        }
        btnPickPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun sendChatMessage() {
        val text = etChatMessage.text.toString().trim()
        if (text.isEmpty()) return

        val currentMillis = System.currentTimeMillis()
        val timeText = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date(currentMillis))

        val message = ChatMessage(
            id = System.currentTimeMillis(),
            senderName = "나",
            message = text,
            timeText = timeText,
            isMe = true
        )

        chatAdapter.addMessage(message)

        if (roomId != -1L) {
            ChatRepository.addMessage(roomId, message)
            // 🔔 테스트용: 내가 보낸 메시지도 알림으로 띄워보기
            NotificationHelper.showChatNotification(
                context = this,
                roomId = roomId,
                roomName = roomName,
                messagePreview = text
            )
        }

        etChatMessage.setText("")

        recyclerView.post {
            recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun setupEditButton() {
        btnEditSchedule.setOnClickListener {
            if (currentBottomTab != BottomTab.SCHEDULE) return@setOnClickListener

            isEditMode = !isEditMode
            scheduleAdapter.isEditMode = isEditMode

            val msg = if (isEditMode) "편집 모드를 켰습니다." else "편집 모드를 껐습니다."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRecyclerTopTo(targetViewId: Int) {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = targetViewId
        recyclerView.layoutParams = params
    }

    // ------------------------------------------------------------
    // 지도
    // ------------------------------------------------------------
    private fun setupMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isTiltGesturesEnabled = true

        if (currentBottomTab == BottomTab.SCHEDULE) {
            updateMapForCurrentDay()
        }
    }

    // ------------------------------------------------------------
    // 날짜 탭
    // ------------------------------------------------------------
    private fun setupTabLayout() {
        dailySchedules.forEach { day ->
            tabLayoutDay.addTab(tabLayoutDay.newTab().setText(day.dayTitle))
        }
        if (tabLayoutDay.tabCount > 0) {
            tabLayoutDay.getTabAt(0)?.select()
        }

        tabLayoutDay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (currentBottomTab != BottomTab.SCHEDULE) return
                showDay(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (currentBottomTab != BottomTab.SCHEDULE) return
                showDay(tab?.position ?: 0)
            }
        })
    }

    // ------------------------------------------------------------
    // 위시리스트 헤더
    // ------------------------------------------------------------
    private fun setupWishlistHeader() {
        btnAddWishlistPlace.setOnClickListener {
            openPlaceSearch()
        }
    }

    private fun openPlaceSearch() {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            fields
        ).build(this)

        placeSearchLauncher.launch(intent)
    }

    private fun handlePlaceSelected(place: Place) {
        val latLng = place.latLng
        if (latLng == null) {
            Toast.makeText(this, "좌표 정보가 없는 장소입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val newItem = WishlistPlaceItem(
            placeName = place.name ?: "이름 없는 장소",
            address = place.address ?: "",
            lat = latLng.latitude,
            lng = latLng.longitude,
            addedBy = "나",
            // 여기까지는 선택이지만, 이제는 placeId 도 같이 넣어줄 수 있음
            googlePlaceId = place.id,
            formattedAddress = place.address
        )

        // ✅ 여기서 **로컬 리스트에 직접 추가하지 말고**
        //    서버 연동 함수만 호출
        addPlaceToWishlistOnServer(newItem)

        // ❌ 아래 세 줄은 제거
        // wishlistItems.add(newItem)
        // if (currentBottomTab == BottomTab.WISHLIST) {
        //     wishlistAdapter.refresh()
        // }
        // Toast는 addPlaceToWishlistOnServer 안에서 성공 시 한 번만 띄우는 걸로 유지
    }


    // ------------------------------------------------------------
    // 바텀바
    // ------------------------------------------------------------
    private fun setupBottomNav() {
        layoutTabWishlist.setOnClickListener { switchBottomTab(BottomTab.WISHLIST) }
        layoutTabSchedule.setOnClickListener { switchBottomTab(BottomTab.SCHEDULE) }
        layoutTabChat.setOnClickListener { switchBottomTab(BottomTab.CHAT) }

        tabWishlistText.setOnClickListener { switchBottomTab(BottomTab.WISHLIST) }
        tabScheduleText.setOnClickListener { switchBottomTab(BottomTab.SCHEDULE) }
        tabChatText.setOnClickListener { switchBottomTab(BottomTab.CHAT) }

        updateBottomNavUI()
    }

    private fun switchBottomTab(tab: BottomTab) {
        if (currentBottomTab == tab) return
        currentBottomTab = tab
        updateBottomNavUI()

        when (tab) {
            BottomTab.SCHEDULE -> {
                mapContainer.visibility = View.VISIBLE
                tabLayoutDay.visibility = View.VISIBLE
                dividerTop.visibility = View.VISIBLE
                btnEditSchedule.visibility = View.VISIBLE
                wishlistHeader.visibility = View.GONE
                layoutChatInput.visibility = View.GONE
                layoutRoomHeader.visibility = View.GONE

                setRecyclerTopTo(R.id.btnEditSchedule)
                recyclerView.adapter = scheduleAdapter
                showDay(currentDayIndex)
            }

            BottomTab.WISHLIST -> {
                mapContainer.visibility = View.GONE
                tabLayoutDay.visibility = View.GONE
                dividerTop.visibility = View.GONE
                btnEditSchedule.visibility = View.GONE
                wishlistHeader.visibility = View.VISIBLE
                layoutChatInput.visibility = View.GONE
                layoutRoomHeader.visibility = View.GONE

                setRecyclerTopTo(R.id.layoutWishlistHeader)
                recyclerView.adapter = wishlistAdapter
                wishlistAdapter.refresh()
            }

            BottomTab.CHAT -> {
                mapContainer.visibility = View.GONE
                tabLayoutDay.visibility = View.GONE
                dividerTop.visibility = View.GONE
                btnEditSchedule.visibility = View.GONE
                wishlistHeader.visibility = View.GONE

                layoutRoomHeader.visibility = View.VISIBLE
                layoutChatInput.visibility = View.VISIBLE

                // 채팅은 헤더 아래에서 시작
                setRecyclerTopTo(R.id.layoutRoomHeader)

                recyclerView.adapter = chatAdapter
                recyclerView.post {
                    if (chatAdapter.itemCount > 0) {
                        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun updateBottomNavUI() {
        val activeColor = Color.parseColor("#47A8D4")
        val inactiveColor = Color.parseColor("#B3B3B3")

        fun setTabState(
            isActive: Boolean,
            textView: TextView,
            iconView: ImageView
        ) {
            textView.setTextColor(if (isActive) activeColor else inactiveColor)
            textView.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
            iconView.setColorFilter(if (isActive) activeColor else inactiveColor)
        }

        setTabState(currentBottomTab == BottomTab.WISHLIST, tabWishlistText, iconWishlist)
        setTabState(currentBottomTab == BottomTab.SCHEDULE, tabScheduleText, iconSchedule)
        setTabState(currentBottomTab == BottomTab.CHAT, tabChatText, iconChat)
    }

    // ------------------------------------------------------------
    // 일정 모드
    // ------------------------------------------------------------
    private fun getSortedItemsForDay(dayIndex: Int): List<TravelScheduleItem> {
        val day = dailySchedules[dayIndex]
        return day.items.sortedBy { it.timeLabel }
    }

    private fun showDay(dayIndex: Int) {
        if (dailySchedules.isEmpty()) return
        if (dayIndex !in dailySchedules.indices) return

        currentDayIndex = dayIndex

        val sortedItems = getSortedItemsForDay(dayIndex)
        scheduleAdapter.submitList(sortedItems)

        if (::googleMap.isInitialized && currentBottomTab == BottomTab.SCHEDULE) {
            updateMapForCurrentDay()
        }
    }

    private fun updateMapForCurrentDay() {
        if (!::googleMap.isInitialized) return

        val items = getSortedItemsForDay(currentDayIndex)
        if (items.isEmpty()) {
            markerList.forEach { it.remove() }
            markerList.clear()
            routePolyline?.remove()
            routePolyline = null
            return
        }

        markerList.forEach { it.remove() }
        markerList.clear()
        routePolyline?.remove()
        routePolyline = null

        val polylineOptions = PolylineOptions()
            .color(Color.parseColor("#2A80FF"))
            .width(8f)
            .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))

        val boundsBuilder = LatLngBounds.Builder()

        items.forEachIndexed { index, item ->
            val pos = LatLng(item.lat, item.lng)

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("${index + 1}. ${item.placeName}")
            )
            if (marker != null) markerList.add(marker)

            polylineOptions.add(pos)
            boundsBuilder.include(pos)
        }

        routePolyline = googleMap.addPolyline(polylineOptions)

        val bounds = boundsBuilder.build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun focusMapOnItem(item: TravelScheduleItem) {
        if (!::googleMap.isInitialized) return
        val target = LatLng(item.lat, item.lng)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15f))
    }

    // ------------------------------------------------------------
    // 위시리스트: 일정 확정 바텀시트
    // ------------------------------------------------------------
    private fun openConfirmScheduleBottomSheet(place: WishlistPlaceItem) {
        val bottomSheet = ConfirmScheduleBottomSheet(
            place = place,
            days = dailySchedules,
            onConfirmed = { dayIndex, startTime, endTime ->
                val targetDay =
                    dailySchedules.getOrNull(dayIndex) ?: return@ConfirmScheduleBottomSheet

                val newSchedule = TravelScheduleItem(
                    timeLabel = startTime,
                    timeRange = "$startTime ~ $endTime",
                    placeName = place.placeName,
                    address = place.address,
                    lat = place.lat,
                    lng = place.lng
                )
                targetDay.items.add(newSchedule)

                wishlistItems.remove(place)

                if (currentBottomTab == BottomTab.WISHLIST) {
                    wishlistAdapter.refresh()
                } else if (currentBottomTab == BottomTab.SCHEDULE) {
                    showDay(currentDayIndex)
                }

                Toast.makeText(this, "일정에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
        )

        bottomSheet.show(supportFragmentManager, "ConfirmScheduleBottomSheet")
    }

    //위시리스트삭제 함수 팝업
    private fun openDeleteWishlistConfirmDialog(item: WishlistPlaceItem) {
        AlertDialog.Builder(this)
            .setTitle("위시리스트 삭제")
            .setMessage("이 장소를 위시리스트에서 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                deleteWishlistPlaceOnServer(item)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    //위시리스트삭제함수
    private fun deleteWishlistPlaceOnServer(item: WishlistPlaceItem) {
        val roomId = this.roomId          // 이미 멤버 변수로 있는 값 사용
        val placeId = item.placeId

        if (placeId == null) {
            Toast.makeText(
                this,
                "이 장소는 서버 ID가 없어 삭제할 수 없어요.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.roomApiService
                    .deleteWishlistPlace(roomId, placeId)

                Log.d("WishlistDelete", "request roomId=$roomId, placeId=$placeId")
                Log.d("WishlistDelete", "response raw = ${response.raw()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("WishlistDelete", "response body = $body")

                    if (body?.code == 0) {
                        // UI 리스트에서 제거
                        wishlistAdapter.removeItem(item)

                        Toast.makeText(
                            this@RoomScheduleTestActivity,
                            "위시리스트에서 삭제했어요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RoomScheduleTestActivity,
                            "서버 응답 오류: ${body?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RoomScheduleTestActivity,
                        "HTTP 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RoomScheduleTestActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }














    //위시리스트 post용
    private fun addPlaceToWishlistOnServer(place: WishlistPlaceItem) {
        val roomId = 2L   // 테스트용
        val memberId = 8L // 테스트용

        val request = CreateWishlistPlaceRequest(
            name = place.placeName,
            address = place.address,
            googlePlaceId = place.googlePlaceId ?: "",
            formattedAddress = place.formattedAddress ?: place.address,
            latitude = place.lat,
            longitude = place.lng
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.roomApiService
                    .createWishlistPlace(roomId, memberId, request)

                // ★ 디버깅용 로그 (있으면 도움 됨)
                Log.d("Wishlist", "request = $request")
                Log.d("Wishlist", "response body = ${response.body()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Wishlist", "response body = $body")

                    if (body?.code == 0) {
                        val dto = body.data   // WishlistPlaceDto

                        // dto 가 null 아님을 체크
                        if (dto != null) {
                            val newItem = WishlistPlaceItem(
                                placeName = dto.name,
                                address = if (dto.formattedAddress.isNotBlank()) {
                                    dto.formattedAddress
                                } else {
                                    dto.address
                                },
                                lat = dto.latitude,
                                lng = dto.longitude,
                                addedBy = dto.createdByMemberId.toString(),
                                googlePlaceId = dto.googlePlaceId,
                                formattedAddress = dto.formattedAddress,
                                placeId = dto.id           // 🔴 여기 중요
                            )

                            wishlistItems.add(newItem)
                            wishlistAdapter.refresh()
                        }
                        Toast.makeText(
                            this@RoomScheduleTestActivity,
                            "위시리스트에 추가됐어요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RoomScheduleTestActivity,
                            "서버 응답 오류: ${body?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RoomScheduleTestActivity,
                        "HTTP 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RoomScheduleTestActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
   //wishlist post
    private fun loadWishlistFromServer() {
        val roomId = 2L   // TODO: 실제 roomId 로 교체 필요

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.roomApiService
                    .getWishlistPlaces(roomId)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.code == 0) {
                        val dtoList = body.data ?: emptyList()

                        // 기존 리스트 비우고 서버 데이터로 다시 채우기
                        wishlistItems.clear()

                        dtoList.forEach { dto ->
                            val item = WishlistPlaceItem(
                                placeName = dto.name,
                                // formattedAddress 가 있으면 그걸, 없으면 address 사용
                                address = if (dto.formattedAddress.isNotBlank()) {
                                    dto.formattedAddress

                                } else {
                                    dto.address
                                },
                                lat = dto.latitude,
                                lng = dto.longitude,
                                // 지금은 createdByMemberId 를 문자열로 넣어두기 (닉네임 연동 전 임시)
                                addedBy = dto.createdByMemberId.toString(),
                                googlePlaceId = dto.googlePlaceId,
                                formattedAddress = dto.formattedAddress,
                                        placeId = dto.id   // 🔴 여기!!
                            )
                            wishlistItems.add(item)
                        }

                        wishlistAdapter.refresh()

                    } else {
                        Toast.makeText(
                            this@RoomScheduleTestActivity,
                            "서버 응답 오류: ${body?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RoomScheduleTestActivity,
                        "HTTP 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RoomScheduleTestActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }











    // ------------------------------------------------------------
    // 채팅방 메뉴 (상단 헤더의 오른쪽 아이콘)
    // ------------------------------------------------------------
    private fun openRoomMenu() {
        val images = if (roomId != -1L) {
            ChatRepository.getMessages(roomId)
                .filter { it.type == ChatContentType.IMAGE }
                .mapNotNull { it.imageUri }
        } else {
            emptyList()
        }

        val dialog = RoomMenuDialogFragment.newInstance(
            roomId = roomId,
            roomName = roomName,
            memberNicknames = memberNicknames,
            imageUris = images
        )
        dialog.show(supportFragmentManager, "RoomMenuDialog")
    }






    // ------------------------------------------------------------
    // 초기 데이터 생성
    // ------------------------------------------------------------
    private fun createInitialDailySchedules(): MutableList<TravelDailySchedule> {
        val start = java.time.LocalDate.parse(startDate)
        val end = java.time.LocalDate.parse(endDate)
        val days = java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1

        val list = mutableListOf<TravelDailySchedule>()
        for (i in 0 until days) {
            list.add(
                TravelDailySchedule(
                    dayIndex = i,
                    dayTitle = "${i + 1}일차",
                    items = mutableListOf()
                )
            )
        }
        return list
    }
}

// ====== 모델 ======
data class TravelDailySchedule(
    val dayIndex: Int,
    val dayTitle: String,
    val items: MutableList<TravelScheduleItem>
)

data class WishlistPlaceItem(
    val placeName: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val addedBy: String,
    val googlePlaceId: String? = null,
    val formattedAddress: String? = null,
    val placeId: Long? = null   // 🔴 이거 반드시 필요!!
)


