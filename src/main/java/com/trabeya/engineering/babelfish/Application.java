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

    // Google Cloud Storage
    // cloud storage URIs
    public static final String GOOGLE_CLOUD_STORAGE_BUCKET_NAME = "babelfish_service_bucket";

    //text-to-speech synthesis
    public static final String GOOGLE_TEXT_TO_SPEECH_INPUT_URI = "synthesis/input/";
    public static final String GOOGLE_TEXT_TO_SPEECH_OUTPUT_URI = "synthesis/output/";

    //speech-to-text transcription
    public static final String GOOGLE_SPEECH_TO_TEXT_INPUT_URI = "transcribe/input/";
    public static final String GOOGLE_SPEECH_TO_TEXT_OUTPUT_URI = "transcribe/output/";

    //translation
    public static final String GOOGLE_TRANSLATION_INPUT_URI = "translate/input/";
    public static final String GOOGLE_TRANSLATION_OUTPUT_URI = "translate/output/";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
