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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class getOneYearFourCutTest {
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
    private Member artworkMember;
    private Gallery savedGallery;

    @BeforeEach
    void setup() {
        //?????? ??????
        artworkMember = memberRepository.save(Member.builder()
                .nickname("test2")
                .email("test2@gmail.com")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .profile("/path2")
                .build());
        galleryMember = memberRepository.save(Member.builder()
                .nickname("test3")
                .email("test3@gmail.com")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .profile("/path1")
                .build());

        //????????? ??????
        savedGallery = galleryRepository.save(Gallery.builder()
                .member(galleryMember)
                .title("title")
                .content("content")
                .status(GalleryStatus.OPEN)
                .build());
    }
    @AfterEach
    void clear() {
        artworkLikeRepository.deleteAll();
        artworkRepository.deleteAll();
        galleryRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @DisplayName("???????????? ?????? ????????? 4?????? ????????? ????????????.")
    @Test
    void get4CutTest() throws Exception {
        /**
         * ??? ?????? ?????? ???????????? ?????? ????????? 4?????? ????????? ?????? ????????? ????????????
         * ????????? ?????? ????????? ?????? ?????? ????????? ??????
         *
         * ????????? ?????? ?????? ?????? : 0 > 1 > 4 = 3 = 2 > 5
         * ???????????? 0, 1, 4, 3 ????????? ?????? ??????
         */
        //given
        //?????? ??????
        ArrayList<Artwork> artworkList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Artwork artwork = Artwork.builder()
                    .title("artwork title" + i)
                    .content("artwork content" + i)
                    .build();
            artwork.setImagePath("/path/test" + i);
            artwork.setMember(artworkMember);
            artwork.setGallery(savedGallery);

            artworkList.add(artwork);
        }
        List<Artwork> savedArtworkList = artworkRepository.saveAll(artworkList);

        //????????? ????????? ?????? 4??? ??????
        ArrayList<Member> likeMemberList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            likeMemberList.add(memberRepository.save(Member.builder()
                    .nickname("like" + i)
                    .email("like" + i + "@gmail.com")
                    .role(Role.USER)
                    .status(MemberStatus.ACTIVE)
                    .profile("/path")
                    .build()));
        }

        //????????? ?????? ????????? ??????
        for (int i = 0; i < 4; i++) {
            artworkLikeRepository.save(
                    ArtworkLike.builder()
                            .artwork(savedArtworkList.get(0))
                            .member(likeMemberList.get(i))
                            .build()
            );
        }
        for (int i = 0; i < 3; i++) {
            artworkLikeRepository.save(
                    ArtworkLike.builder()
                            .artwork(savedArtworkList.get(1))
                            .member(likeMemberList.get(i))
                            .build()
            );
        }
        for (int i = 0; i < 2; i++) {
            artworkLikeRepository.save(
                    ArtworkLike.builder()
                            .artwork(savedArtworkList.get(4))
                            .member(likeMemberList.get(i))
                            .build()
            );
        }
        for (int i = 0; i < 2; i++) {
            artworkLikeRepository.save(
                    ArtworkLike.builder()
                            .artwork(savedArtworkList.get(3))
                            .member(likeMemberList.get(i))
                            .build()
            );
        }
        for (int i = 0; i < 2; i++) {
            artworkLikeRepository.save(
                    ArtworkLike.builder()
                            .artwork(savedArtworkList.get(2))
                            .member(likeMemberList.get(i))
                            .build()
            );
        }

        //when
        ResultActions actions = mockMvc.perform(
                get("/galleries/{gallery-id}/artworks/like", savedGallery.getGalleryId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.[0].artworkId").value(savedArtworkList.get(0).getArtworkId()))
                .andExpect(jsonPath("$.[1].artworkId").value(savedArtworkList.get(1).getArtworkId()))
                .andExpect(jsonPath("$.[2].artworkId").value(savedArtworkList.get(4).getArtworkId()))
                .andExpect(jsonPath("$.[3].artworkId").value(savedArtworkList.get(3).getArtworkId()));
    }

    @DisplayName("????????? ????????? ??? ????????? ?????????")
    @Test
    void name() throws Exception {
        //when
        ResultActions actions = mockMvc.perform(
                get("/galleries/{gallery-id}/artworks/like", savedGallery.getGalleryId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
