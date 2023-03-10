package com.codestates.mainproject.oneyearfourcut.domain.Like.controller;

import com.codestates.mainproject.oneyearfourcut.domain.Like.service.ArtworkLikeService;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class ArtworkLikeController {

    private final ArtworkLikeService artworkLikeService;

    // 좋아요 등록(생성)과 좋아요 취소(수정)가 동시에 이루어지기 때문에 Post -> Put으로 변경했습니다.
    @PutMapping("/galleries/{gallery-id}/artworks/{artwork-id}/likes")
    public ResponseEntity<?> putArtworkLike(@LoginMember Long memberId,
                                            @Positive @PathVariable("gallery-id") Long galleryId,
                                            @Positive @PathVariable("artwork-id") Long artworkId) {

        artworkLikeService.updateArtworkLike(memberId, galleryId, artworkId, LocalDateTime.now());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
