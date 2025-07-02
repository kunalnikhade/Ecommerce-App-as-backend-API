package com.ecommerce.ecommapis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication
{
    private static final Logger logger = LogManager.getLogger(EcommerceApplication.class);

    public static void main(String[] args)
    {
        logger.info("Starting Ecommerce Application");

        SpringApplication.run(EcommerceApplication.class, args);
    }
}
