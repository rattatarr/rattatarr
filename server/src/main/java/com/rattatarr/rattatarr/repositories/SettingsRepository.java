package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.Setting;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends BaseRepository<Setting> {
    Optional<Setting> findByKey(String key);
}
