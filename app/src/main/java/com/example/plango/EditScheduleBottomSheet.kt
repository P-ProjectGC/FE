package com.example.plango

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.plango.model.TravelScheduleItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditScheduleBottomSheet(
    private val schedule: TravelScheduleItem,
    private val onUpdated: (startTime: String, endTime: String) -> Unit,
    private val onDeleted: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_edit_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<View>(R.id.btnCloseSheet)

        val textPlaceName = view.findViewById<TextView>(R.id.textPlaceNameEdit)
        val textAddress = view.findViewById<TextView>(R.id.textAddressEdit)

        val npStartAmPm = view.findViewById<NumberPicker>(R.id.npEditStartAmPm)
        val npStartHour = view.findViewById<NumberPicker>(R.id.npEditStartHour)
        val npStartMinute = view.findViewById<NumberPicker>(R.id.npEditStartMinute)

        val npEndAmPm = view.findViewById<NumberPicker>(R.id.npEditEndAmPm)
        val npEndHour = view.findViewById<NumberPicker>(R.id.npEditEndHour)
        val npEndMinute = view.findViewById<NumberPicker>(R.id.npEditEndMinute)

        val btnDelete = view.findViewById<TextView>(R.id.btnDeleteSchedule)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancelEdit)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirmEdit)

        // 장소 정보 표시
        textPlaceName.text = schedule.placeName
        textAddress.text = schedule.address

        // NumberPicker 기본 세팅
        setupTimePickers(npStartAmPm, npStartHour, npStartMinute)
        setupTimePickers(npEndAmPm, npEndHour, npEndMinute)

        // 기존 시간값으로 초기화
        applyTimeToPickers(schedule.timeLabel, npStartAmPm, npStartHour, npStartMinute)

        val endTimeFromRange = extractEndTime(schedule.timeRange)  // "16:00"
        applyTimeToPickers(endTimeFromRange, npEndAmPm, npEndHour, npEndMinute)

        // 버튼 동작들
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        // ✅ 삭제 버튼 → 확인 다이얼로그
        btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        btnConfirm.setOnClickListener {
            val startTime = formatTimeFromPickers(npStartAmPm, npStartHour, npStartMinute)
            val endTime = formatTimeFromPickers(npEndAmPm, npEndHour, npEndMinute)

            if (!isTimeRangeValid(startTime, endTime)) {
                Toast.makeText(requireContext(), "종료 시간이 시작 시간보다 빠릅니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onUpdated(startTime, endTime)
            dismiss()
        }
    }

    // ----------------------------------------------------------
    // 삭제 확인 다이얼로그
    // ----------------------------------------------------------
    private fun showDeleteConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("일정을 삭제하시겠습니까?")
            .setMessage("일정이 타임라인에서 삭제됩니다")
            .setPositiveButton("삭제") { _, _ ->
                // 실제 삭제 콜백 실행 (RoomScheduleTestActivity 쪽에서
                // 일정 제거 + 위시리스트로 이동 처리)
                onDeleted()
                dismiss()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor("#FF3B30"))
        }

        dialog.show()
    }

    // ----------------------------------------------------------
    // 시간 관련 유틸들
    // ----------------------------------------------------------
    private fun setupTimePickers(
        npAmPm: NumberPicker,
        npHour: NumberPicker,
        npMinute: NumberPicker
    ) {
        npAmPm.minValue = 0
        npAmPm.maxValue = 1
        npAmPm.displayedValues = arrayOf("오전", "오후")

        npHour.minValue = 1
        npHour.maxValue = 12

        npMinute.minValue = 0
        npMinute.maxValue = 1
        npMinute.displayedValues = arrayOf("00", "30")
    }

    private fun applyTimeToPickers(
        time: String,
        npAmPm: NumberPicker,
        npHour: NumberPicker,
        npMinute: NumberPicker
    ) {
        // time: "HH:mm"
        val parts = time.split(":")
        if (parts.size != 2) return

        val hour24 = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val amPmIndex = if (hour24 >= 12) 1 else 0
        var hour12 = hour24 % 12
        if (hour12 == 0) hour12 = 12

        val minuteIndex = if (minute >= 30) 1 else 0

        npAmPm.value = amPmIndex
        npHour.value = hour12
        npMinute.value = minuteIndex
    }

    private fun formatTimeFromPickers(
        npAmPm: NumberPicker,
        npHour: NumberPicker,
        npMinute: NumberPicker
    ): String {
        val amPmIndex = npAmPm.value      // 0: 오전, 1: 오후
        val hour12 = npHour.value        // 1~12
        val minuteIndex = npMinute.value // 0 or 1

        val minute = if (minuteIndex == 0) 0 else 30

        var hour = hour12 % 12
        if (amPmIndex == 1) {
            hour += 12
        }
        return String.format("%02d:%02d", hour, minute)
    }

    private fun extractEndTime(timeRange: String): String {
        // "14:00 ~ 16:00" → "16:00"
        val parts = timeRange.split("~")
        if (parts.size != 2) return schedule.timeLabel
        return parts[1].trim()
    }

    private fun isTimeRangeValid(start: String, end: String): Boolean {
        fun toMinutes(t: String): Int {
            val p = t.split(":")
            if (p.size != 2) return 0
            val h = p[0].toIntOrNull() ?: 0
            val m = p[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return toMinutes(end) > toMinutes(start)
    }
}
