package selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final String BASE_URL = "https://tienda-sana-frontend.vercel.app/";

    @BeforeEach
    public void setUp() {
        // Asegúrate de que la ruta al ChromeDriver sea correcta para tu sistema.
        // Considera usar WebDriverManager para una gestión automática del driver.
        System.setProperty("webdriver.chrome.driver", "C:/Users/user/ZAP/webdriver/windows/64/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize(); // Maximizar la ventana para una mejor visualización
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Espera implícita
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Espera explícita base
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Helper method for logging in.
     * Adjust credentials and selectors as needed.
     * @param email User's email
     * @param password User's password
     */
    protected void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("contrasenia")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        // Wait for a specific element that indicates successful login, e.g., logout button
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Cerrar Sesión")));
    }
}
