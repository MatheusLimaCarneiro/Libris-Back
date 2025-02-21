package prati.projeto.redeSocial.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import prati.projeto.redeSocial.modal.entity.ComentarioForum;


public interface ComentarioForumRepository extends JpaRepository<ComentarioForum, Integer> {
    Page<ComentarioForum> findByPostForumId(Integer comentarioId, Pageable pageable);
}