package org.example.ecomap.service;

import org.example.ecomap.model.Item;
import org.example.ecomap.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category);
    }

    public List<Item> getItemsByUserEmail(String userEmail) {
        return itemRepository.findByUserEmail(userEmail);
    }

    public Item getItemById(int id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(int id) {
        itemRepository.deleteById(id);
    }
}