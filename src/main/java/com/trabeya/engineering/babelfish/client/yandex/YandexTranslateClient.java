package com.trabeya.engineering.babelfish.client.yandex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@Data
@Slf4j
public class YandexTranslateClient {

    private static final String
    YANDEX_TRANSLATE_API_KEY= "trnsl.1.1.20191101T111039Z.14e58bf59bf71b10.e4947ab1daf2f297b3b684c6949dc7c31aded42b";

    private static final String
            YANDEX_TRANSLATE_GET_LANG_URL= "https://translate.yandex.net/api/v1.5/tr.json/getLangs";

    private HashMap<String,String> supportedTranslationsCodeMap;
    private HashMap<String,String> supportedLanguagesCodeMap;

    public void getSupportedTranslationLanguagesEN() {
        try {
            RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response
                = restTemplate.getForEntity(
                        YANDEX_TRANSLATE_GET_LANG_URL + "?ui=en&key="+YANDEX_TRANSLATE_API_KEY, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode dirs = root.path("dirs");
        JsonNode languages = root.path("langs");
            if (dirs.isArray()) {
                log.info(" Supported Translations :");
                for (JsonNode arrayItem : dirs) {
                    log.info("{}",arrayItem.asText());
                }
                log.info(" List Of Supported Languages :");
                for (JsonNode arrayItem : languages) {
                    log.info("{}",arrayItem.asText());
                }
            }
        } catch (Exception e) {
            log.error("Yandex client getSupportedTranslationLanguages() error :",e);
        }
    }

    public void getSupportedTranslationLanguages(String forLanguageCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response
                    = restTemplate.getForEntity(
                            YANDEX_TRANSLATE_GET_LANG_URL +
                            "?ui="+forLanguageCode+"&key="+YANDEX_TRANSLATE_API_KEY, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dirs = root.path("dirs");
            JsonNode languages = root.path("langs");
            if (dirs.isArray()) {
                log.info(" Supported Translations :");
                for (JsonNode arrayItem : dirs) {
                    log.info("{}",arrayItem.asText());
                }
                log.info(" List Of Supported Languages :");
                for (JsonNode arrayItem : languages) {
                    log.info("{}",arrayItem.asText());
                }
            }
        } catch (Exception e) {
            log.error("Yandex client getSupportedTranslationLanguages() error :",e);
        }
    }

}
