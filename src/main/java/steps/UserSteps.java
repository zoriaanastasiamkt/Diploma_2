package steps;

import data.UserData;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static data.UserData.*;
import static io.restassured.RestAssured.given;
import model.UserModel;

public class UserSteps {

    // Создание пользователя
    @Step("Send POST request to /api/auth/register")
    public static Response createUser(UserModel user) {
        return given()
                .log().all()
                .baseUri(UserData.BASE_URI)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post(CREATE_USER_ENDPOINT)
                .then()
                .extract().response();
    }

    // Авторизация пользователя
    @Step("Send POST request to /api/auth/login")
    public static Response loginUser(UserModel user) {
        return given()
                .log().all()
                .baseUri(UserData.BASE_URI)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post(LOGIN_USER_ENDPOINT)
                .then()
                .extract().response();
    }

    // Удаление пользователя
    @Step("Send DELETE request to /api/auth/user")
    public static Response deleteUser(String accessToken) {
        return given()
                .log().all()
                .baseUri(UserData.BASE_URI)
                .header("Authorization", accessToken)
                .when()
                .delete(DELETE_USER_ENDPOINT)
                .then()
                .extract().response();
    }

}