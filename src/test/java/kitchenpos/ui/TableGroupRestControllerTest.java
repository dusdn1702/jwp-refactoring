package kitchenpos.ui;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static kitchenpos.ui.TableRestControllerTest.postOrderTable;
import static org.assertj.core.api.Assertions.assertThat;

class TableGroupRestControllerTest extends ControllerTest {
    @Test
    @DisplayName("TableGroup 생성")
    void create() {
        TableGroup tableGroup = new TableGroup(LocalDateTime.now());

        OrderTable orderTable = new OrderTable(0, true);
        orderTable = postOrderTable(orderTable).as(OrderTable.class);

        OrderTable orderTable2 = new OrderTable(0, true);
        orderTable2 = postOrderTable(orderTable2).as(OrderTable.class);

        List<OrderTable> orderTables = new ArrayList<>();
        orderTables.add(orderTable);
        orderTables.add(orderTable2);
        tableGroup.addAllOrderTables(orderTables);

        ExtractableResponse<Response> response = postTableGroup(tableGroup);
        TableGroup savedTableGroup = response.as(TableGroup.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(savedTableGroup.getId()).isNotNull();
    }

    @Test
    @DisplayName("TableGroup 해제")
    void ungroup() {
        TableGroup tableGroup = new TableGroup(LocalDateTime.now());

        OrderTable orderTable = new OrderTable(0, true);
        orderTable = postOrderTable(orderTable).as(OrderTable.class);

        OrderTable orderTable2 = new OrderTable(0, true);
        orderTable2 = postOrderTable(orderTable2).as(OrderTable.class);

        List<OrderTable> orderTables = new ArrayList<>();
        orderTables.add(orderTable);
        orderTables.add(orderTable2);

        tableGroup.addAllOrderTables(orderTables);
        TableGroup savedTableGroup = postTableGroup(tableGroup).as(TableGroup.class);

        ExtractableResponse<Response> response = deleteTableGroup(savedTableGroup.getId());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    static ExtractableResponse<Response> postTableGroup(TableGroup tableGroup) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(tableGroup)
                .when().post("/api/table-groups")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> deleteTableGroup(Long tableGroupId) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/api/table-groups/" + tableGroupId)
                .then().log().all().extract();
    }
}
