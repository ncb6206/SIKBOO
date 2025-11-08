// src/main/java/com/stg/sikboo/Ingredient/domain/IngredientRepository.java
package com.stg.sikboo.ingredient.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // 목록 + 검색 (네이티브)  ★ location은 String
    @Query(
      value = """
        SELECT i.*
        FROM ingredient i
        WHERE i.member_id = :memberId
          AND (:location IS NULL OR i.location::text = :location)
          AND (:q IS NULL OR lower(i.ingredient_name) LIKE lower(concat('%', :q, '%')))
        ORDER BY i.due ASC, i.ingredient_name ASC
      """,
      countQuery = """
        SELECT count(*)
        FROM ingredient i
        WHERE i.member_id = :memberId
          AND (:location IS NULL OR i.location::text = :location)
          AND (:q IS NULL OR lower(i.ingredient_name) LIKE lower(concat('%', :q, '%')))
      """,
      nativeQuery = true
    )
    Page<Ingredient> search(
        @Param("memberId") Long memberId,
        @Param("location") String location,   // ★ 여기 String
        @Param("q") String q,
        Pageable pageable
    );

    // 중복 후보 조회 (네이티브)  ★ location은 String
    @Query(
      value = """
        SELECT i.*
        FROM ingredient i
        WHERE i.member_id = :memberId
          AND i.location::text = :location
          AND i.due >= :start AND i.due < :end
      """,
      nativeQuery = true
    )
    List<Ingredient> findDupCandidates(
        @Param("memberId") Long memberId,
        @Param("location") String location,   // ★ 여기 String
        @Param("start") LocalDateTime startInclusive,
        @Param("end") LocalDateTime endExclusive
    );

    Optional<Ingredient> findByIdAndMemberId(Long id, Long memberId);
}
