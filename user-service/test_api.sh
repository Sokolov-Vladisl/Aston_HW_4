#!/bin/bash

# Базовый URL API
BASE_URL="http://localhost:8080/api/users"

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция паузы
pause() {
    echo -e "${YELLOW}⏳ Пауза $1 секунд...${NC}"
    sleep $1
    echo
}

# Функция для красивого вывода
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ УСПЕХ: $2${NC}"
    else
        echo -e "${RED}✗ ОШИБКА: $2${NC}"
    fi
}

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}=== Тестирование User Service API ===${NC}"
echo -e "${BLUE}========================================${NC}"

pause 2

# 1. Создание первого пользователя
echo -e "${BLUE}1. Создаем пользователя Alice...${NC}"
response1=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Smith","email":"alice@example.com","age":25}' \
  -w "%{http_code}")

http_code1=${response1: -3}
if [ "$http_code1" -eq 201 ]; then
    print_result 0 "Пользователь Alice создан"
    # Извлекаем ID из ответа
    user1_id=$(echo "${response1%???}" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "ID Alice: $user1_id"
    echo "Полный ответ:"
    echo "${response1%???}" | python -m json.tool 2>/dev/null || echo "${response1%???}"
else
    print_result 1 "Не удалось создать пользователя Alice"
    echo "Код ответа: $http_code1"
    echo "Response: $response1"
fi

pause 3

# 2. Создание второго пользователя
echo -e "${BLUE}2. Создаем пользователя Bob...${NC}"
response2=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Johnson","email":"bob@example.com","age":30}' \
  -w "%{http_code}")

http_code2=${response2: -3}
if [ "$http_code2" -eq 201 ]; then
    print_result 0 "Пользователь Bob создан"
    user2_id=$(echo "${response2%???}" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "ID Bob: $user2_id"
    echo "Полный ответ:"
    echo "${response2%???}" | python -m json.tool 2>/dev/null || echo "${response2%???}"
else
    print_result 1 "Не удалось создать пользователя Bob"
    echo "Код ответа: $http_code2"
    echo "Response: $response2"
fi

pause 2

# 3. Получение всех пользователей
echo -e "${BLUE}3. Получаем всех пользователей...${NC}"
response3=$(curl -s -X GET "$BASE_URL")
echo "Все пользователи:"
echo "$response3" | python -m json.tool 2>/dev/null || echo "$response3"

pause 2

# 4. Получение пользователя по ID
if [ ! -z "$user1_id" ]; then
    echo -e "${BLUE}4. Получаем пользователя с ID $user1_id...${NC}"
    response4=$(curl -s -X GET "$BASE_URL/$user1_id")
    echo "Пользователь $user1_id:"
    echo "$response4" | python -m json.tool 2>/dev/null || echo "$response4"
else
    echo -e "${YELLOW}⚠ Пропускаем шаг 4 - ID Alice не получен${NC}"
fi

pause 2

# 5. Обновление пользователя
if [ ! -z "$user1_id" ]; then
    echo -e "${BLUE}5. Обновляем пользователя Alice...${NC}"
    response5=$(curl -s -X PUT "$BASE_URL/$user1_id" \
      -H "Content-Type: application/json" \
      -d '{"name":"Alice Brown","email":"alice.brown@example.com","age":26}' \
      -w "%{http_code}")

    http_code5=${response5: -3}
    if [ "$http_code5" -eq 200 ]; then
        print_result 0 "Пользователь Alice обновлен"
        echo "Обновленные данные:"
        echo "${response5%???}" | python -m json.tool 2>/dev/null || echo "${response5%???}"
    else
        print_result 1 "Не удалось обновить пользователя"
        echo "Код ответа: $http_code5"
        echo "Response: $response5"
    fi
else
    echo -e "${YELLOW}⚠ Пропускаем шаг 5 - ID Alice не получен${NC}"
fi

pause 2

# 6. Удаление пользователя
if [ ! -z "$user2_id" ]; then
    echo -e "${BLUE}6. Удаляем пользователя Bob...${NC}"
    response6=$(curl -s -X DELETE "$BASE_URL/$user2_id" -w "%{http_code}")

    if [ "$response6" -eq 204 ]; then
        print_result 0 "Пользователь Bob удален"
    else
        print_result 1 "Не удалось удалить пользователя"
        echo "Код ответа: $response6"
    fi
else
    echo -e "${YELLOW}⚠ Пропускаем шаг 6 - ID Bob не получен${NC}"
fi

pause 2

# 7. Финальная проверка
echo -e "${BLUE}7. Финальный список пользователей:${NC}"
final_response=$(curl -s -X GET "$BASE_URL")
if [ -z "$final_response" ]; then
    echo "Список пуст"
else
    echo "$final_response" | python -m json.tool 2>/dev/null || echo "$final_response"
fi

pause 1

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}=== Тестирование завершено ===${NC}"
echo -e "${BLUE}========================================${NC}"