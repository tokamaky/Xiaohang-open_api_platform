package com.xiaohang.project.model.vo;

import com.xiaohang.project.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Interface Information Encapsulation View
 *
 * @author
 * <a href="https://github.com/lixiaohang">Programmer Yupi</a>
 * @from
 * <a href="https://xiaohang.icu">Programming Navigation Knowledge Planet</a>
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * Number of Calls
     */
    private Integer totalNum;

    private static final long serialVersionUID = 1L;
}
