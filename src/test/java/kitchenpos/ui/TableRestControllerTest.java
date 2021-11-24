package kitchenpos.ui;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.OrderTableRequest;
import kitchenpos.dto.OrderTableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableRestControllerTest extends ControllerTest {
    static ExtractableResponse<Response> postOrderTable(OrderTableRequest orderTableRequest) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderTableRequest)
                .when().post("/api/tables")
                .then().log().all().extract();
    }

    @Test
    @DisplayName("OrderTable 생성")
    void create() {
        OrderTable orderTable = new OrderTable(null, 0, true);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());

        ExtractableResponse<Response> response = postOrderTable(orderTableRequest);
        OrderTableResponse savedOrderTable = response.as(OrderTableResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(savedOrderTable.getId()).isNotNull();
    }

    @Test
    @DisplayName("모든 OrderTable 조회")
    void list() {
        OrderTable orderTable = new OrderTable(null, 0, true);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        postOrderTable(orderTableRequest);

        OrderTable orderTable2 = new OrderTable(null, 0, true);
        OrderTableRequest orderTableRequest2 = new OrderTableRequest(orderTable2.getNumberOfGuests(), orderTable2.isEmpty());
        postOrderTable(orderTableRequest2);

        ExtractableResponse<Response> response = getOrderTables();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body().as(List.class)).hasSize(2);
    }

    @Test
    @DisplayName("빈 테이블로 변경")
    void changeEmpty() {
        OrderTable orderTable = new OrderTable(null, 3, false);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        OrderTableResponse savedOrderTable = postOrderTable(orderTableRequest).as(OrderTableResponse.class);

        OrderTable changeTable = new OrderTable(null, 3, true);
        OrderTableRequest changeOrderTableRequest = new OrderTableRequest(changeTable.getNumberOfGuests(), changeTable.isEmpty());
        ExtractableResponse<Response> response = putOrderTableEmpty(savedOrderTable.getId(), changeOrderTableRequest);
        OrderTableResponse changedTable = response.as(OrderTableResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(changedTable.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("테이블 손님 수 변경")
    void changeNumberOfGuests() {
        OrderTable orderTable = new OrderTable(null, 3, false);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        OrderTableResponse savedOrderTable = postOrderTable(orderTableRequest).as(OrderTableResponse.class);

        OrderTable changeTable = new OrderTable(null, 5, false);
        OrderTableRequest changeOrderTableRequest = new OrderTableRequest(changeTable.getNumberOfGuests(), changeTable.isEmpty());
        ExtractableResponse<Response> response = putOrderTableGuest(savedOrderTable.getId(), changeOrderTableRequest);
        OrderTableResponse changedTable = response.as(OrderTableResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(changedTable.getNumberOfGuests()).isEqualTo(changedTable.getNumberOfGuests());
    }

    private ExtractableResponse<Response> getOrderTables() {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/tables")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> putOrderTableEmpty(Long orderTableId, OrderTableRequest orderTableRequest) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderTableRequest)
                .when().put("/api/tables/" + orderTableId + "/empty")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> putOrderTableGuest(Long orderTableId, OrderTableRequest orderTableRequest) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderTableRequest)
                .when().put("/api/tables/" + orderTableId + "/number-of-guests")
                .then().log().all().extract();
    }
}
