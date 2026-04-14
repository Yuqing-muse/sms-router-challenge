package com.sinch.smsrouter.service;

import com.sinch.smsrouter.model.Carrier;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CarrierRoutingService {

    private final AtomicInteger auCounter = new AtomicInteger(0);

    /**
     * Selects a carrier based on the destination number prefix.
     * AU (+61): alternates between Telstra and Optus (round-robin).
     * NZ (+64): Spark.
     * Other: Global.
     */
    public void reset() {
        auCounter.set(0);
    }

    public Carrier selectCarrier(String destinationNumber) {
        if (destinationNumber.startsWith("+61")) {
            return auCounter.getAndIncrement() % 2 == 0 ? Carrier.TELSTRA : Carrier.OPTUS;
        }
        if (destinationNumber.startsWith("+64")) {
            return Carrier.SPARK;
        }
        return Carrier.GLOBAL;
    }
}
