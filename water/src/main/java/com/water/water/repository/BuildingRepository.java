package com.water.water.repository;

import com.water.water.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByColonyIdAndDeletedFalse(Long colonyId);
    boolean existsByNameIgnoreCaseAndColonyIdAndDeletedFalse(String name, Long colonyId);
    Optional<Building> findByNameIgnoreCaseAndColonyIdAndDeletedFalse(String name, Long colonyId);

    @Query("SELECT b FROM Building b WHERE b.colony.id = :colonyId AND b.deleted = false " +
           "AND (LOWER(b.name) = LOWER(:name) " +
           "     OR LOWER(REPLACE(b.name, 'Block ', '')) = LOWER(REPLACE(:name, 'Block ', '')) " +
           "     OR LOWER(REPLACE(b.name, ' ', '')) = LOWER(REPLACE(:name, ' ', '')))")
    Optional<Building> findByNameFlexible(@Param("name") String name, @Param("colonyId") Long colonyId);
}
