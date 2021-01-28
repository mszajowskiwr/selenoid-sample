package org.selenide.examples.selenoid;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.FileDownloadMode;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class FileDownloadTest {

    SelenideElement downloadable = $("#downloadable");
    SelenideElement navigable = $("#navigable");
    SelenideElement title = $("#title");

    private static String getStdOutputOf(String commandStr) throws IOException {
        Process process = Runtime.getRuntime().exec(String.format(commandStr));
        String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8.name());
        return output;
    }

    @BeforeAll
    static void setUpAll() {
        Configuration.baseUrl = "http://10.0.0.70:8888/"; // please replace this with your local IP and port
    }

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.deleteDirectory(new File("./build/"));
    }

    @AfterEach
    void tearDown() {
        WebDriverRunner.getWebDriver().close();
    }

    @Test
    void thisWorks() throws IOException, InterruptedException {
        disableSelenoid();
        twoDownloads(); // passes
    }

    @Test
    void thisAlsoWorks() throws IOException, InterruptedException {
        disableSelenoid();
        threeDownloads(); // passes
    }

    @Test
    void thisDoesntWork() throws IOException, InterruptedException {
        enableSelenoid();
        twoDownloads(); // java.lang.AssertionError: Expected size:<2> but was:<1> in:

    }

    @Test
    void thisAlsoDoesntWork() throws IOException, InterruptedException {
        enableSelenoid();
        threeDownloads(); // java.io.FileNotFoundException: Failed to download file {#navigable} in 4000 ms.
    }

    void twoDownloads() throws IOException, InterruptedException {

        Selenide.open("");
        title.shouldHave(text("This is freshly opened page"));

        downloadable.click(); // this triggers downloading
        downloadable.download(); // this triggers downloading as well
        title.shouldHave(text("This is freshly opened page")); // we're still on the same page

        navigable.click();  // this triggers navigating
        title.shouldHave(text("This is page that's been navigated to")); // new page has been navigated to

        List<String> downloadedFiles = Arrays.stream(getStdOutputOf("find build -name index.html").split("\n"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // we expect 2 files to be downloaded: via downloadable.click() and downloadable.download()
        assertThat(downloadedFiles).hasSize(2);
    }

    void threeDownloads() throws IOException, InterruptedException {

        Selenide.open("");
        title.shouldHave(text("This is freshly opened page"));

        downloadable.click(); // this triggers downloading
        downloadable.download(); // this triggers downloading as well
        title.shouldHave(text("This is freshly opened page")); // we're still on the same page

        navigable.download(); // this triggers downloading, NOTE: this is navigable link!
        title.shouldHave(text("This is freshly opened page")); // we're still on the same page

        navigable.click();  // this triggers navigating
        title.shouldHave(text("This is page that's been navigated to")); // new page has been navigated to

        List<String> downloadedFiles = Arrays.stream(getStdOutputOf("find build -name index.html").split("\n"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // we expect 3 files to be downloaded, one via downloadable.click(), other via downloadable.download() and the last via navigable.download()
        assertThat(downloadedFiles).hasSize(3);
    }

    private void enableSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.fileDownload = FileDownloadMode.FOLDER;  // as mentioned in https://github.com/selenide/selenide-selenoid#usage
        Configuration.downloadsFolder = "build/downloads";
    }

    private void disableSelenoid() {
        Configuration.remote = null;
        Configuration.fileDownload = FileDownloadMode.HTTPGET;  // setting default value
        Configuration.downloadsFolder = "build/downloads";
    }


}
