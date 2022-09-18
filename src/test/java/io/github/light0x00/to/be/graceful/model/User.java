package io.github.light0x00.to.be.graceful.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author light
 * @since 2022/9/18
 */
@AllArgsConstructor
@Data
public class User {
    private Integer userId;
    private String userName;
}
