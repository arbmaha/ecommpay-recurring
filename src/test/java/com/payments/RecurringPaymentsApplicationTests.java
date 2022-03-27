package com.payments;

import com.payments.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Map;
import java.util.UUID;

/**
 * The {@link RecurringPaymentsApplication} is a
 * Spring Boot application
 * that manages a bounded context for
 * RecurringPayments
 *
 * @author Olga Savin
 */
@SpringBootTest
class RecurringPaymentsApplicationTests {

    @Autowired
    private PaymentService paymentService;

    @Test
    public void testPaymentPage() {
        int amount = 100;
        String currency = "USD";
        String customerId = "90";
        String formUrl = paymentService.getFormUrl(customerId, amount, currency);
        System.out.println(formUrl);
    }

    @Test
    public void testRecurringPayment() {
        int amount = 100;
        String currency = "USD";
        String customerId = "90";

        int recurringId = 1640233721;
        int projectId = 47331;
        String ip = "84.24.78.78";

        String payment_id = UUID.randomUUID().toString();
        Map<String, Object> map = paymentService.createPaymentMap(amount,currency,ip,customerId,projectId,recurringId,payment_id);
        paymentService.makePayment(map);
    }
}
