package com.example.plango.model

data class ScheduleDto(
    // ⬇️ 기본 API 스펙 ⬇️
    val scheduleId: Long,
    val dayIndex: Int,
    val startTime: String,
    val endTime: String,
    val roomPlaceId: Long,
    val memo: String?, // 메모는 사용하지 않지만 Nullable로 받아둠

    // ⬇️ 백엔드가 추가한 필드 (Null 안정성을 위해 ? 유지) ⬇️
    val placeName: String?,
    val address: String?,
    val lat: Double?,
    val lng: Double?
)
// ScheduleDto 파일에 추가하거나 별도의 Mapper 파일에 정의할 수 있습니다.

fun ScheduleDto.toTravelScheduleItem(): TravelScheduleItem {
    return TravelScheduleItem(
        timeLabel = this.startTime,
        timeRange = "${this.startTime} ~ ${this.endTime}",

        // ⬇️ placeName이 null이면, roomPlaceId를 활용한 임시 이름으로 대체합니다.
        placeName = this.placeName ?: "장소 #${this.roomPlaceId}",

        // ⬇️ address가 null이면, '주소 정보 없음'으로 표시합니다.
        address = this.address ?: "주소 정보 없음",

        lat = this.lat ?: 0.0,
        lng = this.lng ?: 0.0
    )
}
