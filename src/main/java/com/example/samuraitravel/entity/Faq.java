package com.example.samuraitravel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faqs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String question;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String answer;

    /**
     * 作成日時はDBのデフォルト（CURRENT_TIMESTAMP）で自動セット
     * アプリ側からは値を入れないため insertable=false / updatable=false
     */
    @Column(name = "created_at",
            insertable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    /**
     * 更新日時はDBで自動更新（ON UPDATE CURRENT_TIMESTAMP）
     */
    @Column(name = "updated_at",
            insertable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;
}
