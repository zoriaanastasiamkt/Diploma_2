import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import model.UserModel;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static steps.UserSteps.createUser;
import static steps.UserSteps.deleteUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserCreationTest {
    private String accessToken;
    private UserModel genericUser;
    private Faker faker;

    @Before
    public void setUp() {
        faker = new Faker();

        // Создание уникальных данных из библиотеки JavaFaker
        String randomName = faker.name().firstName();
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password(6, 12);

        genericUser = new UserModel(randomName, randomEmail, randomPassword);
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Позитивный сценарий — успешная регистрация нового пользователя с генерацией токена")
    public void createUniqueUserTest() {
        Response response = createUser(genericUser);

        response.then()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    @Description("Негативный сценарий — попытка зарегистрировать дубликат по email")
    public void createDuplicateUserTest() {
        accessToken = createUser(genericUser).path("accessToken");

        Response duplicateResponse = createUser(genericUser);

        duplicateResponse.then()
                .statusCode(HTTP_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля Имя")
    @Description("Негативный сценарий — передаем пустое имя, остальные поля заполняем через Faker")
    public void createUserWithoutNameTest() {
        UserModel userWithoutName = new UserModel("", faker.internet().emailAddress(), faker.internet().password());

        Response response = createUser(userWithoutName);

        response.then()
                .statusCode(HTTP_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля Email")
    @Description("Негативный сценарий — передаем пустой email, остальные поля заполняем через Faker")
    public void createUserWithoutEmailTest() {
        UserModel userWithoutEmail = new UserModel(faker.name().firstName(), "", faker.internet().password());

        Response response = createUser(userWithoutEmail);

        response.then()
                .statusCode(HTTP_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }


    @Test
    @DisplayName("Создание пользователя без обязательного поля Пароль")
    @Description("Негативный сценарий — передаем пустой пароль, остальные поля заполняем через Faker")
    public void createUserWithoutPasswordTest() {
        UserModel userWithoutPassword = new UserModel(faker.name().firstName(), faker.internet().emailAddress(), "");

        Response response = createUser(userWithoutPassword);

        response.then()
                .statusCode(HTTP_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }
}