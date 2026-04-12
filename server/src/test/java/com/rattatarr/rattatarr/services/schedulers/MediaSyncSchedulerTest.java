package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.services.JellyfinTraversalService;
import com.rattatarr.rattatarr.services.MediaItemCreditsService;
import com.rattatarr.rattatarr.services.MediaItemMetadataService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaSyncSchedulerTest {

    @Mock
    private JellyfinTraversalService jellyfinTraversalService;
    @Mock
    private MediaItemMetadataService mediaItemMetadataService;
    @Mock
    private MediaItemCreditsService mediaItemCreditsService;
    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private MediaSyncScheduler mediaSyncScheduler;

    @BeforeEach
    void setUp() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_JELLYFIN_ENABLED), eq(true)
        )).thenReturn(true);
    }

    @Test
    void scheduledJellyfinSync_shouldExecuteFullPipeline() {
        mediaSyncScheduler.scheduledJellyfinSync();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_JELLYFIN_ENABLED), eq(true));
        verify(jellyfinTraversalService).pipelineTraverseSyncMedia();
        verify(mediaItemMetadataService).refreshAllMetadata(false);
        verify(mediaItemCreditsService).updateAllMediaItemCredits(false);
    }

    @Test
    void scheduledJellyfinSync_shouldSkipWhenDisabled() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_JELLYFIN_ENABLED), eq(true)
        )).thenReturn(false);

        mediaSyncScheduler.scheduledJellyfinSync();

        verify(jellyfinTraversalService, never()).pipelineTraverseSyncMedia();
        verify(mediaItemMetadataService, never()).refreshAllMetadata(any());
        verify(mediaItemCreditsService, never()).updateAllMediaItemCredits(any());
    }

    @Test
    void scheduledJellyfinSync_shouldCallServicesInCorrectOrder() {
        mediaSyncScheduler.scheduledJellyfinSync();

        var inOrder = inOrder(jellyfinTraversalService, mediaItemMetadataService, mediaItemCreditsService);
        inOrder.verify(jellyfinTraversalService).pipelineTraverseSyncMedia();
        inOrder.verify(mediaItemMetadataService).refreshAllMetadata(false);
        inOrder.verify(mediaItemCreditsService).updateAllMediaItemCredits(false);
    }

    @Test
    void scheduledJellyfinSync_shouldHandleTraversalError() {
        doThrow(new RuntimeException("Traversal failed")).when(jellyfinTraversalService).pipelineTraverseSyncMedia();

        // Should not propagate — caught internally
        mediaSyncScheduler.scheduledJellyfinSync();

        verify(jellyfinTraversalService).pipelineTraverseSyncMedia();
        verify(mediaItemMetadataService, never()).refreshAllMetadata(any());
    }

    @Test
    void scheduledJellyfinSync_shouldHandleMetadataRefreshError() {
        doThrow(new RuntimeException("Metadata refresh failed")).when(mediaItemMetadataService).refreshAllMetadata(false);

        mediaSyncScheduler.scheduledJellyfinSync();

        verify(jellyfinTraversalService).pipelineTraverseSyncMedia();
        verify(mediaItemMetadataService).refreshAllMetadata(false);
    }

    @Test
    void scheduledJellyfinSync_shouldPassCorrectParametersToServices() {
        mediaSyncScheduler.scheduledJellyfinSync();

        verify(mediaItemMetadataService).refreshAllMetadata(false);
        verify(mediaItemCreditsService).updateAllMediaItemCredits(false);
    }

    @Test
    void scheduledJellyfinSync_shouldCompleteSuccessfullyWithAllSteps() {
        mediaSyncScheduler.scheduledJellyfinSync();

        verify(jellyfinTraversalService).pipelineTraverseSyncMedia();
        verify(mediaItemMetadataService).refreshAllMetadata(false);
        verify(mediaItemCreditsService).updateAllMediaItemCredits(false);
    }
}
