package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientUserResponseDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NullMarked
public class JellyfinService {
    private static final Logger logger = LoggerFactory.getLogger(JellyfinService.class);

    private final JellyfinClient jellyfinClient;
    private final ProfilesService profilesService;

    public JellyfinService(JellyfinClient jellyfinClient, ProfilesService profilesService) {
        this.jellyfinClient = jellyfinClient;
        this.profilesService = profilesService;
    }

    public boolean testConnection() {
        return jellyfinClient.testConnection();
    }

    private Profile mapJellyfinUserToProfile(JellyfinClientUserResponseDTO userDTO) {
        return new Profile(
                userDTO.name(),
                userDTO.id() // jellyfinId
        );
    }

    public List<Profile> syncJellyfinUsersWithRattatarrProfiles() {
        logger.info("Starting synchronization of Jellyfin users with Rattatarr profiles");

        List<Profile> profiles = jellyfinClient.getUsers().stream()
                .map(this::mapJellyfinUserToProfile)
                .toList();

        List<Profile> synced = profilesService.syncProfilesWithJellyfin(profiles);
        logger.info("Jellyfin sync finished. Synced {} profiles", synced.size());
        return synced;
    }
}
