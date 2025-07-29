package id.co.hibank.benchmark.jdbc.model;

public class DummyEntity {
    private Long id;
    private String name;

    public DummyEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
