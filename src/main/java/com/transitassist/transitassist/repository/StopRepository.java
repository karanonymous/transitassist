package com.transitassist.transitassist.repository;

import com.transitassist.transitassist.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<Stop, Long> {
}
