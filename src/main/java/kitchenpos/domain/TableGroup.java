package kitchenpos.domain;

import kitchenpos.dto.TableGroupRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "table_group")
public class TableGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @OneToMany
    private List<OrderTable> orderTables;

    protected TableGroup() {

    }

    public TableGroup(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public TableGroup(Long id, LocalDateTime createdDate) {
        this(createdDate);
        this.id = id;
    }

    public TableGroup(LocalDateTime createdDate, List<OrderTable> orderTables) {
        this(createdDate);
        this.orderTables = orderTables;
    }

    public static TableGroup of(TableGroupRequest tableGroupRequest) {
        List<OrderTable> orderTables = tableGroupRequest.getOrderTableRequests().stream()
                .map(OrderTable::of)
                .collect(Collectors.toList());

        return new TableGroup(tableGroupRequest.getCreatedDate(), orderTables);
    }

    public void addAllOrderTables(List<OrderTable> orderTables) {
        this.orderTables = orderTables;
    }

    public void tableCreatedAt(final LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }
}
