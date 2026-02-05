package com.advocacia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "user_screen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScreen {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserScreenId implements Serializable {
        private Long userId;
        private Long screenId;
    }

    @EmbeddedId
    private UserScreenId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("screenId")
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
}
