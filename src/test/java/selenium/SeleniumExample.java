package selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;

public class SeleniumExample {
    public static void main(String[] args) {
        // Configurar la ubicación del WebDriver
        System.setProperty("webdriver.chrome.driver", "C:/Users/user/Downloads/chromedriver-win64/chromedriver.exe");

        // Crear una instancia del navegador
        WebDriver driver = new ChromeDriver();

        try {
            // Navegar a una página web
            driver.get("https://www.google.com");

            // Encontrar un elemento (por ejemplo, el campo de búsqueda)
            WebElement searchBox = driver.findElement(By.name("q"));

            // Escribir texto en el campo de búsqueda
            searchBox.sendKeys("Selenium WebDriver");

            // Enviar el formulario
            searchBox.submit();

            // Esperar unos segundos para ver el resultado
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar el navegador
            driver.quit();
        }
    }
}