package de.andre.repository;

import de.andre.entity.catalog.DcsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by andreika on 4/21/2015.
 */
public interface CatalogRepository extends JpaRepository<DcsCategory, String> {
	@Query("select c from DcsCategory c where c.parentCategory.rootCategory = true")
	List<DcsCategory> getRootChildCategories();
}
