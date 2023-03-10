package com.codestates.mainproject.oneyearfourcut.domain.follow.service;

import com.codestates.mainproject.oneyearfourcut.domain.alarm.event.AlarmEventPublisher;
import com.codestates.mainproject.oneyearfourcut.domain.follow.dto.FollowerResponseDto;
import com.codestates.mainproject.oneyearfourcut.domain.follow.dto.FollowingResponseDto;
import com.codestates.mainproject.oneyearfourcut.domain.follow.entity.Follow;
import com.codestates.mainproject.oneyearfourcut.domain.follow.repository.FollowRepository;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.Gallery;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.GalleryStatus;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.service.GalleryService;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.service.MemberService;
import com.codestates.mainproject.oneyearfourcut.global.exception.exception.BusinessLogicException;
import com.codestates.mainproject.oneyearfourcut.global.exception.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberService memberService;
    private final GalleryService galleryService;
    private final AlarmEventPublisher alarmEventPublisher;

    @Transactional
    public Follow createFollow(Long loginMemberId, Long targetGalleryId) {
        Member loginMember = memberService.findMember(loginMemberId);
        galleryService.verifiedGalleryExist(targetGalleryId);
        Gallery targetGallery = galleryService.findGallery(targetGalleryId);
        Long galleryOwnerMemberId = targetGallery.getMember().getMemberId();

        if(Objects.equals( loginMemberId, galleryOwnerMemberId )){
            throw new BusinessLogicException(ExceptionCode.CANNOT_FOLLOW_OWN_GALLERY);
        }
        if(followRepository.existsByMember_MemberIdAndFollowMemberId(loginMemberId, galleryOwnerMemberId )){
            throw new BusinessLogicException(ExceptionCode.ALREADY_FOLLOWED);
        }

        Boolean isFollowingMeCheck = followRepository.existsByMember_MemberIdAndFollowMemberId(galleryOwnerMemberId, loginMemberId) ;
        //????????? , ?????????????????? ?????? ????????? ?????????? true or false

        if(isFollowingMeCheck){
            try {Follow follow = Follow.builder()
                    .member(loginMember)
                    .followMemberId(galleryOwnerMemberId)
                    .gallery(targetGallery)
                    .isFollowTogetherCheck(true)
                    .build();
            alarmEventPublisher.publishAlarmEvent(followRepository.save(follow).toAlarmEvent(galleryOwnerMemberId));
            //????????????
            return follow;
            }
            finally{
                Follow foundOppositeFollow = findVerifiedFollowByMemberAndGallery(
                        targetGallery.getMember(), galleryService.findLoginGallery(loginMemberId));
                //??? ???????????? follow ?????? ????????? follow Id ??? following me check??? true ????????? ?????? (??????)
                foundOppositeFollow.changeFollowTogetherCheck(true);
            }
        }
        else{
            Follow follow = Follow.builder()
                    .member(loginMember)
                    .followMemberId(galleryOwnerMemberId)
                    .gallery(targetGallery)
                    .isFollowTogetherCheck(false)
                    .build();
            alarmEventPublisher.publishAlarmEvent(followRepository.save(follow).toAlarmEvent(galleryOwnerMemberId));
            // ?????? ??????
            return follow;
        }

    }

    @Transactional
    public Boolean unfollow(Long myMemberId, Long otherGalleryId) {
        Member myMember = memberService.findMember(myMemberId);
        Gallery otherGallery = galleryService.findGallery(otherGalleryId);
        Follow foundMyFollowing = findVerifiedFollowByMemberAndGallery(myMember, otherGallery);

        // ?????????????????? ??????????????? ?????? ???????????? or ?????????????????? : (other) true, true (me) ->  (other) false , deleted (me)
        if(foundMyFollowing.getIsFollowTogetherCheck()){
            Follow foundOppositeFollow = findVerifiedFollowByMemberAndGallery(
                    memberService.findMember( foundMyFollowing.getFollowMemberId() ) ,
                    galleryService.findLoginGallery(myMemberId) );
            // ?????? ????????? ?????? ?????? ??????
            foundOppositeFollow.changeFollowTogetherCheck(false);
        }
        followRepository.delete(foundMyFollowing);
        return true;
    }

    @Transactional
    public Boolean deleteFollower(Long myMemberId, Long followId) {
        Member myMember = memberService.findMember(myMemberId);
        Gallery myGallery = galleryService.findLoginGallery(myMemberId);
        Follow foundFollower = findVerifiedFollow(followId); //?????????????????? follow id

        if(!Objects.equals(foundFollower.getGallery().getGalleryId(), myGallery.getGalleryId())){
            throw new BusinessLogicException(ExceptionCode.FOLLOW_NOT_FOUND_FROM_GALLERY);
        }
        // ?????? ????????? ?????? ????????? (?????? ????????????) : (other) true, true (me) -> (other) deleted, false (me)
        if(foundFollower.getIsFollowTogetherCheck()){
            Follow foundMyFollow =
                    findVerifiedFollowByFollowMemberIdAndMember(foundFollower.getMember().getMemberId(), myMember);
            // ?????? ????????? ?????? ?????? ??????
            foundMyFollow.changeFollowTogetherCheck(false);
        }
        followRepository.delete(foundFollower);
        return true;
    }

    @Transactional(readOnly = true) //?????? ???????????? ????????? ???????????? ????????????.
    public Object getFollowingListByGalleryId(Long galleryId) {
        Long galleryOwnerMemberId = galleryService.findGallery(galleryId).getMember().getMemberId();
        List<Follow> followingList = followRepository.findAllByMember_MemberIdAndGallery_StatusOrderByFollowIdDesc(galleryOwnerMemberId, GalleryStatus.OPEN);
        return FollowingResponseDto.toFollowingResponseDtoList(followingList);
    }

    @Transactional(readOnly = true) //?????? ???????????? ????????? ???????????? ????????????.
    public Object getFollowerListByGalleryId(Long galleryId) {
        Long galleryOwnerMemberId = galleryService.findGallery(galleryId).getMember().getMemberId();
        List<Follow> followerList = followRepository.findAllFollowerListByMemberId(galleryOwnerMemberId); //member Active ??? ??????
        return FollowerResponseDto.toFollowerResponseDtoList(followerList);
    }

    //---????????????---//
    private Follow findVerifiedFollowByFollowMemberIdAndMember(Long memberId, Member myMember) {
        return followRepository.findByFollowMemberIdAndMember(memberId, myMember).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.FOLLOW_NOT_FOUND_FROM_GALLERY));
    }
    private Follow findVerifiedFollow(Long followId) {
        return followRepository.findById(followId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.FOLLOW_NOT_FOUND));
    }
    private Follow findVerifiedFollowByMemberAndGallery(Member member, Gallery gallery) {
        return followRepository.findByMemberAndGallery(member, gallery).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.FOLLOW_NOT_FOUND_FROM_GALLERY));
    }

}

