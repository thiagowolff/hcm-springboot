package br.com.litecode.controller;

import br.com.litecode.util.MessageUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequestScoped
@Component
public class SystemController {
    @Autowired
    private CacheManager cacheManager;

    private Map<String, Cache> caches;

    public Map<String, Cache> getCaches() {
       if (caches == null) {
           caches = new TreeMap<>();
           for (String cacheName : cacheManager.getCacheNames()) {
               CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
               caches.put(cacheName, cache.getNativeCache());
           }
       }
       return caches;
    }

    public int getCacheNumberOfElements(String cacheName)  {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        int numberOfElements = 0;

        for (Map.Entry<Object, Object> entry :cache.getNativeCache().asMap().entrySet()) {
            numberOfElements += entry.getValue() instanceof Collection ? ((Collection) entry.getValue()).size() : 1;
        }
        return numberOfElements;
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
