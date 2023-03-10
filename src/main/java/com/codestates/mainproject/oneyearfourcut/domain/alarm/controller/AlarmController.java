package com.codestates.mainproject.oneyearfourcut.domain.alarm.controller;

import com.codestates.mainproject.oneyearfourcut.domain.alarm.dto.AlarmReadCheckResponseDto;
import com.codestates.mainproject.oneyearfourcut.domain.alarm.service.AlarmService;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.LoginMember;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members/me/alarms")
@Validated
@Slf4j
@AllArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<Object> getAlarmListFiltered(@RequestParam String filter, @RequestParam int page,
                                                       @LoginMember Long memberId) {
        return new ResponseEntity<>(alarmService.getAlarmPagesByFilter(filter, page, memberId), HttpStatus.OK);
    }

    @GetMapping("/read")
    public ResponseEntity<Object> checkReadAlarm(@LoginMember Long memberId){
        Boolean alarmExist = alarmService.checkReadAlarm(memberId);
        AlarmReadCheckResponseDto dto;
        if (alarmExist) {
            dto = AlarmReadCheckResponseDto.builder()
                    .readAlarmExist(Boolean.TRUE)
                    .message("읽지않은 알림이 존재합니다.")
                    .build();
        } else{
            dto = AlarmReadCheckResponseDto.builder()
                    .readAlarmExist(Boolean.FALSE)
                    .message("현재 알림이 없습니다.")
                    .build();
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

}
