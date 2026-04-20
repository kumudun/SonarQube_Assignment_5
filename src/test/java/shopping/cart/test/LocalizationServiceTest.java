package shopping.cart.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import shopping.cart.LocalizationService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceTest {

    private static final String SELECT_LANGUAGE = "select.language";

    private final LocalizationService localizationService = new LocalizationService();

    @ParameterizedTest
    @CsvSource({
            "en, Select the language:",
            "fi, Valitse kieli:",
            "sv, Välj språk:",
            "ja, 言語を選択してください:",
            "ar, اختر اللغة:"
    })
    void testLocalizationContainsExpectedEnglishKey(String languageCode, String expectedValue) {
        Map<String, String> result = localizationService.getLocalizedStrings(languageCode);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey(SELECT_LANGUAGE));
        assertTrue(result.containsKey("calculate.total"));
        assertTrue(result.containsKey("total.cost"));
        assertEquals(expectedValue, result.get(SELECT_LANGUAGE));
    }

    @Test
    void testUnknownLanguageFallsBackToEnglish() {
        Map<String, String> result = localizationService.getLocalizedStrings("xx");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Select the language:", result.get(SELECT_LANGUAGE));
    }

    @Test
    void testEnglishContainsErrorMessages() {
        Map<String, String> result = localizationService.getLocalizedStrings("en");

        assertEquals("Error", result.get("error.title"));
        assertEquals("Please enter a valid number of items.", result.get("error.invalid.items"));
        assertEquals("Please click Enter Items first.", result.get("error.enter.items.first"));
    }
}