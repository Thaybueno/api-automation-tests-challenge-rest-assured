import entities.Booking;
import entities.BookingDates;
import entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;

    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        bookingDates = new BookingDates("2018-01-02", "2018-01-03");
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (float)faker.number().randomDouble(2, 50, 100000),
                true,bookingDates,
                "");

       RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin", "password123");
    }

    @Test 
    public void CreateToken_returnOk(){
        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "password123");

        request           
            .when()
                .body(body)
                .post("/auth")
            .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void getAllBookingsById_returnOk(){
      Response response = request
      .when()
      .get("/booking")
      .then()
      .extract()
      .response();
      
      Assertions.assertNotNull(response);
      Assertions.assertEquals(200, response.statusCode());
    }
    
    @Test
    public void  getAllBookingsByUserFirstName_BookingExists_returnOk(){
                 request
                 .when()
                     .queryParam("firstName", "Bianca")
                     .get("/booking")
                 .then()
                     .assertThat()
                     .statusCode(200)
                     .contentType(ContentType.JSON)
                 .and()
                 .body("results", hasSize(greaterThan(0)));

    }

    @Test
    public void  getAllBookingsByTotalPrice_returnOk(){
        request
            .when()
                .queryParam("totalprice", 1000)
                .get("/booking")
            .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
            .and()
                .body("results", hasSize(greaterThan(0)))
        ;
    }

    @Test
    public void  CreateBooking_WithValidData_returnOk(){
                 request                    
                 .when()
                   .body(booking)
                   .post("/booking")
                 .then()
                   .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                 .and()
                   .assertThat()
                   .statusCode(200)
                   .contentType(ContentType.JSON).and().time(lessThan(2000L));
    }
   
    @Test
    public void HealthCheck(){
       Response response = request
            .when()
                .get("/ping")
            .then()
            .extract().
            response()
        ;

        Assertions.assertEquals(201, response.statusCode());
    }
  
  }
