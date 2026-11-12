package com.water.water.repository;

import com.water.water.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    // Helps us check if a specific flat already exists in a specific block
    boolean existsByApartmentIdAndBlockAndFlatNumber(Long apartmentId, String block, String flatNumber);
    
    Optional<Household> findByApartmentIdAndBlockAndFlatNumber(Long apartmentId, String block, String flatNumber);

    // Used for cascading soft-delete propagation: find all flats under a specific block in an apartment
    List<Household> findByApartmentIdAndBlock(Long apartmentId, String block);

    List<Household> findByApartmentId(Long apartmentId);

    // Flexible find that handles block with or without "Block " prefix case-insensitively
    @Query("SELECT h FROM Household h WHERE h.apartment.id = :apartmentId " +
           "AND (LOWER(h.block) = LOWER(:block) " +
           "     OR LOWER(REPLACE(h.block, 'Block ', '')) = LOWER(REPLACE(:block, 'Block ', '')) " +
           "     OR LOWER(REPLACE(h.block, ' ', '')) = LOWER(REPLACE(:block, ' ', ''))) " +
           "AND LOWER(h.flatNumber) = LOWER(:flatNumber)")
    Optional<Household> findFlexible(@Param("apartmentId") Long apartmentId, @Param("block") String block, @Param("flatNumber") String flatNumber);

    @Query("SELECT COUNT(h) > 0 FROM Household h WHERE h.apartment.id = :apartmentId " +
           "AND (LOWER(h.block) = LOWER(:block) " +
           "     OR LOWER(REPLACE(h.block, 'Block ', '')) = LOWER(REPLACE(:block, 'Block ', '')) " +
           "     OR LOWER(REPLACE(h.block, ' ', '')) = LOWER(REPLACE(:block, ' ', ''))) " +
           "AND LOWER(h.flatNumber) = LOWER(:flatNumber)")
    boolean existsFlexible(@Param("apartmentId") Long apartmentId, @Param("block") String block, @Param("flatNumber") String flatNumber);

    @Query("SELECT h FROM Household h WHERE h.apartment.id = :apartmentId " +
           "AND (LOWER(h.block) = LOWER(:block) " +
           "     OR LOWER(REPLACE(h.block, 'Block ', '')) = LOWER(REPLACE(:block, 'Block ', '')) " +
           "     OR LOWER(REPLACE(h.block, ' ', '')) = LOWER(REPLACE(:block, ' ', '')))")
    List<Household> findByApartmentIdAndBlockFlexible(@Param("apartmentId") Long apartmentId, @Param("block") String block);
}
