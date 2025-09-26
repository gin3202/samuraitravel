package com.example.samuraitravel.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	/** 民宿との紐付け */
	@ManyToOne
	@JoinColumn(name = "house_id")
	private House house;

	/** 会員との紐付け */
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	/** コメント */
	@Column(name = "content")
	private String content;

	/** 評価スコア */
	@Column(name = "score")
	private Integer score;

	/** 投稿日時（DBで自動設定） */
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;

	/** 更新日時（DBで自動更新） */
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
}
