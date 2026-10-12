package com.crowdcheck.repository;

import com.crowdcheck.entity.CrowdUpdate;
import com.crowdcheck.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrowdUpdateRepository extends JpaRepository<CrowdUpdate, Integer> {

    List<CrowdUpdate> findByLocationOrderByUpdateTimeDesc(Location location);

    List<CrowdUpdate> findAllByOrderByUpdateTimeDesc();

    long countByLocation(Location location);

    @Query("SELECT u.location.locationId, u.location.locationName, u.location.locationType, " +
           "u.location.capacity, COUNT(u) " +
           "FROM CrowdUpdate u " +
           "WHERE u.crowdLevel IN ('HIGH', 'CRITICAL') " +
           "GROUP BY u.location.locationId, u.location.locationName, u.location.locationType, u.location.capacity " +
           "ORDER BY COUNT(u) DESC")
    List<Object[]> findFrequentOvercrowdedLocations();
}
