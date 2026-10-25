package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.NearbyStopResponse;
import com.transitassist.transitassist.dto.StopResponse;
import com.transitassist.transitassist.entity.Stop;
import com.transitassist.transitassist.exception.BadRequestException;
import com.transitassist.transitassist.repository.StopRepository;
import com.transitassist.transitassist.util.GeoUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StopService {

    static final int MAX_RADIUS_METERS = 5000;
    static final int MAX_RESULTS = 20;

    private final StopRepository stopRepository;

    public StopService(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public List<StopResponse> listAll() {
        return stopRepository.findAll(Sort.by("name")).stream().map(StopResponse::from).toList();
    }

    /**
     * Nearest stops within a radius. Distance is computed in Java over all stops, which is fine for a small
     * prototype; a large system would filter in SQL with a bounding box and an index.
     */
    public List<NearbyStopResponse> nearby(double lat, double lng, int radiusMeters) {
        if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
            throw new BadRequestException("lat must be between -90 and 90");
        }
        if (!Double.isFinite(lng) || lng < -180 || lng > 180) {
            throw new BadRequestException("lng must be between -180 and 180");
        }
        if (radiusMeters < 1 || radiusMeters > MAX_RADIUS_METERS) {
            throw new BadRequestException("radius must be between 1 and " + MAX_RADIUS_METERS + " metres");
        }
        return stopRepository.findAll().stream()
                .map(s -> toNearby(s, GeoUtils.haversineMeters(lat, lng, s.getLatitude(), s.getLongitude())))
                .filter(n -> n.distanceMeters() <= radiusMeters)
                .sorted(Comparator.comparingInt(NearbyStopResponse::distanceMeters))
                .limit(MAX_RESULTS)
                .toList();
    }

    private NearbyStopResponse toNearby(Stop s, double meters) {
        return new NearbyStopResponse(s.getId(), s.getName(), s.getLocality(), s.getLatitude(), s.getLongitude(),
                (int) Math.round(meters));
    }
}
