package br.com.litecode.controller;

import br.com.litecode.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestScoped
@Component
public class SystemController {
    @Autowired
    private CacheManager cacheManager;

    private  Map<String, Map<Object, Object>> caches;

    public Map<String, Map<Object, Object>> getCaches() {
       if (caches == null) {
           caches = new HashMap<>();
           for (String cacheName : cacheManager.getCacheNames()) {
               CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
               caches.put(cacheName, cache.getNativeCache().asMap());
           }
       }
        return caches;
    }

    public boolean isNewVersion() {
        LocalDate versionDate = Faces.getApplicationAttribute("versionDate");
        return LocalDate.now().isBefore(versionDate.plusDays(2));
    }

    public void reloadMessagesResourceBundle() {
        MessageUtil.reloadMessagesResourceBundle();
    }

    public void invalidateCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            log.info("Cache {} ({}) invalidated", cacheName, cache.getNativeCache().estimatedSize());
            cache.clear();
        }
    }

    public long getCacheSize() {
        long cacheSize = 0;
        List<String> cacheSizeInfo = new ArrayList<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            cacheSize += cache.getNativeCache().estimatedSize();
            cacheSizeInfo.add(cacheName + ": " + cache.getNativeCache().estimatedSize());
        }

        log.info(String.join(", ", cacheSizeInfo));
        return cacheSize;
    }
}
