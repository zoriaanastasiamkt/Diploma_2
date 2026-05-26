import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import model.UserModel;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static steps.UserSteps.createUser;
import static steps.UserSteps.loginUser;
import static steps.UserSteps.deleteUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserLoginTest extends BaseApiTest {
    private String accessToken;
    private UserModel genericUser;
    private Faker faker;

    @Before
    public void setUp() {
        faker = new Faker();

        String randomName = faker.name().firstName();
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password(6, 12);

        genericUser = new UserModel(randomName, randomEmail, randomPassword);
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Позитивный сценарий — успешный логин с валидными учетными данными")
    public void loginExistingUserTest() {
        accessToken = createUser(genericUser).path("accessToken");

        Response loginResponse = loginUser(genericUser);

        loginResponse.then()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с несуществующим логином (email)")
    @Description("Негативный сценарий — попытка авторизации с почтой, которой нет в системе")
    public void loginWithNonexistentEmailTest() {
        UserModel nonexistentUser = new UserModel("FakeName", faker.internet().emailAddress(), "anyPassword123");

        Response response = loginUser(nonexistentUser);

        response.then()
                .statusCode(HTTP_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Негативный сценарий — пытаемся войти под существующим email, но с ошибочным паролем")
    public void loginWithIncorrectPasswordTest() {
        accessToken = createUser(genericUser).path("accessToken");

        UserModel userWithWrongPassword = new UserModel(genericUser.getName(), genericUser.getEmail(), "wrong_password_123");

        Response response = loginUser(userWithWrongPassword);

        response.then()
                .statusCode(HTTP_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }
}