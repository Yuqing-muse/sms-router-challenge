package com.sinch.smsrouter;

import com.jayway.jsonpath.JsonPath;
import com.sinch.smsrouter.model.Carrier;
import com.sinch.smsrouter.model.MessageStatus;
import com.sinch.smsrouter.repository.MessageRepository;
import com.sinch.smsrouter.repository.OptOutRepository;
import com.sinch.smsrouter.service.CarrierRoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmsRouterIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MessageRepository messageRepository;
    @Autowired private OptOutRepository optOutRepository;
    @Autowired private CarrierRoutingService carrierRoutingService;

    @BeforeEach
    void resetState() {
        messageRepository.clear();
        optOutRepository.clear();
        carrierRoutingService.reset();
    }

    @Test
    void shouldSuccessWithDeliveredStatusForValidAuNumber() throws Exception {
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequest("+61491570156", "Hello world", "SMS")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value(MessageStatus.DELIVERED.name()));
    }

    @Test
    void shouldRouteAuNumberToTelstra() throws Exception {
        String id = createMessage("+61491570156", "Hello");

        fetchMessage(id)
                .andExpect(jsonPath("$.destination_number").value("+61491570156"))
                .andExpect(jsonPath("$.status").value(MessageStatus.DELIVERED.name()))
                .andExpect(jsonPath("$.carrier").value(Carrier.TELSTRA.getDisplayName()));
    }

    @Test
    void shouldRouteNzNumberToSpark() throws Exception {
        String id = createMessage("+64211234567", "Kia ora");

        fetchMessage(id)
                .andExpect(jsonPath("$.destination_number").value("+64211234567"))
                .andExpect(jsonPath("$.status").value(MessageStatus.DELIVERED.name()))
                .andExpect(jsonPath("$.carrier").value(Carrier.SPARK.getDisplayName()));
    }

    @Test
    void shouldBlockMessageToOptedOutNumber() throws Exception {
        mockMvc.perform(post("/optout/{phoneNumber}", "+61491570156"))
                .andExpect(status().isOk());

        String id = createMessage("+61491570156", "Hello");

        fetchMessage(id)
                .andExpect(jsonPath("$.status").value(MessageStatus.BLOCKED.name()))
                .andExpect(jsonPath("$.carrier").doesNotExist());
    }

    @Nested
    class ErrorCases {

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    Arguments.of("invalid phone number",  buildRequest("invalid-number", "Hello", "SMS")),
                    Arguments.of("unsupported format",    buildRequest("+61491570156", "Hello", "MMS")),
                    Arguments.of("missing required field", """
                            {"destination_number": "+61491570156", "format": "SMS"}
                            """)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequests")
        void shouldReturnErrorWithInvalidRequest(String ignored, String body) throws Exception {
            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").isNotEmpty());
        }

        @Test
        void shouldReturnErrorWithUnknownMessageId() throws Exception {
            mockMvc.perform(get("/messages/{id}", "00000000-0000-0000-0000-000000000000"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").isNotEmpty());
        }

        @Test
        void shouldReturnErrorWhenOptOutInvalidPhoneNumber() throws Exception {
            mockMvc.perform(post("/optout/{phoneNumber}", "invalid-number"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").isNotEmpty());
        }
    }

    private String createMessage(String number, String content) throws Exception {
        String response = mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequest(number, content, "SMS")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private ResultActions fetchMessage(String id) throws Exception {
        return mockMvc.perform(get("/messages/{id}", id))
                .andExpect(status().isOk());
    }

    private static String buildRequest(String number, String content, String format) {
        return """
                {
                  "destination_number": "%s",
                  "content": "%s",
                  "format": "%s"
                }
                """.formatted(number, content, format);
    }
}
