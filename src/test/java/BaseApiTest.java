import data.UserData;
import io.restassured.RestAssured;
import org.junit.BeforeClass;

public class BaseApiTest extends UserData {
    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }
}
