package com.stg.sikboo.ingredient.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.stg.sikboo.ingredient.domain.Ingredient;
import com.stg.sikboo.ingredient.domain.IngredientLocation;
import com.stg.sikboo.ingredient.domain.IngredientRepository;
import com.stg.sikboo.ingredient.dto.request.CreateIngredientRequestDTO;
import com.stg.sikboo.ingredient.dto.request.UpdateIngredientRequestDTO;
import com.stg.sikboo.ingredient.dto.response.IngredientResponseDTO;
import com.stg.sikboo.ingredient.dto.response.PageResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientService {

    private final IngredientRepository repo;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 목록 조회 */
    public PageResponseDTO<IngredientResponseDTO> list(
            Long memberId, IngredientLocation location, String q,
            int page, int size, String sort, String order
    ) {
        Sort s = sort(sort, order);
        String loc = (location == null) ? null : location.name();                 // ★ 변경: enum → String
        Page<Ingredient> result = repo.search(
                memberId,
                loc,                                                               // ★ 변경
                emptyToNull(q),
                PageRequest.of(page, size, s)
        );
        Page<IngredientResponseDTO> mapped = result.map(i -> IngredientResponseDTO.from(i, KST));
        return PageResponseDTO.from(mapped);
    }

    /** 단건 조회 */
    public IngredientResponseDTO get(Long memberId, Long id) {
        Ingredient i = repo.findByIdAndMemberId(id, memberId).orElseThrow(NotFound::new);
        return IngredientResponseDTO.from(i, KST);
    }

    /** 생성 */
    public Long create(Long memberId, CreateIngredientRequestDTO req) {
        if (req == null) throw badRequest("요청 본문이 비어있습니다.");
        if (isBlank(req.ingredientName())) throw badRequest("ingredientName 은 필수입니다.");
        if (req.location() == null) throw badRequest("location 은 필수입니다.");
        if (isBlank(req.due())) throw badRequest("due(YYYY-MM-DD) 는 필수입니다.");

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(req.due());
        } catch (DateTimeParseException e) {
            throw badRequest("due 형식이 올바르지 않습니다. 예: 2025-11-30");
        }

        LocalDateTime dueDateTime = dueDate.atStartOfDay(KST).toLocalDateTime();

        // 중복 검사
        LocalDateTime start = dueDate.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime end   = start.plusDays(1);
        var candidates = repo.findDupCandidates(
                memberId,
                req.location().name(),                                             // ★ 변경: enum → String
                start, end
        );
        String newNorm = norm(req.ingredientName());
        boolean dup = candidates.stream().anyMatch(c -> norm(c.getIngredientName()).equals(newNorm));
        if (dup) throw new Duplicate(req.ingredientName(), req.location().name(), req.due());

        Ingredient saved = repo.save(Ingredient.builder()
                .memberId(memberId)
                .ingredientName(req.ingredientName())
                .location(req.location())
                .due(dueDateTime)
                .memo(req.memo())
                .build());
        return saved.getId();
    }

    /** 수정 */
    public IngredientResponseDTO update(Long memberId, Long id, UpdateIngredientRequestDTO req) {
        Ingredient i = repo.findByIdAndMemberId(id, memberId).orElseThrow(NotFound::new);

        String newName = (req != null && !isBlank(req.ingredientName())) ? req.ingredientName() : i.getIngredientName();
        IngredientLocation newLoc = (req != null && req.location() != null) ? req.location() : i.getLocation();

        LocalDate newDueDate;
        if (req != null && !isBlank(req.due())) {
            try { newDueDate = LocalDate.parse(req.due()); }
            catch (DateTimeParseException e) { throw badRequest("due 형식이 올바르지 않습니다. 예: 2025-11-30"); }
        } else {
            newDueDate = i.getDue().toLocalDate();
        }
        LocalDateTime newDueDateTime = newDueDate.atStartOfDay(KST).toLocalDateTime();

        // 자신 제외 중복 검사
        LocalDateTime start = newDueDate.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime end   = start.plusDays(1);
        var candidates = repo.findDupCandidates(
                memberId,
                newLoc.name(),                                                     // ★ 변경: enum → String
                start, end
        );
        String newNorm = norm(newName);
        boolean dup = candidates.stream()
                .anyMatch(c -> !c.getId().equals(id) && norm(c.getIngredientName()).equals(newNorm));
        if (dup) throw new Duplicate(newName, newLoc.name(), newDueDate.toString());

        i.setIngredientName(newName);
        i.setLocation(newLoc);
        i.setDue(newDueDateTime);
        if (req != null && req.memo() != null) i.setMemo(req.memo());

        return IngredientResponseDTO.from(i, KST);
    }

    /** 삭제 */
    public void delete(Long memberId, Long id) {
        Ingredient i = repo.findByIdAndMemberId(id, memberId).orElseThrow(NotFound::new);
        repo.delete(i);
    }

    // ---------- helpers ----------
    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    private static Sort sort(String sort, String order) {
        Sort.Direction dir = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        if ("name".equalsIgnoreCase(sort) || "ingredientName".equalsIgnoreCase(sort)) {
            return Sort.by(new Sort.Order(dir, "ingredientName"), Sort.Order.asc("due"));
        }
        return Sort.by(Sort.Order.asc("due"), Sort.Order.asc("ingredientName"));
    }

    private static String norm(String s) {
        if (s == null) return "";
        String t = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
        t = t.toLowerCase(java.util.Locale.ROOT);
        return t.replaceAll("\\s+", "");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends RuntimeException {}

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class Duplicate extends RuntimeException {
        public Duplicate(String name, String loc, String due) {
            super("Duplicate: " + name + " / " + loc + " / " + due);
        }
    }
}
