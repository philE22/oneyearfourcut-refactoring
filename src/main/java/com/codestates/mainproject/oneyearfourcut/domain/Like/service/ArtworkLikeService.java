package com.codestates.mainproject.oneyearfourcut.domain.Like.service;

import com.codestates.mainproject.oneyearfourcut.domain.Like.entity.ArtworkLike;
import com.codestates.mainproject.oneyearfourcut.domain.Like.entity.LikeStatus;
import com.codestates.mainproject.oneyearfourcut.domain.Like.repository.ArtworkLikeRepository;
import com.codestates.mainproject.oneyearfourcut.domain.alarm.event.AlarmEventPublisher;
import com.codestates.mainproject.oneyearfourcut.domain.artwork.entity.Artwork;
import com.codestates.mainproject.oneyearfourcut.domain.artwork.service.ArtworkService;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtworkLikeService {

    private final ArtworkLikeRepository artworkLikeRepository;
    private final ArtworkService artworkService;
    private final MemberService memberService;
    private final AlarmEventPublisher alarmEventPublisher;

    public void updateArtworkLike(long memberId, long galleryId, long artworkId, LocalDateTime now) {
        Member findMember = memberService.findMember(memberId);
        Artwork findArtwork = artworkService.findGalleryVerifiedArtwork(galleryId, artworkId);

        Optional<ArtworkLike> likeOptional = artworkLikeRepository.findByMemberAndArtwork(findMember, findArtwork);
        likeOptional.ifPresentOrElse(
                like -> {
                    if (like.isLike()) {
                        like.setStatus(LikeStatus.CANCEL);
                        return;
                    }

                    like.setStatus(LikeStatus.LIKE);
                    //취소하고 30초안에 좋아요 누르면 알림이 또 안 가도록 구현
                    if (Duration.between(like.getModifiedAt(), now).getSeconds() >= 30) {
                        //전시관 주인 알람 생성
                        Long galleryReceiverId = findArtwork.getGallery().getMember().getMemberId();
                        alarmEventPublisher.publishAlarmEvent(like.toAlarmEvent(galleryReceiverId));
                        //작품 주인 알람 생성
                        Long artworkReceiverId = findArtwork.getMember().getMemberId();
                        if (artworkReceiverId != galleryReceiverId) {   //두 알람의 주인이 같으면 중복으로 보내지 않음
                            alarmEventPublisher.publishAlarmEvent(like.toAlarmEvent(artworkReceiverId));
                        }
                    }
                },
                () -> { //좋아요가 처음 눌릴 때
                    ArtworkLike savedArtworkLike =
                            artworkLikeRepository.save(ArtworkLike.builder()
                            .member(findMember)
                            .artwork(findArtwork)
                            .build());

                    //전시관 주인 알람 생성
                    Long galleryReceiverId = findArtwork.getGallery().getMember().getMemberId();
                    alarmEventPublisher.publishAlarmEvent(savedArtworkLike.toAlarmEvent(galleryReceiverId));
                    //작품 주인 알람 생성
                    Long artworkReceiverId = findArtwork.getMember().getMemberId();
                    if (artworkReceiverId != galleryReceiverId) {   //두 알람의 주인이 같으면 중복으로 보내지 않음
                        alarmEventPublisher.publishAlarmEvent(savedArtworkLike.toAlarmEvent(artworkReceiverId));
                    }
                });
    }
}
