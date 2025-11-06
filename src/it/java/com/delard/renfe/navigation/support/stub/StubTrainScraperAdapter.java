package com.delard.renfe.navigation.support.stub;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stub implementation of the {@link TrainScraperPort} port used in tests.
 * Returns a fixed set of outbound trains and an empty list for return trains,
 * allowing tests to be isolated from real integration with Playwright or external services.
 */
@Alternative
@ApplicationScoped
public class StubTrainScraperAdapter implements TrainScraperPort {

    @Override
    public List<List<Train>> scrapeTrains(String origin, String destination, String dateOut, String dateReturn, int adults) {
        Train t1 = new Train("T123", "AVE", "08:00", "10:30", "2h30m", 29.99);
        Train t2 = new Train("T456", "ALVIA", "09:00", "12:30", "3h30m", 19.99);
        List<Train> outBoundTrains = new ArrayList<>(Arrays.asList(t1, t2));
        List<Train> returnTrains = new ArrayList<>();
        return Arrays.asList(outBoundTrains, returnTrains);
    }
}


