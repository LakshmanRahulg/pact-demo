package com.test.pact;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.consumer.services.StudentConsumerService;
import com.test.consumer.services.connector.ProviderConnector;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MyTestConfig.class)
public class ConsumerTest {

    @Autowired
    StudentConsumerService studentConsumerService;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("studentProvider", "localhost", 8066, this);

    @Pact(consumer = "myconsumer") // will default to the provider name from mockProvider in Rule
    public RequestResponsePact defineExpectation(PactDslWithProvider builder) {
        return builder
                .uponReceiving("get Student data")
                .path("/myconsumer/student/A123")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\n" +
                        "\t\"rollId\": \"A123\",\n" +
                        "\t\"fullName\": \"Tom Hanks\",\n" +
                        "\t\"age\": 12\n" +
                        "}")
                .toPact();
    }

    @Test
    @PactVerification
    public void runTest() {

        Assert.assertTrue(studentConsumerService.getStudent("A123").isPresent());

    }
}

@Configuration
class MyTestConfig {

    @Bean
    public StudentConsumerService getStudentConsumerService(ProviderConnector providerConnector) {
        return new StudentConsumerService(providerConnector);
    }

    @Bean
    public ProviderConnector getProviderConnector(ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
        return new ProviderConnector("http://localhost:8066/myconsumer", restTemplateBuilder, objectMapper);
    }

    @Bean
    public ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public RestTemplateBuilder getRestTemplateBuilder(){
        return new RestTemplateBuilder();
    }

}
