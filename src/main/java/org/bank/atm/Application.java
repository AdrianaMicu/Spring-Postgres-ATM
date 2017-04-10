package org.bank.atm;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    private static final Logger log = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        log.info("Starting application...");
        SpringApplication.run(Application.class);
    }
}
