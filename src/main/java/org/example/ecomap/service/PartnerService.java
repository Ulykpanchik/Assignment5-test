package org.example.ecomap.service;

import org.example.ecomap.model.Partner;
import org.example.ecomap.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;

    @Autowired
    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public List<Partner> getPartners() {
        return partnerRepository.findAll();
    }

    public Optional<Partner> getPartnerById(int id) {
        return partnerRepository.findById(id); // Возвращаем Optional
    }

    public Partner savePartner(Partner partner) {
        return partnerRepository.save(partner);
    }
}
