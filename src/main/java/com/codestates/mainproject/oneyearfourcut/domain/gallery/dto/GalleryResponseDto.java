package com.codestates.mainproject.oneyearfourcut.domain.gallery.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GalleryResponseDto {
    private Long galleryId;
    private Long memberId;
    private String profile;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long followingCount;
    private Long followerCount;
}
