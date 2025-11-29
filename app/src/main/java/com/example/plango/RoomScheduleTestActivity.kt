package com.example.plango

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.model.TravelScheduleItem
import com.google.android.material.tabs.TabLayout
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

class RoomScheduleTestActivity :
    AppCompatActivity(),
    OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    // 일정 / 위시리스트 데이터
    private lateinit var dailySchedules: MutableList<TravelDailySchedule>
    private var currentDayIndex: Int = 0
    private lateinit var wishlistItems: MutableList<WishlistPlaceItem>

    // UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleTimelineAdapter
    private lateinit var wishlistAdapter: WishlistAdapter

    private lateinit var tabLayoutDay: TabLayout
    private lateinit var tabWishlistText: TextView
    private lateinit var tabScheduleText: TextView
    private lateinit var tabChatText: TextView

    private lateinit var mapContainer: View
    private lateinit var dividerTop: View
    private lateinit var btnEditSchedule: View        // 편집 버튼(LinearLayout)
    private lateinit var wishlistHeader: View
    private lateinit var btnAddWishlistPlace: Button

    // 편집 모드 플래그
    private var isEditMode: Boolean = false

    // 지도 캐시
    private val markerList = mutableListOf<Marker>()
    private var routePolyline: Polyline? = null

    private enum class BottomTab { WISHLIST, SCHEDULE, CHAT }
    private var currentBottomTab: BottomTab = BottomTab.SCHEDULE

    // Places Autocomplete 결과 받기
    private val placeSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            handlePlaceSelected(place)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_room_schedule)

        // Places 초기화 (이미 되어 있으면 패스)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // 데이터 준비: 더미 대신 "빈 일정 / 빈 위시리스트"부터 시작
        dailySchedules = createInitialDailySchedules()
        wishlistItems = mutableListOf()

        // 뷰 찾기
        recyclerView = findViewById(R.id.recyclerTimeline)
        tabLayoutDay = findViewById(R.id.tabLayoutDay)
        tabWishlistText = findViewById(R.id.tabWishlist)
        tabScheduleText = findViewById(R.id.tabSchedule)
        tabChatText = findViewById(R.id.tabChat)

        mapContainer = findViewById(R.id.mapContainer)
        dividerTop = findViewById(R.id.dividerTop)
        btnEditSchedule = findViewById(R.id.btnEditSchedule)
        wishlistHeader = findViewById(R.id.layoutWishlistHeader)
        btnAddWishlistPlace = findViewById(R.id.btnAddWishlistPlace)

        setupRecyclerView()
        setupMap()
        setupTabLayout()
        setupBottomNav()
        setupWishlistHeader()
        setupEditButton()

        // 기본: 일정 탭 + 1일차
        switchBottomTab(BottomTab.SCHEDULE)
        showDay(0)
    }

    // 현재 dayIndex 기준으로 시간 순으로 정렬된 일정 리스트
    private fun getSortedItemsForDay(dayIndex: Int): List<TravelScheduleItem> {
        val day = dailySchedules[dayIndex]
        // timeLabel = "HH:mm" 형식이라고 가정
        return day.items.sortedBy { it.timeLabel }
    }

    // ============================================================
    // RecyclerView
    // ============================================================
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 카드 클릭 / 연필 클릭 콜백 분리
        scheduleAdapter = ScheduleTimelineAdapter(
            onItemClick = { item ->
                // 카드 전체 클릭 → 일정 탭일 때 지도 포커스
                if (currentBottomTab == BottomTab.SCHEDULE) {
                    focusMapOnItem(item)
                }
            },
            onItemEditClick = { item ->
                // 연필 아이콘 클릭 → 해당 day 내 인덱스 찾기
                val day = dailySchedules[currentDayIndex]
                val indexInDay = day.items.indexOf(item)
                if (indexInDay == -1) {
                    Toast.makeText(this, "일정을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@ScheduleTimelineAdapter
                }

                val bottomSheet = EditScheduleBottomSheet(
                    schedule = item,
                    onUpdated = { newStart, newEnd ->
                        // ✅ 시간 수정
                        val old = day.items[indexInDay]
                        val updated = old.copy(
                            timeLabel = newStart,
                            timeRange = "$newStart ~ $newEnd"
                        )
                        day.items[indexInDay] = updated

                        // 타임라인 & 지도 갱신
                        showDay(currentDayIndex)
                        Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onDeleted = {
                        // ✅ 1) day 에서 제거
                        val removed = day.items.removeAt(indexInDay)

                        // ✅ 2) 위시리스트 아이템으로 변환해서 추가
                        val wishlistItem = WishlistPlaceItem(
                            placeName = removed.placeName,
                            address = removed.address,
                            lat = removed.lat,
                            lng = removed.lng,
                            addedBy = "나"   // TODO: 나중에 실제 유저 닉네임
                        )
                        wishlistItems.add(wishlistItem)

                        // ✅ 3) UI 갱신
                        // - 일정 탭: 타임라인 & 지도 새로 그림
                        showDay(currentDayIndex)

                        // - 위시리스트 탭 열려 있으면 리스트 새로고침
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

        wishlistAdapter = WishlistAdapter(wishlistItems) { place ->
            openConfirmScheduleBottomSheet(place)
        }

        recyclerView.adapter = scheduleAdapter
    }

    // 편집 버튼(우측 상단 LinearLayout) 클릭 시 편집 모드 토글
    private fun setupEditButton() {
        btnEditSchedule.setOnClickListener {
            if (currentBottomTab != BottomTab.SCHEDULE) return@setOnClickListener

            isEditMode = !isEditMode
            scheduleAdapter.isEditMode = isEditMode

            val msg = if (isEditMode) "편집 모드를 켰습니다." else "편집 모드를 껐습니다."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // RecyclerView의 top 제약 변경
    private fun setRecyclerTopTo(targetViewId: Int) {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = targetViewId
        recyclerView.layoutParams = params
    }

    // ============================================================
    // 지도
    // ============================================================
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

    // ============================================================
    // 날짜 탭
    // ============================================================
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

    // ============================================================
    // 위시리스트 헤더 (+ 장소 추가)
    // ============================================================
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
            addedBy = "나"   // TODO: 나중에 실제 사용자 닉네임으로 교체
        )

        wishlistItems.add(newItem)
        if (currentBottomTab == BottomTab.WISHLIST) {
            wishlistAdapter.refresh()
        }

        Toast.makeText(this, "위시리스트에 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }

    // ============================================================
    // 바텀바
    // ============================================================
    private fun setupBottomNav() {
        tabWishlistText.setOnClickListener { switchBottomTab(BottomTab.WISHLIST) }
        tabScheduleText.setOnClickListener { switchBottomTab(BottomTab.SCHEDULE) }
        tabChatText.setOnClickListener {
            switchBottomTab(BottomTab.CHAT)
            Toast.makeText(this, "채팅 화면은 나중에 붙이자 😅", Toast.LENGTH_SHORT).show()
        }
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

                setRecyclerTopTo(R.id.layoutWishlistHeader)
                recyclerView.adapter = wishlistAdapter
                wishlistAdapter.refresh()
            }
            BottomTab.CHAT -> {
                // 아직 별도 UI 없음
            }
        }
    }

    private fun updateBottomNavUI() {
        fun TextView.setActive(active: Boolean) {
            val colorRes = if (active) android.R.color.black else android.R.color.darker_gray
            setTextColor(ContextCompat.getColor(context, colorRes))
            setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
        }

        tabWishlistText.setActive(currentBottomTab == BottomTab.WISHLIST)
        tabScheduleText.setActive(currentBottomTab == BottomTab.SCHEDULE)
        tabChatText.setActive(currentBottomTab == BottomTab.CHAT)
    }

    // ============================================================
    // 일정 모드: 날짜별 일정 표시
    // ============================================================
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
            .color(android.graphics.Color.parseColor("#2A80FF"))
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

    // ============================================================
    // 일정 모드: 아이템 클릭 → 지도 포커스
    // ============================================================
    private fun focusMapOnItem(item: TravelScheduleItem) {
        if (!::googleMap.isInitialized) return
        val target = LatLng(item.lat, item.lng)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15f))
    }

    // ============================================================
    // 위시리스트: 일정 확정 바텀시트
    // ============================================================
    private fun openConfirmScheduleBottomSheet(place: WishlistPlaceItem) {
        val bottomSheet = ConfirmScheduleBottomSheet(
            place = place,
            days = dailySchedules,
            onConfirmed = { dayIndex, startTime, endTime ->
                val targetDay = dailySchedules.getOrNull(dayIndex) ?: return@ConfirmScheduleBottomSheet

                val newSchedule = TravelScheduleItem(
                    timeLabel = startTime,
                    timeRange = "$startTime ~ $endTime",
                    placeName = place.placeName,
                    address = place.address,
                    lat = place.lat,
                    lng = place.lng
                )
                targetDay.items.add(newSchedule)

                // 위시리스트에서 제거
                wishlistItems.remove(place)

                // UI 갱신
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

    // ============================================================
    // 초기 데이터: 일정은 빈 리스트, 날짜만 존재
    // ============================================================
    private fun createInitialDailySchedules(): MutableList<TravelDailySchedule> {
        val day1Items = mutableListOf<TravelScheduleItem>()
        val day2Items = mutableListOf<TravelScheduleItem>()

        return mutableListOf(
            TravelDailySchedule(0, "1일차", day1Items),
            TravelDailySchedule(1, "2일차", day2Items)
        )
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
    val addedBy: String
)
