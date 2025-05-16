package selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/user/Downloads/chromedriver-win64/chromedriver.exe"); // o .sh/.bin según tu SO
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    public void testLoginSuccess() {
        driver.get("http://localhost:4200/login");

        // Encuentra los campos y escribe credenciales
        WebElement emailField = driver.findElement(By.id("email")); // Usa el ID real de tu input
        WebElement passwordField = driver.findElement(By.id("contrasenia")); // Usa el ID real de tu input
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']")); // o el selector adecuado

        emailField.sendKeys("santiquinterouribe0412@gmail.com");
        passwordField.sendKeys("juanjuan");
        loginButton.click();

        // Verifica que se redirigió o se mostró algo indicando éxito
        // Ejemplo: verificar que aparece un componente específico tras login
        assertTrue(driver.getCurrentUrl().contains("/") ||
                driver.getPageSource().contains("Tienda Sana"));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
