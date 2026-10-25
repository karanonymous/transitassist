package com.transitassist.transitassist.repository;

import com.transitassist.transitassist.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    /** One query for everything; "join fetch" loads route and stop together (avoids N+1 queries). */
    @Query("select rs from RouteStop rs join fetch rs.route join fetch rs.stop order by rs.route.id, rs.stopOrder")
    List<RouteStop> findAllOrdered();
}
