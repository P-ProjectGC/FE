package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmScheduleBottomSheet(
    private val place: WishlistPlaceItem,
    private val days: List<TravelDailySchedule>,
    private val onConfirmed: (dayIndex: Int, startTime: String, endTime: String) -> Unit
) : BottomSheetDialogFragment() {

    private var selectedDayIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_confirm_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textPlaceName = view.findViewById<TextView>(R.id.textPlaceName)
        val textAddress = view.findViewById<TextView>(R.id.textAddress)
        val spinnerDay = view.findViewById<Spinner>(R.id.spinnerDay)

        val npStartAmPm = view.findViewById<NumberPicker>(R.id.npStartAmPm)
        val npStartHour = view.findViewById<NumberPicker>(R.id.npStartHour)
        val npStartMinute = view.findViewById<NumberPicker>(R.id.npStartMinute)

        val npEndAmPm = view.findViewById<NumberPicker>(R.id.npEndAmPm)
        val npEndHour = view.findViewById<NumberPicker>(R.id.npEndHour)
        val npEndMinute = view.findViewById<NumberPicker>(R.id.npEndMinute)

        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)

        textPlaceName.text = place.placeName
        textAddress.text = place.address

        // 날짜 스피너
        val dayTitles = days.map { it.dayTitle }
        val dayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dayTitles
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerDay.adapter = dayAdapter
        spinnerDay.setSelection(0)
        selectedDayIndex = 0

        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                v: View?,
                position: Int,
                id: Long
            ) {
                selectedDayIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ===== 시간 NumberPicker 설정 =====
        setupTimePickers(
            npStartAmPm, npStartHour, npStartMinute,
            defaultAmPm = 1,   // 오후
            defaultHour = 2,   // 2시
            defaultMinuteIndex = 0 // 00분
        )
        setupTimePickers(
            npEndAmPm, npEndHour, npEndMinute,
            defaultAmPm = 1,   // 오후
            defaultHour = 4,   // 4시
            defaultMinuteIndex = 0
        )

        btnCancel.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            // 1) 총 분 단위로 변환해서 비교
            val startTotalMinutes = getTotalMinutesFromPickers(
                npStartAmPm, npStartHour, npStartMinute
            )
            val endTotalMinutes = getTotalMinutesFromPickers(
                npEndAmPm, npEndHour, npEndMinute
            )

            // 종료 시간이 시작 시간보다 같거나 이르면 막기
            if (endTotalMinutes <= startTotalMinutes) {
                Toast.makeText(
                    requireContext(),
                    "종료 시간은 시작 시간보다 늦어야 합니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 2) 유효하면 문자열 포맷 후 콜백 호출
            val startTime = formatTimeFromPickers(npStartAmPm, npStartHour, npStartMinute)
            val endTime = formatTimeFromPickers(npEndAmPm, npEndHour, npEndMinute)

            onConfirmed(selectedDayIndex, startTime, endTime)
            dismiss()
        }
    }

    private fun setupTimePickers(
        npAmPm: NumberPicker,
        npHour: NumberPicker,
        npMinute: NumberPicker,
        defaultAmPm: Int,
        defaultHour: Int,
        defaultMinuteIndex: Int
    ) {
        // 오전/오후
        npAmPm.minValue = 0
        npAmPm.maxValue = 1
        npAmPm.displayedValues = arrayOf("오전", "오후")

        // 1~12시
        npHour.minValue = 1
        npHour.maxValue = 12

        // 분: 00, 30
        npMinute.minValue = 0
        npMinute.maxValue = 1
        npMinute.displayedValues = arrayOf("00", "30")

        npAmPm.value = defaultAmPm
        npHour.value = defaultHour
        npMinute.value = defaultMinuteIndex
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

    // 시간 비교용: 00:00 기준 총 분 단위로 변환
    private fun getTotalMinutesFromPickers(
        npAmPm: NumberPicker,
        npHour: NumberPicker,
        npMinute: NumberPicker
    ): Int {
        val amPmIndex = npAmPm.value      // 0: 오전, 1: 오후
        val hour12 = npHour.value        // 1~12
        val minuteIndex = npMinute.value // 0 or 1

        val minute = if (minuteIndex == 0) 0 else 30

        var hour = hour12 % 12
        if (amPmIndex == 1) {
            hour += 12
        }
        return hour * 60 + minute
    }
}
