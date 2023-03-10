package com.codestates.mainproject.oneyearfourcut.domain.chat.serivce;

import com.codestates.mainproject.oneyearfourcut.domain.chat.dto.ChatListResponseDto;
import com.codestates.mainproject.oneyearfourcut.domain.chat.dto.ChatPostDto;
import com.codestates.mainproject.oneyearfourcut.domain.chat.dto.ChatResponseDto;
import com.codestates.mainproject.oneyearfourcut.domain.chat.entity.Chat;
import com.codestates.mainproject.oneyearfourcut.domain.chat.repository.ChatRepository;
import com.codestates.mainproject.oneyearfourcut.domain.chatroom.dto.ChatRoomMemberInfo;
import com.codestates.mainproject.oneyearfourcut.domain.chatroom.entity.ChatRoom;
import com.codestates.mainproject.oneyearfourcut.domain.chatroom.event.ChatRoomEvent;
import com.codestates.mainproject.oneyearfourcut.domain.chatroom.event.ChatRoomEventPublisher;
import com.codestates.mainproject.oneyearfourcut.domain.chatroom.service.ChatRoomService;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.Gallery;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.service.GalleryService;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final MemberService memberService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomEventPublisher chatRoomEventPublisher;
    private final GalleryService galleryService;

    public ChatResponseDto createMessage(long chatRoomId, ChatPostDto chatPostDto) {
        /* 채팅방을 구독하고 있는 user에게 sse로 send해야 함. */

        long memberId = chatPostDto.getSenderId();
        log.info("chatRoomId : {}", chatRoomId);
        log.info("memberId : {}", memberId);

        Chat chatRequest = chatPostDto.toEntity();

        ChatRoom findChatRoom = chatRoomService.findVerifiedChatRoomWithMember(memberId, chatRoomId);
        Member findMember = memberService.findMember(memberId);

        chatRequest.setMember(findMember);
        chatRequest.setChatRoom(findChatRoom);
        Chat savedChat = chatRepository.save(chatRequest);

        log.info("savedChat.chatRoomId : {}", savedChat.getChatRoom().getChatRoomId());

        //알람 이벤트 발생
        ChatRoom chatRoom = savedChat.getChatRoom();
        Long galleryId = findMember.getOpenGallery()
                .orElse(new Gallery(0L))
                .getGalleryId();

        ChatRoomEvent chatRoomEvent = ChatRoomEvent.builder()
                .chatRoomId(chatRoomId)
                .profile(findMember.getProfile())
                .nickName(findMember.getNickname())
                .chattedAt(chatRoom.getChattedAt())
                .lastChatMessage(chatRoom.getLastChatMessage())
                .chatRoom(chatRoom)
                .galleryId(galleryId)
                .build();

        chatRoomEventPublisher.publishAlarmEvent(chatRoomEvent);


        return savedChat.toResponseDto();
    }

    // 채팅방에 해당하는 채팅 리스트 조회
    @Transactional(readOnly = true)
    public ChatListResponseDto findChatList(long memberId, long chatRoomId) {
        // 해당 채팅방이 존재하며, 해당 채팅방에 멤버가 존재하는가?
        chatRoomService.verifyChatRoomWithMember(memberId, chatRoomId);
        List<Chat> chatList = chatRepository.findAllByChatRoom_ChatRoomIdOrderByChatIdDesc(chatRoomId);

        List<ChatResponseDto> response = chatList.stream()
                .map(Chat::toResponseDto)
                .collect(Collectors.toList());
        List<ChatRoomMemberInfo> memberInfos = chatRoomService.findChatRoomMemberInfo(memberId, chatRoomId);

        return new ChatListResponseDto(memberInfos, response);
    }
}
