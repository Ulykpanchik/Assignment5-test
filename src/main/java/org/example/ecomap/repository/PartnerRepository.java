package org.example.ecomap.repository;

import org.example.ecomap.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Integer> {
    Optional<Partner> findById(int id); // Найти партнера по id
}