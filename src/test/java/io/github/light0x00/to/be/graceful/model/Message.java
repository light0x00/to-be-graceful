package io.github.light0x00.to.be.graceful.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author light
 * @since 2022/9/18
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class Message {
    private Integer groupId;
    private Integer userId;
    private String content;
}
