package com.codestates.mainproject.oneyearfourcut.e2e.artwork;

import com.codestates.mainproject.oneyearfourcut.domain.alarm.repository.AlarmRepository;
import com.codestates.mainproject.oneyearfourcut.domain.artwork.repository.ArtworkRepository;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.Gallery;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.GalleryStatus;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.repository.GalleryRepository;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.MemberStatus;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Role;
import com.codestates.mainproject.oneyearfourcut.domain.member.repository.MemberRepository;
import com.codestates.mainproject.oneyearfourcut.global.aws.service.AwsS3Service;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.jwt.JwtTokenizer;
import com.codestates.mainproject.oneyearfourcut.global.exception.exception.ExceptionCode;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostArtworkTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GalleryRepository galleryRepository;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired  //???????????? ??? ????????? ???????????? ????????? ?????? ?????? ??????
    private AlarmRepository alarmRepository;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @MockBean
    private AwsS3Service awsS3Service;

    private Member galleryMember;
    private Member artworkMember;
    private Gallery gallery;
    private String jwt;
    private MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            "image/png",
            "<<image.png>>".getBytes());

    @BeforeEach
    void beforeSetup() {
        //????????? ?????? ????????? ????????? ??????
        galleryMember = memberRepository.save(Member.builder()
                .nickname("gallery Writer")
                .email("gallery@gmail.com")
                .profile("/path/gallery")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .build());
        gallery = galleryRepository.save(Gallery.builder()
                .title("gallery title")
                .content("gallery content")
                .member(galleryMember)
                .status(GalleryStatus.OPEN)
                .build());

        //?????? ?????? ????????? ?????? ??????
        artworkMember = memberRepository.save(Member.builder()
                .nickname("artwork Writer")
                .email("artwork@gmail.com")
                .profile("/path/artwork")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .build());
        jwt = jwtTokenizer.testJwtGenerator(artworkMember);

    }
    @AfterEach
    void clear() {
        alarmRepository.deleteAll();
        artworkRepository.deleteAll();
        galleryRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @DisplayName("???????????? ?????? ????????? ????????????.")
    @Test
    void successPostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .file(image)
                        .param("title", "artwork title")
                        .param("content", "artwork content")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isCreated());

    }

    @DisplayName("?????? ?????? ?????? ???????????? ????????????.")
    @Test
    void noTitlePostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .file(image)
                        .param("title", "")
                        .param("content", "artwork content")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isBadRequest());
    }

    @DisplayName("????????? ???????????? ?????? ???????????? ????????????.")
    @Test
    void blankTitlePostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .file(image)
                        .param("title", " ")
                        .param("content", "artwork content")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isBadRequest());
    }

    @DisplayName("?????? ?????? ?????? ???????????? ????????????.")
    @Test
    void noContentPostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .file(image)
                        .param("title", "artwork title")
                        .param("content", "")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isBadRequest());
    }


    @DisplayName("?????? ???????????? ?????? ???????????? ????????????.")
    @Test
    void blankContentPostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .file(image)
                        .param("title", "artwork title")
                        .param("content", " ")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isBadRequest());
    }


    @DisplayName("????????? ?????? ?????? ???????????? ????????????.")
    @Test
    void noImagePostTest() throws Exception {
        //given
        //s3 ????????? ?????? Mock ??????
        given(awsS3Service.uploadFile(any())).willReturn("/savedPath");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/galleries/{gallery-id}/artworks", gallery.getGalleryId())
                        .param("title", "artwork title")
                        .param("content", "artwork content")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(jsonPath("$.status").value(ExceptionCode.IMAGE_NOT_FOUND_FROM_REQUEST.getStatus()))
                .andExpect(jsonPath("$.exception").value(ExceptionCode.IMAGE_NOT_FOUND_FROM_REQUEST.name()));
    }
}
