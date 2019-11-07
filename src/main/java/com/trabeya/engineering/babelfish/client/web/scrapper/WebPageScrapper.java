package com.trabeya.engineering.babelfish.client.web.scrapper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
class WebPageScrapper {

    Document webPageDocument;

    WebPageScrapper(String webPageUrl) {
        try {
            webPageDocument = Jsoup.connect(webPageUrl).get();
        } catch (IOException e) {
            log.error("Jsoup Error connecting to :{}", webPageUrl, e );
        }
    }
}
