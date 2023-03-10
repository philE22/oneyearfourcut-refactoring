package com.codestates.mainproject.oneyearfourcut.domain.comment.repository;

import com.codestates.mainproject.oneyearfourcut.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, PagingAndSortingRepository<Comment,Long> {

    Page<Comment> findAllByGallery_GalleryIdOrderByCommentIdDesc
            (Long galleryId, Pageable pageable);
    Page<Comment> findAllByArtwork_ArtworkIdOrderByCommentIdDesc
            (Long galleryId, Pageable pageable);

    List<Comment> findAllByArtwork_ArtworkId(Long ArtworkId);

}
