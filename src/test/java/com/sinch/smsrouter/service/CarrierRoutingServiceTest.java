package com.sinch.smsrouter.service;

import com.sinch.smsrouter.model.Carrier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarrierRoutingServiceTest {

    private CarrierRoutingService carrierRoutingService;

    @BeforeEach
    void setUp() {
        carrierRoutingService = new CarrierRoutingService();
    }

    @Test
    void shouldRouteFirstAuMessageToTelstra() {
        assertThat(carrierRoutingService.selectCarrier("+61491570156")).isEqualTo(Carrier.TELSTRA);
    }

    @Test
    void shouldRouteSecondAuMessageToOptus() {
        carrierRoutingService.selectCarrier("+61491570156");
        assertThat(carrierRoutingService.selectCarrier("+61491570156")).isEqualTo(Carrier.OPTUS);
    }

    @Test
    void shouldRouteThirdAuMessageBackToTelstra() {
        carrierRoutingService.selectCarrier("+61491570156");
        carrierRoutingService.selectCarrier("+61491570156");
        assertThat(carrierRoutingService.selectCarrier("+61491570156")).isEqualTo(Carrier.TELSTRA);
    }

    @Test
    void shouldRouteNzNumberToSpark() {
        assertThat(carrierRoutingService.selectCarrier("+64211234567")).isEqualTo(Carrier.SPARK);
    }

    @Test
    void shouldRouteUsNumberToGlobal() {
        assertThat(carrierRoutingService.selectCarrier("+12025550123")).isEqualTo(Carrier.GLOBAL);
    }

    @Test
    void shouldRouteUnknownPrefixToGlobal() {
        assertThat(carrierRoutingService.selectCarrier("+441234567890")).isEqualTo(Carrier.GLOBAL);
    }

    @Test
    void shouldNotIncrementAuCounterForNzNumber() {
        carrierRoutingService.selectCarrier("+61491570156"); // Telstra
        carrierRoutingService.selectCarrier("+64211234567"); // Spark - should not affect AU counter
        assertThat(carrierRoutingService.selectCarrier("+61491570156")).isEqualTo(Carrier.OPTUS);
    }
}
