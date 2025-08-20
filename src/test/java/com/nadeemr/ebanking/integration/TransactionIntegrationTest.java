package com.nadeemr.ebanking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadeemr.ebanking.EBankingTransactionsApplication;
import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.infrastructure.messaging.TransactionEvent;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = EBankingTransactionsApplication.class)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Disabled("Integration tests require Docker environment")
class TransactionIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("ebanking_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.kafka.transaction-topic}")
    private String transactionTopic;
    
    private MockMvc mockMvc;
    private SecretKey secretKey;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @Test
    void fullWorkflow_KafkaToAPI_ShouldWork() throws Exception {
        String customerId = "P-0123456789";
        
        // 1. Send transaction event to Kafka
        TransactionEvent event = new TransactionEvent(
            "test-transaction-id",
            new BigDecimal("150.75"),
            "EUR",
            "DE89370400440532013000",
            LocalDate.now(),
            "Integration test transaction",
            customerId,
            "CREATE"
        );
        
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(transactionTopic, event.getTransactionId(), eventJson);
        
        // Wait for Kafka message processing
        Thread.sleep(2000);
        
        // 2. Generate JWT token
        String token = generateJwtToken(customerId);
        
        // 3. Call API to get transactions
        String yearMonth = LocalDate.now().getYear() + "-" + 
                          String.format("%02d", LocalDate.now().getMonthValue());
        
        mockMvc.perform(get("/api/v1/transactions")
                .header("Authorization", "Bearer " + token)
                .param("yearMonth", yearMonth)
                .param("page", "0")
                .param("size", "10")
                .param("baseCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.summary.baseCurrency").value("EUR"));
    }
    
    @Test
    void getTransactions_WithValidToken_ShouldReturnTransactions() throws Exception {
        String customerId = "P-0123456789";
        String token = generateJwtToken(customerId);
        
        mockMvc.perform(get("/api/v1/transactions")
                .header("Authorization", "Bearer " + token)
                .param("yearMonth", "2023-10")
                .param("page", "0")
                .param("size", "10")
                .param("baseCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.pageInfo").exists())
                .andExpect(jsonPath("$.summary").exists());
    }
    
    @Test
    void getTransactions_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid.jwt.token";
        
        mockMvc.perform(get("/api/v1/transactions")
                .header("Authorization", "Bearer " + invalidToken)
                .param("yearMonth", "2023-10"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void getTransactions_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                .param("yearMonth", "2023-10"))
                .andExpect(status().isUnauthorized());
    }
    
    private String generateJwtToken(String customerId) {
        return Jwts.builder()
                .setSubject(customerId)
                .claim("role", "CUSTOMER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(secretKey)
                .compact();
    }
}
