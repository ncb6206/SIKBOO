// src/main/java/com/stg/sikboo/Ingredient/dto/request/CreateIngredientRequestDTO.java
package com.stg.sikboo.ingredient.dto.request;

import com.stg.sikboo.ingredient.domain.IngredientLocation;

/**
 * 재료 생성 요청 DTO
 *
 * 검증
 * - 필수값: ingredientName, location, due
 * - due 형식: "YYYY-MM-DD" (서비스에서 수동 검증 → 잘못되면 400)
 *
 * 예시 요청(JSON)
 * {
 *   "ingredientName": "대파",
 *   "location": "냉장고",
 *   "due": "2025-11-30",
 *   "memo": "볶음용"           // 선택
 * }
 *
 * 보안
 * - memberId는 클라이언트가 보내지 않음. 서버가 JWT에서 추출하여 주입(오너십 보장)
 */
public record CreateIngredientRequestDTO(
        String ingredientName,
        IngredientLocation location,
        String due,   // "YYYY-MM-DD"
        String memo
) {}
