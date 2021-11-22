package kitchenpos.dto;

public class MenuGroupResponse {
    private long id;
    private String name;

    public MenuGroupResponse() {}

    public MenuGroupResponse(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
