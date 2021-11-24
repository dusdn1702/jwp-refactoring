package kitchenpos.menu.domain;

import kitchenpos.menu.dto.MenuGroupRequest;

import javax.persistence.*;

@Entity
@Table(name = "menu_group")
public class MenuGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected MenuGroup() {
    }

    public MenuGroup(String name) {
        this.name = name;
    }

    public MenuGroup(Long id, String name) {
        this(name);
        this.id = id;
    }

    public static MenuGroup of(MenuGroupRequest menuGroupRequest) {
        return new MenuGroup(menuGroupRequest.getName());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
