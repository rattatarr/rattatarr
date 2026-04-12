package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rattatarr.rattatarr.models.JellyfinMediaType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "Type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JellyfinClientItemMovieResponseDTO.class, name = "Movie"),
        @JsonSubTypes.Type(value = JellyfinClientItemSeriesResponseDTO.class, name = "Series"),
        @JsonSubTypes.Type(value = JellyfinClientItemSeasonResponseDTO.class, name = "Season"),
        @JsonSubTypes.Type(value = JellyfinClientItemEpisodeResponseDTO.class, name = "Episode"),
        @JsonSubTypes.Type(value = JellyfinClientItemFolderResponseDTO.class, name = "Folder"),
        @JsonSubTypes.Type(value = JellyfinClientItemManualPlaylistsFolderResponseDTO.class, name = "ManualPlaylistsFolder")

})
public sealed interface JellyfinClientItemResponseDTO
        permits JellyfinClientItemEpisodeResponseDTO,
        JellyfinClientItemFolderResponseDTO,
        JellyfinClientItemManualPlaylistsFolderResponseDTO,
        JellyfinClientItemMovieResponseDTO,
        JellyfinClientItemSeasonResponseDTO,
        JellyfinClientItemSeriesResponseDTO {

    @JsonProperty("Type")
    JellyfinMediaType mediaType();

    @JsonProperty("Id")
    String id();

    @JsonProperty("Name")
    String name();
}
