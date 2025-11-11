# Demo1 프로젝트

이 프로젝트는 Java로 작성된 간트 차트 애플리케이션입니다.

## 실행 방법

### 방법 1: 스크립트 실행 (권장)
```bash
./run_gantt.sh
```

### 방법 2: 수동 실행

1. 컴파일:
```bash
javac -cp "lib/*:." src/GanttChartMaker.java -d out/production/Demo1
```

2. 실행:
```bash
java -cp "out/production/Demo1:lib/*" GanttChartMaker
```

## 포함된 프로그램

### GanttChartMaker
프로젝트 개발 일정을 간트 차트로 시각화하는 프로그램입니다.
- Week 9 ~ 15의 프로젝트 일정을 표시
- JFreeChart 라이브러리 사용

### InventoCookUI
냉장고 재고 관리 애플리케이션 UI입니다.

## 필요한 라이브러리

- JFreeChart 1.5.3 (lib/jfreechart-1.5.3.jar)
- JCommon 1.0.24 (lib/jcommon-1.0.24.jar)

라이브러리는 `lib/` 폴더에 자동으로 다운로드되어 있습니다.

## 요구사항

- Java JDK 8 이상









