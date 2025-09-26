package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.Faq;
import com.example.samuraitravel.service.FaqService;

@Controller
public class FaqController {

	private static final int PAGE_SIZE = 5; // 1ページ5件
	private final FaqService faqService; // コンストラクタインジェクション

	public FaqController(FaqService faqService) {
		this.faqService = faqService;
	}

	/**
	 * FAQ一覧（検索 & ページング対応）
	 * GET /faqs?keyword=...&page=0
	 */
	@GetMapping("/faqs")
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) {
		// 空白トリム
		String trimmedKeyword = (keyword == null) ? null : keyword.trim();
		boolean searched = (trimmedKeyword != null && !trimmedKeyword.isEmpty());

		PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
		Page<Faq> faqs = searched
				? faqService.findAllFaqs(trimmedKeyword, pageable)
				: faqService.getAllFaqs(pageable);

		// ここでaddAttributeする値を間違えないでください！
		model.addAttribute("faqs", faqs);
		model.addAttribute("keyword", trimmedKeyword); // ← ここで「trimmedKeyword」
		model.addAttribute("searched", searched);

		return "user/faq";
	}
}
