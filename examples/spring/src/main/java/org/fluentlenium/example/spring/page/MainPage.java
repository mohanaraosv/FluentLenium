package org.fluentlenium.example.spring.page;

import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.annotation.PageUrl;
import org.fluentlenium.core.domain.FluentWebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;

@PageUrl("/")
public class MainPage extends FluentPage {
    @FindBy(css = ".sbibod ")
    private FluentWebElement searchInputBorder;
    @FindBy(css = ".gsfi ")
    private FluentWebElement searchInput;
    @FindBy(css = "input[name=\"btnK\"]")
    private FluentWebElement searchButton;
    @Page
    private ResultsPage resultsPage;

    public MainPage typeTextIn() {
        searchInputBorder.click();
        searchInput.write("Something");

        keyboard().basic().sendKeys(Keys.TAB);

        return this;
    }

    public ResultsPage startSearch() {
        await().until(searchButton).clickable();

        searchButton.click();
        return resultsPage;
    }
}
