package com.example.samuraitravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    /**
     * 質問文に部分一致（LIKE）するデータをページング取得
     * 例）repository.findByQuestionLike("%予約%", pageable)
     */
    Page<Faq> findByQuestionContaining(String keyword, Pageable pageable);

    // ※ findAll(Pageable pageable) は JpaRepository 既定メソッドで利用可能
}
