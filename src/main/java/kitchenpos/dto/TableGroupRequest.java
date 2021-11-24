package kitchenpos.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TableGroupRequest {
    private LocalDateTime createdDate;
    private List<OrderTableRequest> orderTableRequests;

    public TableGroupRequest() {
    }

    public TableGroupRequest(LocalDateTime createdDate, List<OrderTableRequest> orderTableRequests) {
        this.createdDate = createdDate;
        this.orderTableRequests = orderTableRequests;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTableRequest> getOrderTableRequests() {
        return orderTableRequests;
    }
}
