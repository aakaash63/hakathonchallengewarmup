package com.hopeguide.repository;

import com.hopeguide.entity.SupportScript;
import com.hopeguide.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportScriptRepository extends JpaRepository<SupportScript, Long> {
    List<SupportScript> findTop10ByUserOrderByCreatedAtDesc(User user);
}
