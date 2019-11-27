package com.trabeya.engineering.babelfish.client.yandex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpringBootTest
@Slf4j
public class YandexTranslateServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private YandexTranslateService translateService;

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testSyncGetYandexTranslationSupport() {
        log.info(translateService.syncGetYandexTranslationSupport("en"));
    }

    @Test
    public void testSyncGetYandexLanguageDetection() {
        log.info(translateService.syncGetYandexLanguageDetection("ලන්දේසීන් "));
    }

    @Test
    public void testSyncGetYandexTranslation() {
        log.info(translateService.syncGetYandexTranslation(
                "In 1583, during the Eighty Years' War of 1568-1648, Habsburg Spain recovered the " +
                        "southern Netherlands from the Protestant rebels. This soon resulted in the use " +
                        "of the occupied ports as bases for privateers, the 'Dunkirkers', " +
                        "to attack the shipping of the Dutch and their allies. To achieve this the " +
                        "Dunkirkers developed small, maneuverable, sailing vessels that came to be " +
                        "referred to as frigates. The success of these Dunkirker vessels influenced " +
                        "the ship design of other navies contending with them, but because most regular " +
                        "navies required ships of greater endurance than the Dunkirker frigates could provide, " +
                        "the term soon came to apply less exclusively to any relatively fast and elegant sail-only " +
                        "warship. In French, the term 'frigate' gave rise to a verb - frégater, meaning 'to build " +
                        "long and low', and to an adjective, adding more confusion. Even the huge English Sovereign " +
                        "of the Seas could be described as 'a delicate frigate' by a contemporary after " +
                        "her upper decks were reduced in 1651.","en-si"));
    }

    @Test
    public void testAsyncGetYandexTranslationSupport() {
        try {
            translateService.asyncGetYandexTranslationSupport("en").subscribe(log::info);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("testAsyncGetYandexTranslationSupport Error : ", e);
        }
    }
}