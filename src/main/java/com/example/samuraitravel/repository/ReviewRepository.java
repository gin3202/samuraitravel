// src/main/java/com/example/samuraitravel/repository/ReviewRepository.java
package com.example.samuraitravel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

	// 特定の民宿に対する最新のレビュー6件を取得（作成日時の降順）
	List<Review> findTop6ByHouseOrderByCreatedAtDesc(House house);

	// 特定の民宿とユーザーに紐づくレビューを1件取得
	Review findByHouseAndUser(House house, User user);

	// 特定の民宿に対するレビュー総数をカウント
	long countByHouse(House house);

	// 特定の民宿に関するレビューを作成日時の降順でページング取得
	Page<Review> findByHouseOrderByCreatedAtDesc(House house, Pageable pageable);
}
