package com.codestates.mainproject.oneyearfourcut.domain.follow.dto;

import com.codestates.mainproject.oneyearfourcut.domain.follow.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowingResponseDto {
    public Long followId;

    public Long galleryId;
    public String galleryTitle;
    public String galleryMemberNickname;

    public String profile;
    public Boolean isFollowTogetherCheck;
    // 맞팔 여부

    public static List<FollowingResponseDto> toFollowingResponseDtoList(List<Follow> followList){
        return followList == null ? Collections.emptyList() : followList
                .stream()
                .map(Follow::toFollowingResponseDto)
                .collect(Collectors.toList());
    }
}
