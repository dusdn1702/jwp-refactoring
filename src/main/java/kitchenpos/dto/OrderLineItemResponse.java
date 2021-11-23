package kitchenpos.dto;

public class OrderLineItemResponse {
    private long seq;
    private long quantity;

    public OrderLineItemResponse() {
    }

    public OrderLineItemResponse(long seq, long quantity) {
        this.seq = seq;
        this.quantity = quantity;
    }

    public long getSeq() {
        return seq;
    }

    public long getQuantity() {
        return quantity;
    }
}
