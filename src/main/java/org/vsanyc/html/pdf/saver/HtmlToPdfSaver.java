package org.vsanyc.html.pdf.saver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PrintOptions;

import java.io.*;
import java.net.MalformedURLException;
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
        var configStream = readConfig();

        props.load(configStream);

        var inputFolder = (String) props.get("inputPaths");
        var outputFolder = (String)props.get("outputFolder");
        var saverProperties = new SaverProperties();
        saverProperties.setInputPaths(inputFolder);
        saverProperties.setOutputFolder(outputFolder);
        return saverProperties;
    }

    private static InputStream readConfig() throws IOException{
        String userDir = System.getProperty("user.dir");
        System.out.println("user.dir:" + userDir);
        var configFile = new File(userDir + "/" + CONFIG_PROPERTIES);
        if (configFile.exists() && !configFile.isDirectory()) {
            return new FileInputStream(userDir + "/" + CONFIG_PROPERTIES);
        } else {
            return HtmlToPdfSaver.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES);
        }
    }
    private static void readInputFile(SaverProperties saverProperties) throws Exception {

        var inputFilePath = getInputPaths(saverProperties);
        List<String> lines = Files.readAllLines(Paths.get(inputFilePath.getPath()));

        for(String line: lines) {
            System.out.println("Try to download [" + line + "]");
            downloadFile(line, saverProperties);
        }
    }

    private static URL getInputPaths(SaverProperties saverProperties) throws MalformedURLException {
        String userDir = System.getProperty("user.dir");
        System.out.println("user.dir:" + userDir);
        var inputPaths = new File(userDir + "/" + saverProperties.getInputPaths());
        if (inputPaths.exists() && !inputPaths.isDirectory()) {
            return new File(userDir + "/" + saverProperties.getInputPaths()).toURI().toURL();
        } else {
            return HtmlToPdfSaver.class.getClassLoader().getResource(saverProperties.getInputPaths());
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
        createOutputFolder(saverProperties);
        var printPage = Paths.get(saverProperties.getOutputFolder() + "/" + title + ".pdf");
        var print = driver.print(new PrintOptions());
        Files.write(printPage, OutputType.BYTES.convertFromBase64Png(print.getContent()));
    }

    private static void createOutputFolder(SaverProperties saverProperties) throws IOException{
        var outputFolderPath = "." + "/" + saverProperties.getOutputFolder();
        if (!Files.isDirectory(Paths.get(outputFolderPath))) {
            Files.createDirectories(Paths.get(outputFolderPath));
        } else {
            System.out.println("Folder [" + outputFolderPath + "] exists");
        }
    }

}
