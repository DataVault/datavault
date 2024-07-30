package org.datavaultplatform.webapp.selenium;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Note : this test takes the username password from the environmental variables (DV_USERNAME, DV_PASSWORD)
 * This is to help prevent the storing of user credentials in Java code and Git and GitHub.
 */
@Slf4j
public abstract class BaseSeleniumTest {

    public static final String ENV_DV_USERNAME = "DV_USERNAME";
    public static final String ENV_DV_PASSWORD = "DV_PASSWORD";

    public static final String DV_DEMO_URL = "https://demo.datavault.ed.ac.uk";
    public static final String DV_EASE_URL = "https://www.ease.ed.ac.uk";
    protected WebDriver driver;
    private String username;
    private String password;

    @BeforeEach
    protected void setup() {
        username = System.getenv(ENV_DV_USERNAME);
        password = System.getenv(ENV_DV_PASSWORD);

        assertThat(username).isNotBlank();
        assertThat(password).isNotBlank();
        assertThat(username).isNotEqualTo(password);

        checkURL("https://httpbin.org/status/200", 200);
        checkURL("https://httpbin.org/status/302", 302);
        checkURL(DV_DEMO_URL, 302);
        checkURL(DV_EASE_URL, 200);
        login();
    }

    protected void checkHeader() {

        int navLinkCount = driver.findElements(By.xpath("//nav//a")).size();
        assertThat(navLinkCount).isEqualTo(6);
        WebElement navLinkDataVault = getLink("(//nav//a)[1]");
        checkLink(navLinkDataVault, "DATAVAULT", DV_DEMO_URL + "/");

        WebElement navLinkAdministration = getLink("(//nav//a)[2]");
        checkLink(navLinkAdministration, "Administration", DV_DEMO_URL + "/admin");

        WebElement navLinFileLocations = getLink("(//nav//a)[3]");
        checkLink(navLinFileLocations, "File Locations", DV_DEMO_URL + "/filestores");

        WebElement navLinkContact = getLink("(//nav//a)[4]");
        checkLink(navLinkContact, "Contact", "https://www.ed.ac.uk/information-services/research-support/research-data-service/contact");

        WebElement navLinkMoreInfo = getLink("(//nav//a)[5]");
        checkLink(navLinkMoreInfo, "More Info", "http://www.ed.ac.uk/is/research-support/datavault");

        WebElement navLinkLogout = getLink("(//nav//a)[6]");
        checkLink(navLinkLogout, "Logout", DV_DEMO_URL + "/auth/logout");

    }

    protected void checkLink(WebElement navLink, String text, String href) {
        assertThat(navLink).isNotNull();
        assertThat(navLink.getTagName()).isEqualTo("a");
        assertThat(navLink.getText()).isEqualTo(text);
        assertThat(navLink.getAttribute("href")).isEqualTo(href);
    }

    protected WebElement getLink(String xpath) {
        WebElement link = driver.findElement(By.xpath(xpath));
        assertThat(link.getTagName()).isEqualTo("a");
        assertThat(link.getAttribute("href")).isNotBlank();
        assertThat(link.getText()).isNotBlank();
        return link;
    }

    protected void checkFooter() {

    }

    @AfterEach
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void login() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        driver.get(DV_DEMO_URL);

        String currentURL = driver.getCurrentUrl();
        log.info("current URL [{}", currentURL);

        assertThat(currentURL).startsWith(DV_EASE_URL);

        WebElement login = driver.findElement(By.id("login"));
        assertThat(login.getTagName()).isEqualTo("input");
        assertThat(login.getAttribute("type")).isEqualTo("text");

        login.sendKeys(username);

        WebElement usernameSubmit = driver.findElement(By.id("submit"));
        assertThat(usernameSubmit.getTagName()).isEqualTo("input");
        assertThat(usernameSubmit.getAttribute("type")).isEqualTo("submit");

        usernameSubmit.click();

        WebElement passwordText = driver.findElement(By.id("password"));
        assertThat(passwordText.getTagName()).isEqualTo("input");
        assertThat(passwordText.getAttribute("type")).isEqualTo("password");
        passwordText.sendKeys(password);

        WebElement passwordSubmit = driver.findElement(By.id("submit"));
        assertThat(passwordSubmit.getTagName()).isEqualTo("input");
        assertThat(passwordSubmit.getAttribute("type")).isEqualTo("submit");

        passwordSubmit.click();

        String postPasswordURL = driver.getCurrentUrl();
        assertThat(postPasswordURL).isEqualTo(DV_DEMO_URL + "/vaults");
    }

    @SneakyThrows
    protected void checkURL(String urlSpec, int expectedResponseCode) {
        try {
            URL url = new URL(urlSpec);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setConnectTimeout(10_000);
            huc.setInstanceFollowRedirects(false);

            int responseCode = huc.getResponseCode();

            assertThat(responseCode).isEqualTo(expectedResponseCode);
        } catch (Exception ex) {
            if (ex instanceof SocketTimeoutException && ex.getMessage().equals("Connect timed out")) {
                throw new RuntimeException("timed out connecting to [%s]".formatted(urlSpec), ex);
            } else {
                throw ex;
            }
        }
    }
}
