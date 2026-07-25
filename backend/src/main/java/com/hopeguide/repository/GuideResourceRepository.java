package com.hopeguide.repository;

import com.hopeguide.entity.GuideResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuideResourceRepository extends JpaRepository<GuideResource, Long> {
    List<GuideResource> findByCategory(String category);
    List<GuideResource> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary);
}
