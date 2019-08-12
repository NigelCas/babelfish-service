package com.trabeya.engineering.babelfish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String API_NAME = "Babelfish API";
    public static final String API_VERSION = "0.0.01";
    public static final String BASE_CONTROLLER_PACKAGE = "com.trabeya.engineering.babelfish";

    // Google API
    // Translation v3 API
    public static final String GOOGLE_API_PROJECT_ID = "babelfish-248311";
    public static final String GOOGLE_API_LOCATION = "global";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
