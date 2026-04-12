package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.SettingValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettingWrapperTest {

    @Test
    void fromMap_shouldCreateWrapperFromSingleEntry() {
        // Given
        SettingValue settingValue = new SettingValue("testValue", "testDescription");
        Map<String, SettingValue> settingMap = Map.of("testKey", settingValue);

        // When
        SettingWrapper wrapper = SettingWrapper.fromMap(settingMap);

        // Then
        assertNotNull(wrapper);
        assertNotNull(wrapper.setting());
        assertEquals("testKey", wrapper.setting().key());
        assertEquals("testValue", wrapper.setting().value());
        assertEquals("testDescription", wrapper.setting().description());
    }
}
