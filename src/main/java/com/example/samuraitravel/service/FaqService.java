package com.example.samuraitravel.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.Faq;
import com.example.samuraitravel.repository.FaqRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    /**
     * 全FAQをページングで取得（1ページ5件などはController側のPageableで指定）
     */
    public Page<Faq> getAllFaqs(Pageable pageable) {
        return faqRepository.findAll(pageable);
    }

    /**
     * キーワードで部分一致検索（question LIKE %keyword%）
     * keyword が null/空文字の場合は全件を返す
     */
    public Page<Faq> findAllFaqs(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getAllFaqs(pageable);
        }
        String like = "%" + keyword.trim() + "%";
        return faqRepository.findByQuestionContaining(keyword, pageable);
    }
}
