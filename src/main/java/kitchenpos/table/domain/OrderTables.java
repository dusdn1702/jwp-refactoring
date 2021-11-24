package kitchenpos.table.domain;

import java.util.List;
import java.util.stream.Collectors;

public class OrderTables {
    private final List<OrderTable> tables;

    public OrderTables(List<OrderTable> tables) {
        this.tables = tables;
    }

    public boolean isValidSize() {
        return tables.isEmpty() || tables.size() < 2;
    }

    public boolean isNotSameSize(OrderTables comparedOrderTables) {
        return tables.size() != comparedOrderTables.tables.size();
    }

    public boolean isAllEmpty() {
        return tables.stream()
                .allMatch(OrderTable::isEmpty);
    }

    public List<OrderTable> getTables() {
        return tables;
    }

    public List<Long> getOrderTableIds() {
        return tables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());
    }
}
