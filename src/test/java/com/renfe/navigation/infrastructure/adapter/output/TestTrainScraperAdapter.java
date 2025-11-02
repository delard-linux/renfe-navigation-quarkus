package com.renfe.navigation.infrastructure.adapter.output;

import com.renfe.navigation.domain.model.Train;
import com.renfe.navigation.domain.port.output.TrainScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Alternative
@Priority(1)
@ApplicationScoped
public class TestTrainScraperAdapter implements TrainScraperPort {

    @Override
    public List<List<Train>> scrapeTrains(String origin, String destination, String dateOut, String dateReturn, int adults) {
        Train t1 = new Train("T123", "AVE", "08:00", "10:30", "2h30m", 29.99);
        Train t2 = new Train("T456", "ALVIA", "09:00", "12:30", "3h30m", 19.99);
                List<Train> out = List.of(t1, t2);t(t1, t2));
        List<Train> returnTrains = new ArrayList<>();
        return Arrays.asList(outBoundTrains, returnTrains);
    }
}
