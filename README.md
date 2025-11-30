InventoCook 프로젝트

냉장고 재고 관리 + 레시피 추천 데스크톱 애플리케이션

Java Swing 기반 UI를 사용하여
냉장고 속 재료를 등록/수정/삭제하고,
보유 재료 기반으로 자동 레시피 추천까지 제공하는 프로젝트입니다.

재료와 레시피는 모두 MySQL DB에 저장되며 비휘발성입니다.

⸻

실행 방법

1) IDE 실행 (IntelliJ / VS Code) — 권장
	1.	프로젝트 열기
	2.	InventoCookUI.main() 실행
	3.	MySQL 연결 성공 시 프로그램 자동 실행

⸻

2) 터미널에서 실행
	1.	컴파일: javac -cp "lib/*" -d out src/*.java

	2.	실행: java -cp "out:lib/*" InventoCookUI

※ macOS는 : / Windows는 ; 사용

⸻

MySQL 설정 (비휘발성 저장 필수)

1) 데이터베이스 생성

CREATE DATABASE inventocook;
USE inventocook;

2) 테이블 생성

CREATE TABLE inventory_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  location VARCHAR(50) NOT NULL,
  quantity INT NOT NULL,
  expiry_date DATE NOT NULL
);

3) 레시피/재료 기본 데이터 삽입

mysql -u root -p inventocook < db/schema.sql
mysql -u root -p inventocook < db/seed_recipes.sql


⸻

주요 기능

1. 인벤토리(재료) 관리
	•	재료 추가 / 수정 / 삭제
	•	MySQL 기반 비휘발성 저장
	•	유통기한 기반 D-Day 자동 계산
	•	테이블 색상 렌더링(D+ 빨강 / 임박 노랑 / 여유 초록)
	•	검색 + 카테고리 필터 + 보관 위치 필터 지원
	•	정렬 (유통기한 / 이름 / 수량)

⸻

2. 긴급 추천 메뉴

보유 재료 기반으로 레시피 자동 분석:
	•	매칭률(%) 자동 계산
	•	보유 재료 / 필요 재료 / 부족 재료 수 표시
	•	임박 재료 자동 가중치 → 추천 우선순위 적용
	•	레시피 더블클릭 → 필요한 재료 상세 보기
	•	다양한 국가/카테고리 레시피 포함

⸻

3. 유통기한 임박 알림
	•	D-3 이하(임박) 또는 D+ (경과) 재료 자동 분류
	•	상단 뱃지로 즉시 표시
	•	알림 패널에서 한 번에 확인 가능

⸻

개발 포인트 (핵심 기술)
	•	JTable + DefaultTableModel 기반 데이터 중심 UI
	•	TableCellRenderer 오버라이드 → D-Day 컬러링
	•	RowFilter + TableRowSorter → 검색/필터/정렬 통합
	•	CardLayout → 부드러운 화면 전환
	•	Stack 기반 뒤로가기 구현
	•	BoxLayout + FlowLayout 조합으로 반응형 UI
	•	DB 연동(JDBC): CRUD 작업을 테이블과 MySQL에 동시 반영

⸻

필요한 라이브러리
	•	JFreeChart 1.5.3
	•	JCommon 1.0.24

라이브러리는 lib/ 폴더에 포함되어 있으며
클래스패스에 자동 로드됩니다.

⸻

향후 계획
	•	JSON/CSV 기반 백업/불러오기
	•	OCR 영수증 인식 → 자동 재고 업데이트
	•	레시피 카테고리 자동 분류 모델
	•	다크 모드 / 사용자 테마
	•	다국어(i18n) 지원
	•	추천 알고리즘 고도화 (사용자 취향 학습)

⸻

요구사항
	•	Java JDK 8 이상
	•	MySQL 8.0 이상
	•	JDBC 드라이버 포함
