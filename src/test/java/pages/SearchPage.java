package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class SearchPage {

    private final SelenideElement searchInput = $("[aria-label='Search GitHub']");

    @Step("Переходим на страницу поиска")
    public SearchPage open() {
        Selenide.open("/search");
        return this;
    }

    @Step("Ищем: {query}")
    public SearchPage searchFor(String query) {
        searchInput.setValue(query).pressEnter();
        return this;
    }
}
