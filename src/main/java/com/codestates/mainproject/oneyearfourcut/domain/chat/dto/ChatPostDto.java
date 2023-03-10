package com.codestates.mainproject.oneyearfourcut.domain.chat.dto;

import com.codestates.mainproject.oneyearfourcut.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class ChatPostDto {

    private long senderId;
    // 메세지
    private String message;

    @Builder
    public ChatPostDto(long senderId, String message) {
        this.message = message;
    }

    public Chat toEntity() {
        return Chat.builder()
                .message(this.message)
                .build();
    }

}
