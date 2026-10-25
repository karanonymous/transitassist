package com.transitassist.transitassist.repository;

import com.transitassist.transitassist.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
