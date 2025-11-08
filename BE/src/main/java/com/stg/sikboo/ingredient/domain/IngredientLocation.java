package com.stg.sikboo.ingredient.domain;

/**
 * 보관 위치 ENUM
 *
 * 직렬화/역직렬화
 * - JSON 요청/응답에서 아래 문자열 그대로 사용됨(예: "냉장고")
 * - 클라이언트가 다른 문자열을 보내면 400(HttpMessageNotReadableException) 발생
 *
 * 예시 요청(JSON)
 * { "ingredientName":"대파", "location":"냉장고", "due":"2025-11-30" }
 *
 * enum 변경 시 주의
 * - DB ENUM 타입(ingredient_location)에도 동일한 레이블이 존재해야 함
 * - 레이블 추가/변경은 DB 타입 수정 + 코드 동시 반영 필요
 */
public enum IngredientLocation {
    냉장고, 냉동실, 실온
}
