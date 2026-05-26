package steps;

import data.OrderData;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.OrderModel;
import static data.OrderData.*;
import static io.restassured.RestAssured.given;

public class OrderSteps {

    // Получение списка ингредиентов
    @Step("Send GET request to /api/ingredients")
    public static Response getIngredients() {
        return given()
                .log().all()
                .baseUri(OrderData.BASE_URI)
                .when()
                .get(OrderData.INGREDIENTS_ENDPOINT)
                .then()
                .extract().response();
    }

    // Создание заказа
    @Step("Send POST request to /api/orders")
    public static Response createOrder(OrderModel order, String accessToken) {
        RequestSpecification request = given()
                .log().all()
                .baseUri(OrderData.BASE_URI)
                .contentType(ContentType.JSON)
                .body(order);
        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .post(OrderData.CREATE_ORDER_ENDPOINT)
                .then()
                .extract().response();
    }

}