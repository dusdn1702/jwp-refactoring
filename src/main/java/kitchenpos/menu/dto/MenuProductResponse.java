package kitchenpos.menu.dto;

public class MenuProductResponse {
    private long seq;
    private long quantity;

    public MenuProductResponse() {
    }

    public MenuProductResponse(long seq, long quantity) {
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
