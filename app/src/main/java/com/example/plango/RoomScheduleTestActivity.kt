// 패키지 이름은 네 프로젝트에 맞게 수정해줘!
package com.example.plango  // <-- 여기를 네 실제 패키지명으로 변경

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.R  // 패키지명 바꾸면 여기 R도 같이 바뀜

class RoomScheduleTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 그냥 fragment_room_schedule 레이아웃을 통째로 Activity 화면으로 사용
        setContentView(R.layout.fragment_room_schedule)
    }
}
