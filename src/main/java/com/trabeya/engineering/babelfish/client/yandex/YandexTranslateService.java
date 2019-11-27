package com.trabeya.engineering.babelfish.client.yandex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class YandexTranslateService {

    private WebClient webClient;

    private static final String
            YANDEX_TRANSLATE_API_KEY
            = "trnsl.1.1.20191101T111039Z.14e58bf59bf71b10.e4947ab1daf2f297b3b684c6949dc7c31aded42b";

    private static final String
            YANDEX_TRANSLATE_GET_LANG_URL= "https://translate.yandex.net/api/v1.5/tr.json";

    private String yandexTranslateApiKey;

    public YandexTranslateService() {
        this.webClient = WebClient.builder()
                .baseUrl(YANDEX_TRANSLATE_GET_LANG_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.yandexTranslateApiKey = YANDEX_TRANSLATE_API_KEY;
    }

    public YandexTranslateService(String url, String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.yandexTranslateApiKey = apiKey;
    }

    public Mono<String> asyncGetYandexTranslationSupport(String languageCode) {
        return this.webClient.get().uri(
                "/getLangs?ui={languageCode}&key={yandexTranslateApiKey}", languageCode, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> asyncGetYandexLanguageDetection(String text) {
        return this.webClient.get().uri(
                "/detect?text={text}&key={yandexTranslateApiKey}", text, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> asyncGetYandexTranslationSupport(String text, String translationCode) {
        return this.webClient.get().uri(
                "/translate?text={text}&lang={translationCode}&key={yandexTranslateApiKey}",
                text, translationCode, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class);
    }

    public String syncGetYandexTranslationSupport(String languageCode) {
        return this.webClient.get().uri(
                "/getLangs?ui={languageCode}&key={yandexTranslateApiKey}", languageCode, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class).block();
    }

    public String syncGetYandexLanguageDetection(String text) {
        return this.webClient.get().uri(
                "/detect?text={text}&key={yandexTranslateApiKey}", text, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class).block();
    }

    public String syncGetYandexTranslation(String text, String translationCode) {
        return this.webClient.get().uri(
                "/translate?text={text}&lang={translationCode}&key={yandexTranslateApiKey}",
                text, translationCode, yandexTranslateApiKey)
                .retrieve().bodyToMono(String.class).block();
    }
}
