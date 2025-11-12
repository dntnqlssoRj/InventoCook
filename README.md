# InventoCook 프로젝트

냉장고 재고 관리 애플리케이션 UI입니다.

## 실행 방법

### 방법 : 터미널에서 실행 (IntelliJ/VS Code 등 IDE 실행(권장)

1. 컴파일:
javac -d out src/InventoCookUI.java


2. 실행:
java -cp out InventoCookUI

## 개발 포인트
	•	JTable + DefaultTableModel 활용한 데이터 중심 UI
	•	TableCellRenderer 오버라이드로 D-Day 컬러링 처리
	•	RowFilter + TableRowSorter로 검색/필터/정렬 통합 구현
	•	CardLayout + Stack 기반 뒤로가기로 부드러운 화면 전환
	•	BoxLayout + FlowLayout 조합으로 반응형 UI 구현

## 필요한 라이브러리

- JFreeChart 1.5.3 (lib/jfreechart-1.5.3.jar)
- JCommon 1.0.24 (lib/jcommon-1.0.24.jar)

라이브러리는 `lib/` 폴더에 자동으로 다운로드되어 있습니다.

## 향후 계획
	•	JSON/CSV 파일 저장 및 불러오기
	•	임박 재료 기반 레시피 추천
	•	단축키 및 키보드 내비게이션
	•	사용자 설정값 저장(임박 기준, 테마 등)
	•	i18n 다국어 지원

## 요구사항

- Java JDK 8 이상









<<<<<<< HEAD
=======

>>>>>>> 3bbc11b (Initial commit)
