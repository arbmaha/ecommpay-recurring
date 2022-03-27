package com.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The {@link RecurringPaymentsApplication} is a
 * Spring Boot application
 * that manages a bounded context for
 * RecurringPayments
 *
 * @author Olga Savin
 */
@SpringBootApplication
public class RecurringPaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecurringPaymentsApplication.class, args);
    }

}
