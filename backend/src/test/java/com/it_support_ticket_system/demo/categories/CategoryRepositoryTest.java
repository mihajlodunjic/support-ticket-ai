package com.it_support_ticket_system.demo.categories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void seededCategoriesAreAvailableFromLabelMapping() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderByNameAsc();
        List<String> categoryNames = categories.stream().map(Category::getName).toList();

        assertThat(categories).hasSize(8);
        assertThat(categoryNames)
            .containsExactlyInAnyOrder(
                "Access",
                "Administrative rights",
                "HR Support",
                "Hardware",
                "Internal Project",
                "Miscellaneous",
                "Purchase",
                "Storage"
            );
        assertThat(categoryNames).isSorted();
    }

    @Test
    void categoryCanBeSavedAndLoaded() {
        Category category = new Category("Printer Support", "Non-seeded test category.", true);

        Category savedCategory = categoryRepository.saveAndFlush(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getCreatedAt()).isNotNull();
        assertThat(savedCategory.getUpdatedAt()).isNotNull();
        assertThat(categoryRepository.findByNameIgnoreCase("printer support")).contains(savedCategory);
        assertThat(categoryRepository.existsByNameIgnoreCase("PRINTER SUPPORT")).isTrue();
    }
}
