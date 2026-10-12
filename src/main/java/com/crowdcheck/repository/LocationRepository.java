package com.crowdcheck.repository;

import com.crowdcheck.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    Optional<Location> findByLocationName(String locationName);

    boolean existsByLocationName(String locationName);

    List<Location> findByLocationType(String locationType);

    List<Location> findByCrowdLevel(String crowdLevel);

    @Query("SELECT l FROM Location l WHERE " +
           "(:name IS NULL OR LOWER(l.locationName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:type IS NULL OR l.locationType = :type) AND " +
           "(:level IS NULL OR l.crowdLevel = :level) " +
           "ORDER BY l.locationName ASC")
    List<Location> searchLocations(@Param("name") String name,
                                   @Param("type") String type,
                                   @Param("level") String level);
}
