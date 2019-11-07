package com.trabeya.engineering.babelfish.client.web.scrapper;

import com.trabeya.engineering.babelfish.controllers.dtos.SpeechToTextSupportedAudioLanguagesResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GcpSpeechToTextLanguageSupportPage extends WebPageScrapper {

    private static final String PAGE_URL = "https://cloud.google.com/speech-to-text/docs/languages";

    public GcpSpeechToTextLanguageSupportPage() {
        super(PAGE_URL);
    }

    public List<SpeechToTextSupportedAudioLanguagesResponse> getSupportedLanguageList() {
        List<SpeechToTextSupportedAudioLanguagesResponse> languageScraps = new ArrayList<>();
        Element supportedLanguageTable = webPageDocument.select("table").first();
        Elements supportedLanguageTableRows = supportedLanguageTable.select("tbody > tr");
        for (Element row : supportedLanguageTableRows) {
            SpeechToTextSupportedAudioLanguagesResponse language = new SpeechToTextSupportedAudioLanguagesResponse();
            Elements rowCells = row.select("td");
            int column = 0;
            for (Element cell : rowCells) {
                switch (column) {
                    case 0:
                        language.setLanguage(cell.text());
                        break;
                    case 1:
                        language.setLanguageCode(cell.text());
                        break;
                    case 2:
                        language.setLanguageAngloName(cell.text());
                        break;
                    default:
                        break;
                }
                column++;
            }
            languageScraps.add(language);
        }
        return languageScraps;
    }


}
