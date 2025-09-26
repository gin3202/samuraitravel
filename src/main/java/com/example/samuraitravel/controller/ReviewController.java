package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReviewService;
import com.example.samuraitravel.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Controller
@RequestMapping("/houses/{houseId}/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final HouseService houseService;
	private final UserService userService;

	public ReviewController(ReviewService reviewService,
			HouseService houseService,
			UserService userService) {
		this.reviewService = reviewService;
		this.houseService = houseService;
		this.userService = userService;
	}

	/**
	 * index:
	 * 指定された民宿のレビューを作成日時の降順でページング取得し、民宿詳細ページへ渡して表示する。
	 * 画面は「houses/show」を再利用します。
	 */
	@GetMapping
	public String index(@PathVariable Integer houseId,
			@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
			Model model,
			RedirectAttributes ra) {
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if (optionalHouse.isEmpty()) {
			ra.addFlashAttribute("errorMessage", "民宿が存在しません。");
			return "redirect:/houses";
		}
		House house = optionalHouse.get();
		Page<Review> reviewPage = reviewService.findReviewsByHouseOrderByCreatedAtDesc(house, pageable);

		 // ↓ 「reviews/index」に変更
	    model.addAttribute("house", house);
	    model.addAttribute("reviewPage", reviewPage);
	    return "reviews/index";
	}

	/**
	 * register:
	 * レビュー投稿ページを表示。フォームを新規生成して民宿データと一緒に渡す。
	 */
	@GetMapping("/register")
	public String register(@PathVariable Integer houseId,
			Model model,
			RedirectAttributes ra) {
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if (optionalHouse.isEmpty()) {
			ra.addFlashAttribute("errorMessage", "民宿が存在しません。");
			return "redirect:/houses";
		}
		if (!model.containsAttribute("reviewRegisterForm")) {
			model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		}
		model.addAttribute("house", optionalHouse.get());
		return "reviews/register";
	}

	/**
	 * create:
	 * 新しいレビューを登録。バリデーションNG時は投稿ページを再表示。
	 */
	@PostMapping
	public String create(@PathVariable Integer houseId,
			@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
			BindingResult bindingResult,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes ra,
			Model model) {
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if (optionalHouse.isEmpty()) {
			ra.addFlashAttribute("errorMessage", "民宿が存在しません。");
			return "redirect:/houses";
		}
		House house = optionalHouse.get();

		if (bindingResult.hasErrors()) {
			model.addAttribute("house", house);
			model.addAttribute("reviewRegisterForm", reviewRegisterForm);
			return "reviews/register";
		}

		User user = userService.findUserByEmail(userDetailsImpl.getUsername());
		try {
			reviewService.createReview(house.getId(), user.getId(), reviewRegisterForm);
			ra.addFlashAttribute("message", "レビューを投稿しました。");
		} catch (IllegalStateException e) {
			// 既に投稿済みなど
			ra.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/houses/" + houseId + "/reviews/register";
		} catch (EntityNotFoundException e) {
			ra.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/houses";
		}
		return "redirect:/houses/" + houseId;
	}

	/**
	 * edit:
	 * レビュー編集ページを表示。対象レビュー・民宿を読み込み、フォームへ詰めて表示。
	 */
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable Integer houseId,
			@PathVariable Integer reviewId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model,
			RedirectAttributes ra) {
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if (optionalHouse.isEmpty()) {
			ra.addFlashAttribute("errorMessage", "民宿が存在しません。");
			return "redirect:/houses";
		}
		Review review = reviewService.findReviewById(reviewId);

		// 所有者チェック（必要に応じて）
		User currentUser = userService.findUserByEmail(userDetailsImpl.getUsername());
		if (!review.getUser().getId().equals(currentUser.getId())) {
			ra.addFlashAttribute("errorMessage", "このレビューを編集する権限がありません。");
			return "redirect:/houses/" + houseId;
		}

		if (!model.containsAttribute("reviewEditForm")) {
			model.addAttribute("reviewEditForm",
					new ReviewEditForm(review.getScore(), review.getContent()));
		}
		model.addAttribute("house", optionalHouse.get());
		model.addAttribute("review", review);
		return "reviews/edit";
	}

	/**
	 * update:
	 * 既存レビューを更新。バリデーションNG時は編集ページを再表示。
	 */
	@PostMapping("/{reviewId}")
	public String update(@PathVariable Integer houseId,
			@PathVariable Integer reviewId,
			@ModelAttribute @Validated ReviewEditForm reviewEditForm,
			BindingResult bindingResult,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes ra,
			Model model) {
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if (optionalHouse.isEmpty()) {
			ra.addFlashAttribute("errorMessage", "民宿が存在しません。");
			return "redirect:/houses";
		}
		House house = optionalHouse.get();

		if (bindingResult.hasErrors()) {
			model.addAttribute("house", house);
			model.addAttribute("review", reviewService.findReviewById(reviewId));
			model.addAttribute("reviewEditForm", reviewEditForm);
			return "reviews/edit";
		}

		// 所有者チェック
		Review review = reviewService.findReviewById(reviewId);
		User currentUser = userService.findUserByEmail(userDetailsImpl.getUsername());
		if (!review.getUser().getId().equals(currentUser.getId())) {
			ra.addFlashAttribute("errorMessage", "このレビューを編集する権限がありません。");
			return "redirect:/houses/" + houseId;
		}

		reviewService.updateReview(reviewId, reviewEditForm);
		ra.addFlashAttribute("message", "レビューを更新しました。");
		return "redirect:/houses/" + houseId;
	}

	/**
	 * delete:
	 * 指定レビューを削除。
	 */
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable Integer houseId,
			@PathVariable Integer reviewId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes ra) {
		// 所有者チェック
		Review review = reviewService.findReviewById(reviewId);
		User currentUser = userService.findUserByEmail(userDetailsImpl.getUsername());
		if (!review.getUser().getId().equals(currentUser.getId())) {
			ra.addFlashAttribute("errorMessage", "このレビューを削除する権限がありません。");
			return "redirect:/houses/" + houseId;
		}

		reviewService.deleteReview(reviewId);
		ra.addFlashAttribute("message", "レビューを削除しました。");
		return "redirect:/houses/" + houseId;
	}
}
