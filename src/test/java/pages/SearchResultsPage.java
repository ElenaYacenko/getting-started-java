package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class SearchResultsPage {

    private final SelenideElement resultsList = $("[data-testid='results-list']");

    @Step("Проверяем результат поиска: {expectedText}")
    public SearchResultsPage shouldHaveResult(String expectedText) {
        resultsList.shouldHave(text(expectedText));
        return this;
    }

    @Step("Проверяем отсутствие результатов поиска")
    public SearchResultsPage shouldBeEmpty() {
        $x("//span[contains(text(), '0') and contains(text(), 'results')]").shouldHave(text("0 results"));
        return this;
    }
}
