package pl.taskmanager.taskmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Category() {}

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public User getUser() { return user; }

    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setUser(User user) { this.user = user; }
}
