import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import model.OrderModel;
import model.UserModel;
import static java.net.HttpURLConnection.*;
import static steps.OrderSteps.createOrder;
import static steps.OrderSteps.getIngredients;
import static steps.UserSteps.createUser;
import static steps.UserSteps.deleteUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import java.util.ArrayList;
import java.util.List;

public class OrderTest {
    private String accessToken;
    private UserModel user;
    private Faker faker;

    private List<String> allIngredientIds;
    private List<String> validIngredientsOption1;
    private List<String> validIngredientsOption2;

    @Before
    public void setUp() {
        faker = new Faker();

        Response ingredientsResponse = getIngredients();

        allIngredientIds = ingredientsResponse.path("data._id");

        // Комбинации ингредиентов для тестов:
        // вариант 1 Первые два ингредиента
        validIngredientsOption1 = List.of(allIngredientIds.get(0), allIngredientIds.get(1));
        // вариант 2 Третий ингредиент
        validIngredientsOption2 = List.of(allIngredientIds.get(2));

        // Генерируем случайного пользователя через библиотеку Faker
        String randomName = faker.name().firstName();
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password(6, 12);
        user = new UserModel(randomName, randomEmail, randomPassword);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Позитивный сценарий — авторизованный пользователь создает заказ с двумя ингредиентами")
    public void createOrderWithAuthTest() {
        accessToken = createUser(user).path("accessToken");

        OrderModel orderRequest = new OrderModel(validIngredientsOption1);
        Response response = createOrder(orderRequest, accessToken);

        response.then()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Позитивный сценарий — создание заказа неавторизованным пользователем с одним ингредиентом")
    public void createOrderWithoutAuthTest() {
        OrderModel orderRequest = new OrderModel(validIngredientsOption2);
        Response response = createOrder(orderRequest, null);

        response.then()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    @Description("Позитивный сценарий — создание заказа, для примера со всем доступным списком ингредиентов, хранимых в бэкенде")
    public void createOrderWithIngredientsTest() {
        accessToken = createUser(user).path("accessToken");

        OrderModel orderRequest = new OrderModel(allIngredientIds);

        Response response = createOrder(orderRequest, accessToken);

        response.then()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Негативный сценарий — попытка отправить заказ с пустым списком")
    public void createOrderWithoutIngredientsTest() {
        accessToken = createUser(user).path("accessToken");

        OrderModel orderRequest = new OrderModel(new ArrayList<>());
        Response response = createOrder(orderRequest, accessToken);

        response.then()
                .statusCode(HTTP_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Негативный сценарий — передаем отсутствующий на бэкенде ID")
    public void createOrderWithInvalidIngredientHashTest() {
        accessToken = createUser(user).path("accessToken");

        List<String> invalidIngredients = List.of("invalid_hash_1234567890");
        OrderModel orderRequest = new OrderModel(invalidIngredients);

        Response response = createOrder(orderRequest, accessToken);

        response.then()
                .statusCode(HTTP_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

}