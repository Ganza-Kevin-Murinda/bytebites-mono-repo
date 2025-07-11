package com.restaurant_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String location;

    @Column(nullable = false)
    private String type;

    @Column(name = "owner_id", nullable = false, unique = true)
    private String ownerId;

}
