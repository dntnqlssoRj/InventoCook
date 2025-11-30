-- 해외 레시피용 seed (외국 요리)

USE inventocook;

-- --------------------------------------------------
-- 1. 해외 요리용 추가 재료 (이미 있는 건 INSERT IGNORE라 무시됨)
-- --------------------------------------------------
INSERT IGNORE INTO ingredients (name) VALUES
('올리브유'),
('베이컨'),
('치즈'),
('양상추'),
('토마토'),
('피클'),
('케첩'),
('머스터드'),
('밀가루'),
('우유'),
('베이킹파우더'),
('메이플시럽'),
('토르티야'),
('사워크림'),
('살사소스'),
('통조림콩'),
('햄버거번'),
('닭가슴살'),
('라임');

-- --------------------------------------------------
-- 2. 해외 레시피 목록 (나라별 2개씩)
-- --------------------------------------------------
INSERT INTO recipes (name, description) VALUES
('볼로네즈 파스타', '[이탈리아] 다진 소고기와 토마토 소스로 만드는 파스타'),
('카르보나라 파스타', '[이탈리아] 크림 없이 계란과 치즈로 만드는 파스타'),
('치즈버거', '[미국] 소고기 패티와 치즈를 넣은 햄버거'),
('클래식 팬케이크', '[미국] 아침 식사용 기본 팬케이크'),
('규동', '[일본] 양파와 소고기를 간장 베이스 양념에 졸인 덮밥'),
('일본식 카레라이스', '[일본] 카레 블록/카레가루로 만드는 부드러운 카레 덮밥'),
('치킨 타코', '[멕시코] 토르티야에 닭고기와 채소를 넣은 타코'),
('부리또', '[멕시코] 밥과 콩, 고기를 또르티야에 싸서 만든 부리또');

-- --------------------------------------------------
-- 3. 레시피 ↔ 재료 매핑 (해외 레시피)
--   amount, unit 은 지금 안 써서 NULL
-- --------------------------------------------------

-- [이탈리아] 볼로네즈 파스타
-- 파스타면, 소고기, 양파, 마늘, 당근, 토마토, 토마토소스(대신 토마토+케첩), 올리브유, 소금, 후추
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('파스타면','소고기','양파','마늘','당근','토마토','올리브유','소금','후추','케첩')
WHERE r.name = '볼로네즈 파스타';

-- [이탈리아] 카르보나라 파스타
-- 파스타면, 베이컨, 계란, 치즈, 양파, 마늘, 올리브유, 소금, 후추
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('파스타면','베이컨','계란','치즈','양파','마늘','올리브유','소금','후추')
WHERE r.name = '카르보나라 파스타';

-- [미국] 치즈버거
-- 햄버거번, 소고기, 치즈, 양상추, 토마토, 양파, 피클, 케첩, 머스터드
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('햄버거번','소고기','치즈','양상추','토마토','양파','피클','케첩','머스터드')
WHERE r.name = '치즈버거';

-- [미국] 클래식 팬케이크
-- 밀가루, 우유, 계란, 설탕, 베이킹파우더, 버터, 소금, 메이플시럽
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밀가루','우유','계란','설탕','베이킹파우더','버터','소금','메이플시럽')
WHERE r.name = '클래식 팬케이크';

-- [일본] 규동
-- 밥, 소고기, 양파, 간장, 설탕, 맛술, 마늘, 대파
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밥','소고기','양파','간장','설탕','맛술','마늘','대파')
WHERE r.name = '규동';

-- [일본] 일본식 카레라이스
-- 밥, 카레가루, 감자, 당근, 양파, 돼지고기, 물
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('밥','카레가루','감자','당근','양파','돼지고기','물')
WHERE r.name = '일본식 카레라이스';

-- [멕시코] 치킨 타코
-- 토르티야, 닭가슴살, 양상추, 토마토, 양파, 치즈, 사워크림, 살사소스, 라임
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('토르티야','닭가슴살','양상추','토마토','양파','치즈','사워크림','살사소스','라임')
WHERE r.name = '치킨 타코';

-- [멕시코] 부리또
-- 토르티야, 밥, 소고기, 양파, 통조림콩, 옥수수콘, 치즈, 살사소스
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit)
SELECT r.id, i.id, NULL, NULL
FROM recipes r
JOIN ingredients i ON i.name IN ('토르티야','밥','소고기','양파','통조림콩','옥수수콘','치즈','살사소스')
WHERE r.name = '부리또';