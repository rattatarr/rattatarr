package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MediaItemSpecificationsTest {

    @Test
    void isMovie_shouldReturnSpecification() {
        Specification<MediaItem> spec = MediaItemSpecifications.isMovie();
        assertNotNull(spec);
    }

    @Test
    void isSeries_shouldReturnSpecification() {
        Specification<MediaItem> spec = MediaItemSpecifications.isSeries();
        assertNotNull(spec);
    }

    @Test
    void hasId_shouldReturnSpecificationWhenIdIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.hasId(null);
        assertNotNull(spec);
    }

    @Test
    void hasId_shouldReturnSpecificationWhenIdProvided() {
        UUID id = UUID.randomUUID();
        Specification<MediaItem> spec = MediaItemSpecifications.hasId(id);
        assertNotNull(spec);
    }

    @Test
    void titleLike_shouldReturnSpecificationWhenTitleIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.titleLike(null);
        assertNotNull(spec);
    }

    @Test
    void titleLike_shouldReturnSpecificationWhenTitleProvided() {
        Specification<MediaItem> spec = MediaItemSpecifications.titleLike("Test");
        assertNotNull(spec);
    }

    @Test
    void releasedAfter_shouldReturnSpecificationWhenYearIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.releasedAfter(null);
        assertNotNull(spec);
    }

    @Test
    void releasedAfter_shouldReturnSpecificationWhenYearProvided() {
        Specification<MediaItem> spec = MediaItemSpecifications.releasedAfter(2020);
        assertNotNull(spec);
    }

    @Test
    void releasedBefore_shouldReturnSpecificationWhenYearIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.releasedBefore(null);
        assertNotNull(spec);
    }

    @Test
    void releasedBefore_shouldReturnSpecificationWhenYearProvided() {
        Specification<MediaItem> spec = MediaItemSpecifications.releasedBefore(2020);
        assertNotNull(spec);
    }

    @Test
    void genres_shouldReturnSpecificationWithNullGenresAndNoFetch() {
        Specification<MediaItem> spec = MediaItemSpecifications.genres(null, false);
        assertNotNull(spec);
    }

    @Test
    void genres_shouldReturnSpecificationWithGenresAndNoFetch() {
        Set<String> genres = Set.of("Action", "Drama");
        Specification<MediaItem> spec = MediaItemSpecifications.genres(genres, false);
        assertNotNull(spec);
    }

    @Test
    void genres_shouldReturnSpecificationWithGenresAndFetch() {
        Set<String> genres = Set.of("Action");
        Specification<MediaItem> spec = MediaItemSpecifications.genres(genres, true);
        assertNotNull(spec);
    }

    @Test
    void missingCastOrCrew_shouldReturnSpecification() {
        Specification<MediaItem> spec = MediaItemSpecifications.missingCastOrCrew();
        assertNotNull(spec);
    }

    @Test
    void withMetadata_shouldReturnSpecification() {
        Specification<MediaItem> spec = MediaItemSpecifications.withMetadata();
        assertNotNull(spec);
    }

    // --- resolveRatingSort ---

    @Test
    void resolveRatingSort_withNoRatingSort_returnsOriginalPageableUnchanged() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "productionYear"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertEquals(pageable, result.pageable());
    }

    @Test
    void resolveRatingSort_withNoRatingSort_specIsNoOp() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("title"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertNull(result.spec().toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void resolveRatingSort_withRatingSortAndProfileId_returnsUnsortedPageable() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "ratings"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertFalse(result.pageable().getSort().isSorted());
    }

    @Test
    void resolveRatingSort_withRatingSortAndProfileId_preservesPageNumberAndSize() {
        Pageable pageable = PageRequest.of(2, 50, Sort.by("ratings"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertEquals(2, result.pageable().getPageNumber());
        assertEquals(50, result.pageable().getPageSize());
    }

    @Test
    void resolveRatingSort_withRatingSortAndProfileId_returnsNonNullSpec() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("ratings"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertNotNull(result.spec());
    }

    @Test
    void resolveRatingSort_withRatingSortNoProfileId_dropsRatingSortFromPageable() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "ratings"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, null);

        assertFalse(result.pageable().getSort().stream()
                .anyMatch(o -> o.getProperty().equalsIgnoreCase("ratings")));
    }

    @Test
    void resolveRatingSort_withRatingSortNoProfileId_specIsNoOp() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("ratings"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, null);

        assertNull(result.spec().toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void resolveRatingSort_withMixedSortsAndProfileId_otherSortsRemovedFromPageable() {
        // Other sorts are handled inside the spec's query.orderBy(), not in the pageable,
        // because the pageable must be unsorted to prevent Spring Data overwriting ORDER BY.
        Pageable pageable = PageRequest.of(0, 20, Sort.by(
                Sort.Order.asc("ratings"),
                Sort.Order.desc("productionYear")
        ));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertFalse(result.pageable().getSort().isSorted());
    }

    @Test
    void resolveRatingSort_withMixedSortsNoProfileId_preservesOtherSortsInPageable() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(
                Sort.Order.asc("ratings"),
                Sort.Order.desc("productionYear")
        ));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, null);

        assertTrue(result.pageable().getSort().isSorted());
        assertTrue(result.pageable().getSort().stream()
                .anyMatch(o -> o.getProperty().equals("productionYear") && !o.isAscending()));
        assertFalse(result.pageable().getSort().stream()
                .anyMatch(o -> o.getProperty().equalsIgnoreCase("ratings")));
    }

    @Test
    void resolveRatingSort_isCaseInsensitive_singularRating() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("rating"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertFalse(result.pageable().getSort().isSorted());
    }

    @Test
    void resolveRatingSort_isCaseInsensitive_upperCase() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("RATINGS"));

        var result = MediaItemSpecifications.resolveRatingSort(pageable, UUID.randomUUID());

        assertFalse(result.pageable().getSort().isSorted());
    }

    // --- hasCastOrCrewMember ---

    @Test
    void hasCastOrCrewMember_shouldReturnNullPredicateWhenPersonIdIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.hasCastOrCrewMember(null);
        assertNotNull(spec);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void hasCastOrCrewMember_shouldReturnNonNullSpecificationWhenPersonIdProvided() {
        Specification<MediaItem> spec = MediaItemSpecifications.hasCastOrCrewMember(UUID.randomUUID());
        assertNotNull(spec);
    }

    // --- unrated ---

    @Test
    void unrated_shouldReturnNullPredicateWhenUnratedIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.unrated(UUID.randomUUID(), null);
        assertNotNull(spec);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void unrated_shouldReturnNullPredicateWhenUnratedIsFalse() {
        Specification<MediaItem> spec = MediaItemSpecifications.unrated(UUID.randomUUID(), false);
        assertNotNull(spec);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void unrated_shouldReturnNullPredicateWhenProfileIdIsNull() {
        Specification<MediaItem> spec = MediaItemSpecifications.unrated(null, true);
        assertNotNull(spec);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void unrated_shouldReturnNonNullSpecificationWhenUnratedTrueAndProfileIdProvided() {
        Specification<MediaItem> spec = MediaItemSpecifications.unrated(UUID.randomUUID(), true);
        assertNotNull(spec);
    }
}
