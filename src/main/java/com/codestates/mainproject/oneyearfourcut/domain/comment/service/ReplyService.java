package com.codestates.mainproject.oneyearfourcut.domain.comment.service;

import com.codestates.mainproject.oneyearfourcut.domain.alarm.event.AlarmEventPublisher;
import com.codestates.mainproject.oneyearfourcut.domain.comment.dto.CommentRequestDto;
import com.codestates.mainproject.oneyearfourcut.domain.comment.dto.ReplyResDto;
import com.codestates.mainproject.oneyearfourcut.domain.comment.entity.Comment;
import com.codestates.mainproject.oneyearfourcut.domain.comment.entity.Reply;
import com.codestates.mainproject.oneyearfourcut.domain.comment.repository.ReplyRepository;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.service.MemberService;
import com.codestates.mainproject.oneyearfourcut.global.exception.exception.BusinessLogicException;
import com.codestates.mainproject.oneyearfourcut.global.exception.exception.ExceptionCode;
import com.codestates.mainproject.oneyearfourcut.global.page.ReplyListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final CommentService commentService;
    private final AlarmEventPublisher alarmEventPublisher;

    //Create
    @Transactional
    public ReplyListResponseDto<Object> createReply(CommentRequestDto commentRequestDto, Long commentId, Long memberId) {
        Comment findComment = commentService.findComment(commentId);
        Reply reply = Reply.builder()
                .content(commentRequestDto.getContent())
                .comment(findComment)
                .member(new Member(memberId))
                .build();
        Reply savedReply = replyRepository.save(reply);

        //?????? ??????
        //?????? ?????? ????????? ?????????? ????????? ??????????
        Long receiverId = savedReply.getComment().getMember().getMemberId();
        alarmEventPublisher.publishAlarmEvent(savedReply.toAlarmEvent(receiverId));

        return new ReplyListResponseDto<>(commentId, reply.toReplyResponseDto());
    }

    //Read
    @Transactional(readOnly = true)
    public ReplyListResponseDto<Object> getReplyList(Long commentId) {
        commentService.findComment(commentId);

        // commentId != null ????????? ????????? ????????? ???????????? ?????? findComment?????? commentId??? ???????????? ????????? ????????? ??????????????????.
        List<Reply> replyList = replyRepository.findAllByComment_CommentIdOrderByReplyIdDesc(commentId);

        if (replyList.isEmpty()) { // ??? ????????? ????????? ??? ????????? ???????????? ??????????????? ??????????????????..!
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "???????????? ?????? ????????????.");
        }

        List<ReplyResDto> result = ReplyResDto.toReplyResponseDtoList(replyList);
        return new ReplyListResponseDto<>(commentId, result);
    }

    //Update
    @Transactional
    public ReplyListResponseDto<Object> modifyReply(Long commentId, Long replyId, CommentRequestDto commentRequestDto, Long memberId) {
        Reply foundReply = findReply(replyId);
        // ????????? ????????? ?????? ?????? ????????? ????????? ???????????? ????????? Exception ??????
        checkCommentReplyVerification(commentId, foundReply);
        if (!Objects.equals(foundReply.getMember().getMemberId(), memberId)) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }
        //--????????????
        Reply requestReply = commentRequestDto.toReplyEntity();
        Optional.ofNullable(requestReply.getContent())
                .ifPresent(foundReply::changeContent);
        return new ReplyListResponseDto<>(commentId, foundReply.toReplyResponseDto());
    }

    //Delete
    @Transactional
    public void deleteReply(Long commentId, Long replyId, Long memberId) {
        Reply foundReply = findReply(replyId);
        checkCommentReplyVerification(commentId, foundReply);
        // ????????? ????????? ?????? ?????? ????????? ????????? ???????????? ????????? ????????? ????????? ?????? ???????????? Exception ??????
        if (!Objects.equals(foundReply.getMember().getMemberId(), memberId)
                && !Objects.equals(foundReply.getComment().getGallery().getMember().getMemberId(), memberId)) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }
        //--????????????
        replyRepository.delete(foundReply);
    }

    @Transactional(readOnly = true)
    private void checkCommentReplyVerification(Long commentId, Reply foundReply) {
        if (!Objects.equals(commentId, foundReply.getComment().getCommentId())) {
            throw new BusinessLogicException(ExceptionCode.REPLY_NOT_FOUND_FROM_COMMENT);
        }
    }

    @Transactional(readOnly = true)
    public Reply findReply(Long replyId) {
        Optional<Reply> reply = replyRepository.findById(replyId);
        Reply foundReply = reply.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));
        return foundReply;
    }
}
