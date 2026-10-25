package com.transitassist.transitassist.config;

import com.transitassist.transitassist.entity.Route;
import com.transitassist.transitassist.entity.RouteStop;
import com.transitassist.transitassist.entity.Stop;
import com.transitassist.transitassist.repository.RouteRepository;
import com.transitassist.transitassist.repository.RouteStopRepository;
import com.transitassist.transitassist.repository.StopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inserts sample data ONLY when the tables are empty, so restarting never duplicates or overwrites anything.
 * Stop coordinates are approximate and the routes are fictional: this is demo data, not a real timetable.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;

    public DataSeeder(RouteRepository routeRepository, StopRepository stopRepository,
                      RouteStopRepository routeStopRepository) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (routeRepository.count() > 0 || stopRepository.count() > 0) {
            log.info("Tables already contain data; skipping sample data");
            return;
        }
        Stop junction = new Stop("Bhopal Junction", "Railway Station Area", 23.2670, 77.4120);
        Stop roshanpura = new Stop("Roshanpura Square", "Roshanpura", 23.2385, 77.4085);
        Stop newMarket = new Stop("New Market", "TT Nagar", 23.2340, 77.4010);
        Stop boardOffice = new Stop("Board Office Square", "Arera Hills", 23.2290, 77.4200);
        Stop mpNagar = new Stop("MP Nagar Zone-1", "MP Nagar", 23.2335, 77.4340);
        Stop rkStation = new Stop("Rani Kamlapati Station", "Habibganj", 23.2300, 77.4395);
        Stop kolar = new Stop("Kolar Road Chauraha", "Kolar", 23.1960, 77.4290);
        stopRepository.saveAll(List.of(junction, roshanpura, newMarket, boardOffice, mpNagar, rkStation, kolar));

        Route r101 = routeRepository.save(new Route("101", "City Centre Line", "Bhopal Junction", "MP Nagar Zone-1"));
        Route r202 = routeRepository.save(new Route("202", "Habibganj-Kolar Link", "Rani Kamlapati Station", "Kolar Road Chauraha"));

        routeStopRepository.saveAll(List.of(
                new RouteStop(r101, junction, 1), new RouteStop(r101, roshanpura, 2),
                new RouteStop(r101, newMarket, 3), new RouteStop(r101, boardOffice, 4),
                new RouteStop(r101, mpNagar, 5),
                // Board Office Square is shared: one stop on two routes (many-to-many)
                new RouteStop(r202, rkStation, 1), new RouteStop(r202, boardOffice, 2),
                new RouteStop(r202, kolar, 3)));
        log.info("Seeded sample routes and stops");
    }
}
