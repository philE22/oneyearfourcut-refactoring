package com.codestates.mainproject.oneyearfourcut.e2e.artwork;

import com.codestates.mainproject.oneyearfourcut.domain.Like.entity.ArtworkLike;
import com.codestates.mainproject.oneyearfourcut.domain.Like.repository.ArtworkLikeRepository;
import com.codestates.mainproject.oneyearfourcut.domain.artwork.entity.Artwork;
import com.codestates.mainproject.oneyearfourcut.domain.artwork.repository.ArtworkRepository;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.Gallery;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.entity.GalleryStatus;
import com.codestates.mainproject.oneyearfourcut.domain.gallery.repository.GalleryRepository;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Member;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.MemberStatus;
import com.codestates.mainproject.oneyearfourcut.domain.member.entity.Role;
import com.codestates.mainproject.oneyearfourcut.domain.member.repository.MemberRepository;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.jwt.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GetArtworkTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GalleryRepository galleryRepository;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private ArtworkLikeRepository artworkLikeRepository;
    @Autowired
    private JwtTokenizer jwtTokenizer;

    private Member galleryMember;
    private Gallery savedGallery;
    private Artwork savedArtwork;

    @BeforeEach
    void setup() {
        //?????? ??????
        galleryMember = memberRepository.save(Member.builder()
                .nickname("test3")
                .email("test3@gmail.com")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .profile("/path1")
                .build());
        Member artworkMember = memberRepository.save(Member.builder()
                .nickname("test2")
                .email("test2@gmail.com")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .profile("/path2")
                .build());

        //????????? ??????
        savedGallery = galleryRepository.save(Gallery.builder()
                .member(galleryMember)
                .title("title")
                .content("content")
                .status(GalleryStatus.OPEN)
                .build());

        //?????? ??????
        Artwork artwork = Artwork.builder()
                .title("artwork title")
                .content("artwork content")
                .build();
        artwork.setImagePath("/path/test1");
        artwork.setMember(artworkMember);
        artwork.setGallery(savedGallery);
        savedArtwork = artworkRepository.save(artwork);

        //????????? ????????? ????????? ????????? ??????
        artworkLikeRepository.save(
                ArtworkLike.builder()
                        .artwork(savedArtwork)
                        .member(galleryMember)
                        .build()
        );
    }
    @AfterEach
    void clear() {
        artworkLikeRepository.deleteAll();
        artworkRepository.deleteAll();
        galleryRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @DisplayName("????????? ?????? ??????: ????????? ????????? ?????? ??????")
    @Test
    void loginGetTest() throws Exception {
        //given
        //jwt ??????
        String jwt = jwtTokenizer.testJwtGenerator(galleryMember);

        //when
        ResultActions actions = mockMvc.perform(
                get("/galleries/{gallery-id}/artworks/{artwork-id}",
                        savedGallery.getGalleryId(),
                        savedArtwork.getArtworkId())
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedArtwork.getTitle()))
                .andExpect(jsonPath("$.content").value(savedArtwork.getContent()))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @DisplayName("???????????? ??????: ????????? ?????? false??? ??????")
    @Test
    void anonymousGetTest() throws Exception {
        //given

        //when
        ResultActions actions = mockMvc.perform(
                get("/galleries/{gallery-id}/artworks/{artwork-id}",
                        savedGallery.getGalleryId(),
                        savedArtwork.getArtworkId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedArtwork.getTitle()))
                .andExpect(jsonPath("$.content").value(savedArtwork.getContent()))
                .andExpect(jsonPath("$.liked").value(false));
    }
}
