package com.hopeguide.repository;

import com.hopeguide.entity.MoodLog;
import com.hopeguide.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoodLogRepository extends JpaRepository<MoodLog, Long> {
    List<MoodLog> findTop7ByUserOrderByLoggedAtDesc(User user);
}
