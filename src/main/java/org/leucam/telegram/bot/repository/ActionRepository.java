package org.leucam.telegram.bot.repository;

import org.leucam.telegram.bot.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActionRepository extends JpaRepository<Action, Long> {
    Optional<Action> findByTelegramUserIdAndInProgressTrue(Integer telegramUserId);
}
