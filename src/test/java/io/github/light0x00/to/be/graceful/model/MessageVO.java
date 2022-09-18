package io.github.light0x00.to.be.graceful.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author light
 * @since 2022/9/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    private Integer userId;
    private String userName;
    private String groupName;
    private String content;
}
