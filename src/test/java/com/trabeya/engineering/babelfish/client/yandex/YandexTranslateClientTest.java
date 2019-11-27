package com.trabeya.engineering.babelfish.client.yandex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
@SpringBootTest
@Slf4j
public class YandexTranslateClientTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private YandexTranslateClient yandexTranslateClient;

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    //
    @Test
    public void testGetSupportedTranslationLanguagesFR() {
        yandexTranslateClient.getSupportedTranslationLanguages("fr");
    }

    @Test
    public void testGetSupportedTranslationLanguagesEN() {
        yandexTranslateClient.getSupportedTranslationLanguagesEN();
    }
}