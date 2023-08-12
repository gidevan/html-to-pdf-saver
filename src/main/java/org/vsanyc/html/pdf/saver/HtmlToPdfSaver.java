package org.vsanyc.html.pdf.saver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PrintOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HtmlToPdfSaver {

    private static  final String PAGE_URL = "https://docs.google.com/forms/d/e/1FAIpQLSfn8MpjwAZDZChX_c1bMRevgGFDtYLv8F-OtNSyTswHXxlGUg/viewform";

    private static final String INPUT_FILE_NAME = "input-urls.txt";

    private static final String CONFIG_PROPERTIES = "saver-config.properties";

    public static void main(String[] args) throws Exception {
        var properties = init();
        readInputFile(properties);
    }

    private static SaverProperties init() throws IOException {
        var props = new Properties();
        //var stream = HtmlToPdfSaver.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES);
        var pathToJar = HtmlToPdfSaver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println("Path to jar: " + pathToJar);

        String dir = System.getProperty("user.dir");
        System.out.println("user.dir:" + dir);
        var stream = new FileInputStream(pathToJar + "/" +CONFIG_PROPERTIES);

        props.load(stream);

        var inputFolder = (String) props.get("inputPaths");
        var outputFolder = (String)props.get("outputFolder");
        var saverProperties = new SaverProperties();
        saverProperties.setInputPaths(inputFolder);
        saverProperties.setOutputFolder(outputFolder);
        return saverProperties;
    }

    private static void readInputFile(SaverProperties saverProperties) throws Exception {
        URL inputFilePath = HtmlToPdfSaver.class.getClassLoader().getResource(saverProperties.getInputPaths());
        List<String> lines = Files.readAllLines(Paths.get(inputFilePath.getPath()));

        for(String line: lines) {
            System.out.println("Try to download [" + line + "]");
            downloadFile(line, saverProperties);
        }
    }

    private static void downloadFile(String pageUrl, SaverProperties saverProperties) throws Exception {
        var chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);

        var sauceOptions = new HashMap<>();
        sauceOptions.put("name", "printPageWithChrome");
        chromeOptions.setCapability("sauce:options", sauceOptions);

        var driver = new ChromeDriver(chromeOptions);


        driver.get(pageUrl);
        var title = driver.getTitle();
        var printPage = Paths.get(saverProperties.getOutputFolder() + title + ".pdf");
        var print = driver.print(new PrintOptions());
        Files.write(printPage, OutputType.BYTES.convertFromBase64Png(print.getContent()));
    }

}
