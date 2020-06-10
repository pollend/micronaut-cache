package io.micronaut.cache.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.cache.SyncCache;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import org.apache.ignite.IgniteCache;

import java.util.Optional;
import java.util.function.Supplier;

public class IgniteSyncCache implements SyncCache<IgniteCache> {
    private final ConversionService<?> conversionService;
    private final IgniteCache nativeCache;

    public IgniteSyncCache(ConversionService<?> conversionService, IgniteCache nativeCache) {
        this.conversionService = conversionService;
        this.nativeCache = nativeCache;
    }


    @NonNull
    @Override
    public <T> Optional<T> get(@NonNull Object key, @NonNull Argument<T> requiredType) {
        ArgumentUtils.requireNonNull("key", key);
        Object value = nativeCache.get(key);
        if(value != null){
            return conversionService.convert(value, ConversionContext.of(requiredType));
        }
        return Optional.empty();
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Argument<T> requiredType, @NonNull Supplier<T> supplier) {
        ArgumentUtils.requireNonNull("key", key);
        Optional<T> existingValue = get(key, requiredType);
        if (existingValue.isPresent()) {
            return existingValue.get();
        } else {
            T value = supplier.get();
            put(key, value);
            return value;
        }
    }

    @NonNull
    @Override
    public <T> Optional<T> putIfAbsent(@NonNull Object key, @NonNull T value) {
        ArgumentUtils.requireNonNull("key", key);
        ArgumentUtils.requireNonNull("value", value);
        final Class<T> aClass = (Class<T>) value.getClass();
        if(nativeCache.putIfAbsent(key,value)) {
            return conversionService.convert(value, aClass);
        }
        return conversionService.convert(value, aClass);
    }

    @Override
    public void put(@NonNull Object key, @NonNull Object value) {
        ArgumentUtils.requireNonNull("key", key);
        ArgumentUtils.requireNonNull("value", value);
        nativeCache.putIfAbsent(key,value);
    }

    @Override
    public void invalidate(@NonNull Object key) {
        ArgumentUtils.requireNonNull("key", key);
        nativeCache.remove(key);

    }

    @Override
    public void invalidateAll() {
        nativeCache.clear();
    }

    @Override
    public String getName() {
        return nativeCache.getName();
    }

    @Override
    public IgniteCache getNativeCache() {
        return nativeCache;
    }
}