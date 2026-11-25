package tests;

import org.junit.jupiter.api.Test;

public class SearchTests extends TestBase {

    @Test
    void successfulSearchTest() {
        searchPage.open()
                .searchFor("qa.guru");
        searchResultsPage.shouldHaveResult("QA.GURU");
    }

    @Test
    void noResultsForImpossibleStarsRangeTest() {
        searchPage.open()
                .searchFor("stars:<0");
        searchResultsPage.shouldBeEmpty();
    }

    @Test
    void noResultsForHugeStarsCountTest() {
        searchPage.open()
                .searchFor("stars:>999999999");
        searchResultsPage.shouldBeEmpty();
    }
}
