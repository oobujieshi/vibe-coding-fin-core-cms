package com.fincore.common.base.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permCode;
    private String permName;
    private Long parentId;
    private Integer permType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
