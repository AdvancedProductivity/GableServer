package org.advancedproductivity.gable;

import org.advancedproductivity.gable.framework.config.GableConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GableServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GableServerApplication.class, args);
        GableConfig.initConfig();
    }

}
