package com.example.guess_number_game.event;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LeaderboardCacheInvalidator {
    private static final String LEADERBOARD_CACHE = "leaderboard";
    private static final String USER_INFO_CACHE = "userInfo";
    private static final String TOP_10_KEY = "top10";

    private final CacheManager cacheManager;

    public LeaderboardCacheInvalidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidateAfterCommit(LeaderboardChangedEvent event) {
        Cache userInfoCache = cacheManager.getCache(USER_INFO_CACHE);
        if (userInfoCache != null) {
            userInfoCache.evict(event.username());
        }

        if (event.leaderboardChanged()) {
            Cache leaderboardCache = cacheManager.getCache(LEADERBOARD_CACHE);
            if (leaderboardCache != null) {
                leaderboardCache.evict(TOP_10_KEY);
            }
        }
    }
}
