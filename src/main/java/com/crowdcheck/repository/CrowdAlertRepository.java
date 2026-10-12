package com.crowdcheck.repository;

import com.crowdcheck.entity.CrowdAlert;
import com.crowdcheck.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrowdAlertRepository extends JpaRepository<CrowdAlert, Integer> {

    List<CrowdAlert> findByStatusOrderByAlertTimeDesc(String status);

    List<CrowdAlert> findAllByOrderByAlertTimeDesc();

    Optional<CrowdAlert> findFirstByLocationAndStatus(Location location, String status);

    List<CrowdAlert> findByLocationOrderByAlertTimeDesc(Location location);

    long countByLocation(Location location);
}
