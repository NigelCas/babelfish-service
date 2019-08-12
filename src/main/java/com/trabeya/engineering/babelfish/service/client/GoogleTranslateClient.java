package com.trabeya.engineering.babelfish.service.client;

import com.google.cloud.translate.*;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.trabeya.engineering.babelfish.service.exceptions.GoogleTranslationAPIException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class GoogleTranslateClient {

    private List<Language> listSupportedLanguagesV2;

    GoogleTranslateClient() {
        listSupportedLanguagesV2 = listSupportedLanguagesV2();
    }

    /**
     * Translates a given text to a target language using v2 API.
     *
     * @param text - Text for translation.
     * @param sourceLanguageCode - Language code of text. e.g. "en"
     * @param targetLanguageCode - Language code for translation. e.g. "sr"
     */
    public String translateTextV2(
            String text,
            String format,
            String sourceLanguageCode,
            String targetLanguageCode) {

        // Instantiates a client
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        // Translates some text into Russian
        Translation translation =
                translate.translate(
                        text,
                        Translate.TranslateOption.format(format),
                        Translate.TranslateOption.sourceLanguage(sourceLanguageCode),
                        Translate.TranslateOption.targetLanguage(targetLanguageCode));

        return translation.getTranslatedText();
    }

    /**
     * Translates a given text to a target language using v2 API.
     *
     * @param text - Text for detection.
     */
    public Detection detectTextLangV2(String text) {
        Detection detection = null;
        try {
            // Instantiates a client
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            List<String> texts = Collections.singletonList(text);
            // Detects some text
            List<Detection> detections = translate.detect(texts);
            detection = detections.get(0);
            log.info("language detected :"+detection.getLanguage());
            log.info("language detection confidence factor : "+detection.getConfidence()*100);
        }
        catch (Exception ex ) {
            log.error("detectTextLangV2 service exception : ", ex);
            throw new GoogleTranslationAPIException(ex.getMessage());
        }
        return detection;
    }


    /**
     * Fetches list of languages supported by using v2 API.
     */
    public List<Language> listSupportedLanguagesV2() {
        List<Language> supportedList;
        try {
        // Instantiates a client
        Translate translate = TranslateOptions.getDefaultInstance().getService();
            supportedList =
                translate.listSupportedLanguages();
        log.info("Supported no. of languages :"+supportedList.size());
        }
        catch (Exception ex ) {
            log.error("listSupportedLanguagesV2 service exception : ", ex);
            throw new GoogleTranslationAPIException(ex.getMessage());
        }
        return supportedList;
    }

    /**
     * Fetches list of languages supported by using v2 API.
     * @param targetLanguageCode target language code to translate against
     */
    public List<Language> listSupportedLanguagesV2(
            String targetLanguageCode) {
        List<Language> supportedList;
        try {
            // Instantiates a client
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            supportedList =
                    translate.listSupportedLanguages(Translate.LanguageListOption.targetLanguage(targetLanguageCode));
            log.info("Supported no. of languages :" + supportedList.size());
        }
        catch (Exception ex ) {
            log.error("listSupportedLanguagesV2 service exception : ", ex);
            throw new GoogleTranslationAPIException(ex.getMessage());
        }
        return supportedList;
    }


    /**
     * Translates a given text to a target language using v3 API.
     *
     * @param projectId - Id of the project.
     * @param location - location name.
     * @param text - Text for translation.
     * @param sourceLanguageCode - Language code of text. e.g. "en"
     * @param targetLanguageCode - Language code for translation. e.g. "sr"
     */
    public String translateTextV3(
            String projectId,
            String location,
            String text,
            String sourceLanguageCode,
            String targetLanguageCode) {
        try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {

            LocationName locationName =
                    LocationName.newBuilder().setProject(projectId).setLocation(location).build();

            TranslateTextRequest translateTextRequest =
                    TranslateTextRequest.newBuilder()
                            .setParent(locationName.toString())
                            .setMimeType("text/plain")
                            .setSourceLanguageCode(sourceLanguageCode)
                            .setTargetLanguageCode(targetLanguageCode)
                            .addContents(text)
                            .build();

            // Call the API
            TranslateTextResponse response = translationServiceClient.translateText(translateTextRequest);
            return response.getTranslationsList().get(0).getTranslatedText();

        } catch (Exception e) {
            throw new GoogleTranslationAPIException(e.getMessage());
        }
    }


}
