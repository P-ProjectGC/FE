# 🌱 PlanGo FE Branch Rules (브랜치 규칙)

PlanGo 프론트엔드 레포는 `main`, `dev`, `feat/*`, `fix/*`, `design/*`, `refactor/*` 브랜치 전략을 사용합니다.  
특히 기능 개발 시 GitHub Issue 번호를 포함한 네이밍을 사용합니다: feat/#이슈번호/기능명

---

# 1. 🔵 main
- 최종 결과물(배포/데모용) 저장 브랜치
- 직접 push 금지
- dev 브랜치에서 PR을 통해 merge

---

# 2. 🟣 dev
- 프론트 전체 기능이 통합되는 브랜치
- 모든 기능 브랜치(feat/*)는 dev 기준으로 생성
- dev 안정화 후 main으로 merge

---

# 3. 🟢 feat/*
기능(UI) 단위 개발 브랜치  
하나의 기능 또는 화면 단위로 생성합니다.  
GitHub Issue 번호 포함 네이밍을 사용합니다.

브랜치 네이밍 규칙:
feat/#이슈번호/기능명

예시:
feat/#1/login-ui  
feat/#2/home-ui  
feat/#3/onboarding  
feat/#4/plan-list-ui  
feat/#5/place-search-ui  

브랜치 생성:
git checkout dev  
git checkout -b feat/#이슈번호/기능명

---

# 4. 🟠 fix/*
버그 수정 브랜치  
버그 이슈 번호를 포함한 네이밍을 사용합니다.

네이밍 규칙:
fix/#이슈번호/버그명

예시:
fix/#12/login-padding  
fix/#15/home-crash

---

# 5. 🎨 design/*
디자인 및 스타일 변경 전용 브랜치

네이밍 규칙:
design/#이슈번호/내용

예시:
design/#7/button-style  
design/#10/colors-update

---

# 6. 🟤 refactor/*
코드 구조만 정리하는 브랜치 (기능 변화 없음)

네이밍 규칙:
refactor/#이슈번호/내용

예시:
refactor/#18/navigation-cleanup  
refactor/#21/home-structure

---

# 7. 📌 브랜치 네이밍 규칙 정리

feat      → 새 UI/기능 추가 → feat/#이슈번호/기능명  
fix       → 버그 수정       → fix/#이슈번호/버그명  
design    → UI/스타일 작업  → design/#이슈번호/내용  
refactor  → 코드 구조 정리  → refactor/#이슈번호/내용  

규칙:
- 전부 소문자
- 단어는 하이픈(-) 사용
- 이슈 번호(#번호)는 반드시 포함
- 기능명/내용은 짧고 명확하게 작성

---

# ✔ 최종 정리
main → 최종 완성본  
dev → 전체 기능 통합  
feat/#번호/기능명 → 기능(UI) 개발  
fix/#번호/내용 → 버그 수정  
design/#번호/내용 → 디자인·스타일 작업  
refactor/#번호/내용 → 리팩토링
