-- seed.sql : 기본 재료 + 레시피 + 레시피-재료 매핑

USE inventocook;

--------------------------------------------------
-- 1. 재료 목록 (ingredients)
--------------------------------------------------
INSERT IGNORE INTO ingredients (name) VALUES
('간장'),
('감자'),
('계란'),
('고기'),
('고추장'),
('고춧가루'),
('김가루'),
('김치'),
('깻잎'),
('당근'),
('대파'),
('돼지고기'),
('된장'),
('두부'),
('떡'),
('라면사리'),
('마늘'),
('맛술'),
('멸치'),
('미역'),
('부침가루'),
('부추'),
('버터'),
('밥'),
('소금'),
('소고기'),
('식용유'),
('식초'),
('시금치'),
('양배추'),
('양파'),
('어묵'),
('오이'),
('옥수수콘'),
('올리고당'),
('요거트'),
('우동사리'),
('진미채'),
('참기름'),
('참치캔'),
('카레가루'),
('콩나물'),
('파스타면'),
('햄'),
('후추'),
('스팸'),
('물'),
('마요네즈'),
('무'),
('설탕'),
('순두부'),
('애호박');

--------------------------------------------------
-- 2. 레시피 목록 (recipes)
--------------------------------------------------
INSERT INTO recipes (name, description) VALUES
('계란볶음밥', '자취 1인분 베이스 레시피 (재료 위주)'),
('참치김치볶음밥', '자취 1인분 베이스 레시피 (재료 위주)'),
('간장계란밥', '자취 1인분 베이스 레시피 (재료 위주)'),
('스팸김치찌개', '자취 1인분 베이스 레시피 (재료 위주)'),
('부대찌개', '자취 1인분 베이스 레시피 (재료 위주)'),
('대파파스타', '자취 1인분 베이스 레시피 (재료 위주)'),
('감자볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('두부간장조림', '자취 1인분 베이스 레시피 (재료 위주)'),
('제육볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('카레', '자취 1인분 베이스 레시피 (재료 위주)'),
('부침전(김치전/부추전)', '자취 1인분 베이스 레시피 (재료 위주)'),
('라면계란국', '자취 1인분 베이스 레시피 (재료 위주)'),
('양배추샐러드', '자취 1인분 베이스 레시피 (재료 위주)'),
('고추장볶음우동', '자취 1인분 베이스 레시피 (재료 위주)'),
('계란말이', '자취 1인분 베이스 레시피 (재료 위주)'),
('진미채볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('멸치볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('감자조림', '자취 1인분 베이스 레시피 (재료 위주)'),
('시금치나물', '자취 1인분 베이스 레시피 (재료 위주)'),
('콩나물무침', '자취 1인분 베이스 레시피 (재료 위주)'),
('어묵볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('김치볶음', '자취 1인분 베이스 레시피 (재료 위주)'),
('간장버터감자', '자취 1인분 베이스 레시피 (재료 위주)'),
('깻잎조림', '자취 1인분 베이스 레시피 (재료 위주)'),
('양배추무침', '자취 1인분 베이스 레시피 (재료 위주)'),
('오이무침', '자취 1인분 베이스 레시피 (재료 위주)'),
('달걀장', '자취 1인분 베이스 레시피 (재료 위주)'),
('계란국', '자취 1인분 베이스 레시피 (재료 위주)'),
('미역국', '자취 1인분 베이스 레시피 (재료 위주)'),
('된장국', '자취 1인분 베이스 레시피 (재료 위주)'),
('순두부찌개', '자취 1인분 베이스 레시피 (재료 위주)'),
('김치찌개', '자취 1인분 베이스 레시피 (재료 위주)'),
('어묵탕', '자취 1인분 베이스 레시피 (재료 위주)'),
('고추장찌개', '자취 1인분 베이스 레시피 (재료 위주)'),
('감자국', '자취 1인분 베이스 레시피 (재료 위주)'),
('콩나물국', '자취 1인분 베이스 레시피 (재료 위주)'),
('떡국', '자취 1인분 베이스 레시피 (재료 위주)'),
('우동국물', '자취 1인분 베이스 레시피 (재료 위주)');

--------------------------------------------------
-- 3. 레시피 ↔ 재료 매핑 (recipe_ingredients)
--   amount, unit 은 지금 안 쓰니까 전부 NULL
--   WHERE r.name = '...' 이 네가 적은 레시피 이름이랑 1:1로 맞음
--------------------------------------------------

-- 1. 계란볶음밥 – 밥, 계란, 대파, 간장, 식용유, 소금, 후추
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밥','계란','대파','간장','식용유','소금','후추')
WHERE r.name = '계란볶음밥';

-- 2. 참치김치볶음밥 – 밥, 김치, 참치캔, 대파, 고춧가루, 간장, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밥','김치','참치캔','대파','고춧가루','간장','식용유')
WHERE r.name = '참치김치볶음밥';

-- 3. 간장계란밥 – 밥, 계란, 간장, 참기름, 김가루
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밥','계란','간장','참기름','김가루')
WHERE r.name = '간장계란밥';

-- 4. 스팸김치찌개 – 스팸, 김치, 두부, 양파, 대파, 고춧가루, 마늘, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('스팸','김치','두부','양파','대파','고춧가루','마늘','물')
WHERE r.name = '스팸김치찌개';

-- 5. 부대찌개 – 라면사리, 햄/스팸, 김치, 두부, 양파, 고춧가루, 간장, 마늘, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('라면사리','햄','스팸','김치','두부','양파','고춧가루','간장','마늘','물')
WHERE r.name = '부대찌개';

-- 6. 대파파스타 – 파스타면, 대파, 마늘, 식용유/버터, 소금, 후추
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('파스타면','대파','마늘','식용유','버터','소금','후추')
WHERE r.name = '대파파스타';

-- 7. 감자볶음 – 감자, 양파, 식용유, 소금
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('감자','양파','식용유','소금')
WHERE r.name = '감자볶음';

-- 8. 두부간장조림 – 두부, 간장, 설탕, 마늘, 물, 대파
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('두부','간장','설탕','마늘','물','대파')
WHERE r.name = '두부간장조림';

-- 9. 제육볶음 – 돼지고기, 양파, 대파, 고추장, 고춧가루, 간장, 설탕, 마늘
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('돼지고기','양파','대파','고추장','고춧가루','간장','설탕','마늘')
WHERE r.name = '제육볶음';

-- 10. 카레 – 카레가루, 감자, 당근, 양파, 고기, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('카레가루','감자','당근','양파','고기','물')
WHERE r.name = '카레';

-- 11. 부침전(김치전/부추전) – 부침가루, 물, 김치 or 부추, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('부침가루','물','김치','부추','식용유')
WHERE r.name = '부침전(김치전/부추전)';

-- 12. 라면계란국 – 라면사리, 계란, 대파, 간장
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('라면사리','계란','대파','간장')
WHERE r.name = '라면계란국';

-- 13. 양배추샐러드 – 양배추, 마요네즈 or 요거트, 옥수수콘
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('양배추','마요네즈','요거트','옥수수콘')
WHERE r.name = '양배추샐러드';

-- 14. 고추장볶음우동 – 우동사리, 고추장, 간장, 설탕, 양파, 대파, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('우동사리','고추장','간장','설탕','양파','대파','식용유')
WHERE r.name = '고추장볶음우동';

-- 15. 계란말이 – 계란, 대파, 소금, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('계란','대파','소금','식용유')
WHERE r.name = '계란말이';

-- 16. 진미채볶음 – 진미채, 고추장, 고춧가루, 올리고당, 마요네즈
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('진미채','고추장','고춧가루','올리고당','마요네즈')
WHERE r.name = '진미채볶음';

-- 17. 멸치볶음 – 멸치, 간장, 설탕, 올리고당, 마늘, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('멸치','간장','설탕','올리고당','마늘','식용유')
WHERE r.name = '멸치볶음';

-- 18. 감자조림 – 감자, 간장, 설탕, 마늘, 대파, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('감자','간장','설탕','마늘','대파','식용유')
WHERE r.name = '감자조림';

-- 19. 시금치나물 – 시금치, 마늘, 참기름, 소금
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('시금치','마늘','참기름','소금')
WHERE r.name = '시금치나물';

-- 20. 콩나물무침 – 콩나물, 마늘, 대파, 소금, 참기름
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('콩나물','마늘','대파','소금','참기름')
WHERE r.name = '콩나물무침';

-- 21. 어묵볶음 – 어묵, 양파, 대파, 간장, 설탕, 식용유
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('어묵','양파','대파','간장','설탕','식용유')
WHERE r.name = '어묵볶음';

-- 22. 김치볶음 – 김치, 식용유, 설탕, 대파
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('김치','식용유','설탕','대파')
WHERE r.name = '김치볶음';

-- 23. 간장버터감자 – 감자, 버터, 간장, 설탕
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('감자','버터','간장','설탕')
WHERE r.name = '간장버터감자';

-- 24. 깻잎조림 – 깻잎, 간장, 마늘, 고춧가루, 대파, 설탕
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('깻잎','간장','마늘','고춧가루','대파','설탕')
WHERE r.name = '깻잎조림';

-- 25. 양배추무침 – 양배추, 고춧가루, 식초, 설탕, 소금
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('양배추','고춧가루','식초','설탕','소금')
WHERE r.name = '양배추무침';

-- 26. 오이무침 – 오이, 고춧가루, 식초, 설탕, 소금, 마늘
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('오이','고춧가루','식초','설탕','소금','마늘')
WHERE r.name = '오이무침';

-- 27. 달걀장 – 계란, 간장, 설탕, 대파, 마늘
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('계란','간장','설탕','대파','마늘')
WHERE r.name = '달걀장';

-- 28. 계란국 – 계란, 대파, 간장 또는 소금, 후추, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('계란','대파','간장','소금','후추','물')
WHERE r.name = '계란국';

-- 29. 미역국 – 미역, 소고기 or 참치, 마늘, 간장/소금, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('미역','소고기','참치캔','마늘','간장','소금','물')
WHERE r.name = '미역국';

-- 30. 된장국 – 된장, 두부, 애호박, 양파, 대파, 마늘, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('된장','두부','애호박','양파','대파','마늘','물')
WHERE r.name = '된장국';

-- 31. 순두부찌개 – 순두부, 계란, 고춧가루, 간장, 마늘, 대파, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('순두부','계란','고춧가루','간장','마늘','대파','물')
WHERE r.name = '순두부찌개';

-- 32. 김치찌개 – 김치, 돼지고기 or 참치, 두부, 양파, 마늘, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('김치','돼지고기','참치캔','두부','양파','마늘','물')
WHERE r.name = '김치찌개';

-- 33. 어묵탕 – 어묵, 무(선택), 대파, 간장, 후추, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('어묵','무','대파','간장','후추','물')
WHERE r.name = '어묵탕';

-- 34. 고추장찌개 – 고추장, 감자, 애호박, 양파, 대파, 마늘, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('고추장','감자','애호박','양파','대파','마늘','물')
WHERE r.name = '고추장찌개';

-- 35. 감자국 – 감자, 대파, 마늘, 소금, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('감자','대파','마늘','소금','물')
WHERE r.name = '감자국';

-- 36. 콩나물국 – 콩나물, 대파, 마늘, 소금, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('콩나물','대파','마늘','소금','물')
WHERE r.name = '콩나물국';

-- 37. 떡국 – 떡, 계란, 김가루, 간장, 대파, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('떡','계란','김가루','간장','대파','물')
WHERE r.name = '떡국';

-- 38. 우동국물 – 우동사리, 간장, 맛술, 대파, 후추, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('우동사리','간장','맛술','대파','후추','물')
WHERE r.name = '우동국물';