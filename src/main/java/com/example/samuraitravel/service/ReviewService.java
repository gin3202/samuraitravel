// src/main/java/com/example/samuraitravel/service/ReviewService.java
package com.example.samuraitravel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;
	private final UserRepository userRepository;

	public ReviewService(
			ReviewRepository reviewRepository,
			HouseRepository houseRepository,
			UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.houseRepository = houseRepository;
		this.userRepository = userRepository;
	}

	// --- 1) 指定IDのレビューを取得 ---
	public Review findReviewById(Integer reviewId) {
		return reviewRepository.findById(reviewId)
				.orElseThrow(() -> new EntityNotFoundException("指定されたIDのレビューが存在しません。"));
	}

	// --- 2) 指定民宿の最新レビュー6件を取得 ---
	public List<Review> findTop6ReviewsByHouseOrderByCreatedAtDesc(House house) {
		return reviewRepository.findTop6ByHouseOrderByCreatedAtDesc(house);
	}

	// --- 3) 指定民宿×ユーザーのレビューを取得 ---
	public Review findReviewByHouseAndUser(House house, User user) {
		return reviewRepository.findByHouseAndUser(house, user);
	}

	// --- 4) 指定民宿のレビュー総数を取得 ---
	public long countReviewsByHouse(House house) {
		return reviewRepository.countByHouse(house);
	}

	// --- 5) 指定民宿のレビューを作成日時降順でページング取得 ---
	public Page<Review> findReviewsByHouseOrderByCreatedAtDesc(House house, Pageable pageable) {
		return reviewRepository.findByHouseOrderByCreatedAtDesc(house, pageable);
	}

	// --- 6) フォームから新規レビュー登録 ---
	@Transactional
	public Review createReview(Integer houseId, Integer userId, ReviewRegisterForm form) {
		// 民宿・ユーザーの存在チェック
		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new EntityNotFoundException("指定されたIDの民宿が存在しません。"));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("指定されたIDのユーザーが存在しません。"));

		// 同一ユーザーの重複投稿チェック（ユニーク制約の事前検知）
		if (hasUserAlreadyReviewed(house, user)) {
			throw new IllegalStateException("この民宿には既にレビューを投稿済みです。");
		}

		Review review = new Review();
		review.setHouse(house);
		review.setUser(user);
		review.setScore(form.getScore());
		review.setContent(form.getContent());

		return reviewRepository.save(review);
	}

	// --- 7) フォームから既存レビュー更新 ---
	@Transactional
	public Review updateReview(Integer reviewId, ReviewEditForm form) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new EntityNotFoundException("指定されたIDのレビューが存在しません。"));

		review.setScore(form.getScore());
		review.setContent(form.getContent());

		return reviewRepository.save(review);
	}

	// --- 8) レビュー削除 ---
	@Transactional
	public void deleteReview(Integer reviewId) {
		// 存在チェック（存在しないIDで deleteById を呼ぶと静かに無視される実装もあるため）
		Optional<Review> opt = reviewRepository.findById(reviewId);
		if (opt.isEmpty()) {
			throw new EntityNotFoundException("指定されたIDのレビューが存在しません。");
		}
		reviewRepository.deleteById(reviewId);
	}

	// --- 9) 指定ユーザーが既にその民宿をレビュー済みか判定 ---
	public boolean hasUserAlreadyReviewed(House house, User user) {
		
		return reviewRepository.findByHouseAndUser(house, user) != null;
	}
}
