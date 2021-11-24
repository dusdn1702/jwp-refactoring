package kitchenpos.domain;

import kitchenpos.dto.OrderTableRequest;
import kitchenpos.dto.OrderTableResponse;

import javax.persistence.*;

@Entity
@Table(name = "order_table")
public class OrderTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "table_group_id", foreignKey = @ForeignKey(name = "fk_order_table_table_group"))
    private TableGroup tableGroup;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    private boolean empty;

    protected OrderTable() {
    }

    public OrderTable(int numberOfGuests, boolean empty) {
        this.numberOfGuests = numberOfGuests;
        this.empty = empty;
    }

    public OrderTable(Long id, int numberOfGuests, boolean empty) {
        this(numberOfGuests, empty);
        this.id = id;
    }

    public static OrderTable of(OrderTableResponse orderTableResponse) {
        return new OrderTable(orderTableResponse.getId(), orderTableResponse.getNumberOfGuests(), orderTableResponse.isEmpty());
    }

    public static OrderTable of(OrderTableRequest orderTableRequest) {
        return new OrderTable(orderTableRequest.getId(), orderTableRequest.getNumberOfGuests(), orderTableRequest.isEmpty());
    }

    public void makeEmpty(boolean empty) {
        this.empty = empty;
    }

    public void changeNumberOfGuests(OrderTable orderTable) {
        this.numberOfGuests = orderTable.numberOfGuests;
    }

    public boolean isInvalidGuestNumbers() {
        return numberOfGuests < 0;
    }

    public Long getId() {
        return id;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }
}
