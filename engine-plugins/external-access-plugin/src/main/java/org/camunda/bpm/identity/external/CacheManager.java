package org.camunda.bpm.identity.external;

import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
import org.camunda.bpm.identity.external.apiVO.UserVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 * 提供统一的缓存管理功能，支持多种数据类型的缓存
 */
public class CacheManager {
    
    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    // 用户缓存
    private final Map<String, CachedData<BpmnResponseVO<List<UserVO>>>> userCache = new ConcurrentHashMap<>();
    
    // 组缓存
    private final Map<String, CachedData<Map<String, Object>>> groupCache = new ConcurrentHashMap<>();
    
    // 组列表缓存
    private final Map<String, CachedData<List<Map<String, Object>>>> groupListCache = new ConcurrentHashMap<>();
    
    // 缓存配置
    private static final long DEFAULT_CACHE_DURATION_MS = 5 * 60 * 1000; // 5分钟
    private static final long CLEANUP_INTERVAL_MS = 10 * 60 * 1000; // 10分钟清理一次过期缓存
    private static final int MAX_CACHE_SIZE = 5000; // 最大缓存条目数
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupExecutor;
    
    public CacheManager() {
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CacheManager-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        // 启动定时清理任务
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 
            CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
        
        LOG.writeLog("CacheManager initialized with cleanup interval: " + CLEANUP_INTERVAL_MS + "ms");
    }
    
    /**
     * 缓存数据包装类
     */
    public static class CachedData<T> {
        private final T data;
        private final long timestamp;
        private final long duration;
        
        public CachedData(T data, long timestamp, long duration) {
            this.data = data;
            this.timestamp = timestamp;
            this.duration = duration;
        }
        
        public CachedData(T data, long timestamp) {
            this(data, timestamp, DEFAULT_CACHE_DURATION_MS);
        }
        
        public T getData() {
            return data;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > duration;
        }
        
        public long getRemainingTime() {
            return Math.max(0, duration - (System.currentTimeMillis() - timestamp));
        }
    }
    
    // ==================== 用户缓存相关方法 ====================
    
    /**
     * 获取用户缓存
     */
    public BpmnResponseVO<List<UserVO>> getUserCache(String key) {
        CachedData<BpmnResponseVO<List<UserVO>>> cached = userCache.get(key);
        if (cached != null && !cached.isExpired()) {
            LOG.writeLog("Cache hit for user key: " + key);
            return cached.getData();
        }
        return null;
    }
    
    /**
     * 设置用户缓存
     */
    public void putUserCache(String key, BpmnResponseVO<List<UserVO>> data) {
        putUserCache(key, data, DEFAULT_CACHE_DURATION_MS);
    }
    
    /**
     * 设置用户缓存（自定义过期时间）
     */
    public void putUserCache(String key, BpmnResponseVO<List<UserVO>> data, long durationMs) {
        if (data != null) {
            checkCacheSize(userCache);
            userCache.put(key, new CachedData<>(data, System.currentTimeMillis(), durationMs));
            LOG.writeLog("User data cached with key: " + key + ", duration: " + durationMs + "ms");
        }
    }
    
    /**
     * 清除用户缓存
     */
    public void clearUserCache() {
        userCache.clear();
        LOG.writeLog("User cache cleared");
    }
    
    /**
     * 移除特定用户缓存
     */
    public void removeUserCache(String key) {
        userCache.remove(key);
        LOG.writeLog("User cache removed for key: " + key);
    }
    
    // ==================== 组缓存相关方法 ====================
    
    /**
     * 获取组缓存
     */
    public Map<String, Object> getGroupCache(String key) {
        CachedData<Map<String, Object>> cached = groupCache.get(key);
        if (cached != null && !cached.isExpired()) {
            LOG.writeLog("Cache hit for group key: " + key);
            return cached.getData();
        }
        return null;
    }
    
    /**
     * 设置组缓存
     */
    public void putGroupCache(String key, Map<String, Object> data) {
        if (data != null) {
            checkCacheSize(groupCache);
            groupCache.put(key, new CachedData<>(data, System.currentTimeMillis()));
            LOG.writeLog("Group data cached with key: " + key);
        }
    }
    
    /**
     * 清除组缓存
     */
    public void clearGroupCache() {
        groupCache.clear();
        LOG.writeLog("Group cache cleared");
    }
    
    // ==================== 组列表缓存相关方法 ====================
    
    /**
     * 获取组列表缓存
     */
    public List<Map<String, Object>> getGroupListCache(String key) {
        CachedData<List<Map<String, Object>>> cached = groupListCache.get(key);
        if (cached != null && !cached.isExpired()) {
            LOG.writeLog("Cache hit for group list key: " + key);
            return cached.getData();
        }
        return null;
    }
    
    /**
     * 设置组列表缓存
     */
    public void putGroupListCache(String key, List<Map<String, Object>> data) {
        if (data != null) {
            checkCacheSize(groupListCache);
            groupListCache.put(key, new CachedData<>(data, System.currentTimeMillis()));
            LOG.writeLog("Group list data cached with key: " + key);
        }
    }
    
    /**
     * 清除组列表缓存
     */
    public void clearGroupListCache() {
        groupListCache.clear();
        LOG.writeLog("Group list cache cleared");
    }
    
    // ==================== 通用缓存管理方法 ====================
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        clearUserCache();
        clearGroupCache();
        clearGroupListCache();
        LOG.writeLog("All caches cleared");
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            userCache.size(),
            groupCache.size(),
            groupListCache.size(),
            countExpiredEntries(userCache),
            countExpiredEntries(groupCache),
            countExpiredEntries(groupListCache)
        );
    }
    
    /**
     * 生成缓存键
     */
    public String generateCacheKey(String prefix, Object... params) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(":").append(param != null ? param.toString() : "null");
        }
        return sb.toString();
    }
    
    /**
     * 检查缓存大小，如果超过限制则清理最旧的条目
     */
    private <T> void checkCacheSize(Map<String, CachedData<T>> cache) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            // 找到最旧的条目并移除
            String oldestKey = cache.entrySet().stream()
                .min((e1, e2) -> Long.compare(e1.getValue().getTimestamp(), e2.getValue().getTimestamp()))
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (oldestKey != null) {
                cache.remove(oldestKey);
                LOG.writeLog("Removed oldest cache entry: " + oldestKey);
            }
        }
    }
    
    /**
     * 清理过期的缓存条目
     */
    private void cleanupExpiredEntries() {
        try {
            int removedCount = 0;
            removedCount += removeExpiredEntries(userCache);
            removedCount += removeExpiredEntries(groupCache);
            removedCount += removeExpiredEntries(groupListCache);
            
            if (removedCount > 0) {
                LOG.writeLog("Cleanup completed, removed " + removedCount + " expired entries");
            }
        } catch (Exception e) {
            LOG.writeLog("Error during cache cleanup: " + e.getMessage());
        }
    }
    
    /**
     * 移除指定缓存中的过期条目
     */
    private <T> int removeExpiredEntries(Map<String, CachedData<T>> cache) {
        int removedCount = 0;
        java.util.Iterator<java.util.Map.Entry<String, CachedData<T>>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired()) {
                iterator.remove();
                removedCount++;
            }
        }
        return removedCount;
    }
    
    /**
     * 统计过期条目数量
     */
    private <T> long countExpiredEntries(Map<String, CachedData<T>> cache) {
        return cache.values().stream().mapToLong(data -> data.isExpired() ? 1 : 0).sum();
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int userCacheSize;
        private final int groupCacheSize;
        private final int groupListCacheSize;
        private final long expiredUserEntries;
        private final long expiredGroupEntries;
        private final long expiredGroupListEntries;
        
        public CacheStats(int userCacheSize, int groupCacheSize, int groupListCacheSize,
                         long expiredUserEntries, long expiredGroupEntries, long expiredGroupListEntries) {
            this.userCacheSize = userCacheSize;
            this.groupCacheSize = groupCacheSize;
            this.groupListCacheSize = groupListCacheSize;
            this.expiredUserEntries = expiredUserEntries;
            this.expiredGroupEntries = expiredGroupEntries;
            this.expiredGroupListEntries = expiredGroupListEntries;
        }
        
        public int getTotalCacheSize() {
            return userCacheSize + groupCacheSize + groupListCacheSize;
        }
        
        public long getTotalExpiredEntries() {
            return expiredUserEntries + expiredGroupEntries + expiredGroupListEntries;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{total=%d, user=%d, group=%d, groupList=%d, expired=%d}",
                getTotalCacheSize(), userCacheSize, groupCacheSize, groupListCacheSize, getTotalExpiredEntries());
        }
        
        // Getters
        public int getUserCacheSize() { return userCacheSize; }
        public int getGroupCacheSize() { return groupCacheSize; }
        public int getGroupListCacheSize() { return groupListCacheSize; }
        public long getExpiredUserEntries() { return expiredUserEntries; }
        public long getExpiredGroupEntries() { return expiredGroupEntries; }
        public long getExpiredGroupListEntries() { return expiredGroupListEntries; }
    }
    
    /**
     * 关闭缓存管理器，清理资源
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        clearAllCache();
        LOG.writeLog("CacheManager shutdown completed");
    }
}