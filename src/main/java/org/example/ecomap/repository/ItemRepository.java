package org.example.ecomap.repository;

import org.example.ecomap.model.Item;
import org.example.ecomap.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByCategory(String category);
    List<Item> findByUserEmail(String userEmail);
    Optional<Item> findById(int id);
}