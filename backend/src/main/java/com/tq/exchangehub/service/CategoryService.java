package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    public CategoryService(CategoryRepository categoryRepository, ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> DtoMapper.toCategoryDto(category, itemRepository.countByCategory(category)))
                .collect(Collectors.toList());
    }

    public CategoryDto create(Category category) {
        Category saved = categoryRepository.save(category);
        return DtoMapper.toCategoryDto(saved);
    }
}
