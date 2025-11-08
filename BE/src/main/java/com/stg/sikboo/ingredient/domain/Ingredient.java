package com.stg.sikboo.ingredient.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL BIGSERIAL과 호환
    @Column(name = "ingredient_id")
    private Long id;

    /** 소유자(회원) 식별자 — 항상 서버가 JWT에서 추출한 memberId로 세팅 */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /** 재료명 — 중복 판정 시 정규화(NFKC→소문자→공백제거)하여 비교 */
    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName;

    /**
     * 보관 위치
     * - 자바 enum ↔ DB ENUM(ingredient_location) 문자열 매핑
     * - 레이블(문자열)값이 정확히 일치해야 직렬화/역직렬화 오류가 없음
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false, columnDefinition = "ingredient_location")
    private IngredientLocation location;

    /**
     * 소비기한(유통기한)
     * - DB에는 TIMESTAMP
     * - 서비스 레이어에서 KST(Asia/Seoul) 자정으로 저장 → 날짜 연산의 일관성 확보
     * - 응답 DTO에서는 "YYYY-MM-DD" 문자열로 노출
     */
    @Column(name = "due", nullable = false)
    private LocalDateTime due;

    /** 메모 — 선택 입력 */
    @Column(name = "memo", length = 255)
    private String memo;
}
