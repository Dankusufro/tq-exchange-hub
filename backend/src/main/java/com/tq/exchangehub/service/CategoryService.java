package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(DtoMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto create(Category category) {
        Category saved = categoryRepository.save(category);
        return DtoMapper.toCategoryDto(saved);
    }
}
