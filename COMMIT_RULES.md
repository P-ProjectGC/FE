# 📝 PlanGo FE Commit Rules (커밋 규칙)

PlanGo FE 레포에서는 협업의 일관성과 추적성을 위해 다음 커밋 메시지 규칙을 사용합니다.

---

# 1. 커밋 메시지 기본 형식
<type>: <작업 요약>

예시:
feat: 로그인 화면 UI 구현
fix: 홈 카드 정렬 오류 수정
design: 공통 버튼 스타일 적용
docs: 브랜치 규칙 문서 추가

---

# 2. 커밋 타입(type) 목록

feat      : 새로운 기능/UI 추가  
fix       : 버그 수정  
design    : UI 스타일 변경 (색상, 폰트, 레이아웃 수정 등)  
style     : 코드 포맷 변경 (줄바꿈, 공백 등 — 로직 변화 없음)  
refactor  : 코드 리팩토링 (기능 변화 없음)  
docs      : 문서 작업(.md 파일 등)  
chore     : 빌드/환경 설정, 패키지 추가 등 기타 작업  

---

# 3. 커밋 작성 원칙

- 한 커밋에는 하나의 목적만 포함  
- 커밋 메시지는 간결하지만 명확하게  
- UI 변경 시 feat / design 구분  
- 포맷팅만 수정 시 style 사용  

---

# 4. 커밋 예시 모음

feat 예시:
feat: 온보딩 화면 UI 구성
feat: 여행 일정 리스트 RecyclerView 추가
feat: 장소 검색 필터 기능 구현

fix 예시:
fix: 로그인 시 EditText 포커스 오류 해결
fix: 홈 화면 무한 스크롤 크래시 수정

design 예시:
design: 기본 버튼 라운드 스타일 수정
design: 공통 폰트 스타일 적용

style 예시:
style: 불필요한 공백 제거
style: import 정리

refactor 예시:
refactor: PlanDetailFragment 구조 정리
refactor: navigation 코드 개선

docs 예시:
docs: 커밋 규칙 문서 추가
docs: README 업데이트

chore 예시:
chore: Gradle 버전 업데이트
chore: 이미지 asset 추가

---

# ✔ 핵심 요약
- <type>: <내용> 형식 고정  
- 하나의 커밋 = 하나의 목적  
- UI 변경은 feat / design 구분  
- 메시지는 명확하게 작성
