package kitchenpos.ui;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.OrderTableRequest;
import kitchenpos.dto.OrderTableResponse;
import kitchenpos.dto.TableGroupRequest;
import kitchenpos.dto.TableGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.ui.TableRestControllerTest.postOrderTable;
import static org.assertj.core.api.Assertions.assertThat;

class TableGroupRestControllerTest extends ControllerTest {
    private TableGroupRequest tableGroupRequest;

    static ExtractableResponse<Response> postTableGroup(TableGroupRequest tableGroupRequest) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(tableGroupRequest)
                .when().post("/api/table-groups")
                .then().log().all().extract();
    }

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();

        TableGroup tableGroup = new TableGroup(LocalDateTime.now());

        OrderTable orderTable = new OrderTable(0, true);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        OrderTableResponse orderTableResponse = postOrderTable(orderTableRequest).as(OrderTableResponse.class);
        orderTable = new OrderTable(orderTableResponse.getId(), orderTableResponse.getNumberOfGuests(), orderTableResponse.isEmpty());

        OrderTable orderTable2 = new OrderTable(0, true);
        OrderTableRequest orderTableRequest2 = new OrderTableRequest(orderTable2.getNumberOfGuests(), orderTable2.isEmpty());
        OrderTableResponse orderTableResponse2 = postOrderTable(orderTableRequest2).as(OrderTableResponse.class);
        orderTable2 = new OrderTable(orderTableResponse2.getId(), orderTableResponse2.getNumberOfGuests(), orderTableResponse2.isEmpty());

        List<OrderTable> orderTables = new ArrayList<>();
        orderTables.add(orderTable);
        orderTables.add(orderTable2);
        tableGroup.addAllOrderTables(orderTables);

        List<OrderTableRequest> orderTableRequests = tableGroup.getOrderTables().stream()
                .map(table -> new OrderTableRequest(table.getId(), table.getNumberOfGuests(), table.isEmpty()))
                .collect(Collectors.toList());
        tableGroupRequest = new TableGroupRequest(tableGroup.getCreatedDate(), orderTableRequests);
    }

    @Test
    @DisplayName("TableGroup 생성")
    void create() {
        ExtractableResponse<Response> response = postTableGroup(tableGroupRequest);
        TableGroupResponse savedTableGroup = response.as(TableGroupResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(savedTableGroup.getId()).isNotNull();
    }

    @Test
    @DisplayName("TableGroup 해제")
    void ungroup() {
        TableGroupResponse savedTableGroup = postTableGroup(tableGroupRequest).as(TableGroupResponse.class);

        ExtractableResponse<Response> response = deleteTableGroup(savedTableGroup.getId());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
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
