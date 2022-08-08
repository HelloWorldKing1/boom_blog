package com.ra.boomblog.xo.vo;

import com.ra.boomblog.base.validator.annotion.NotBlank;
import com.ra.boomblog.base.validator.group.Insert;
import com.ra.boomblog.base.validator.group.Update;
import com.ra.boomblog.base.vo.BaseVO;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * 反馈表
 * </p>
 *
 * @author xuzhixiang
 * @since 2020年3月16日08:35:18
 */
@ToString
@Data
public class FeedbackVO extends BaseVO<FeedbackVO> {

    /**
     * 用户uid
     */
    private String userUid;

    /**
     * 标题
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String title;

    /**
     * 反馈的内容
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String content;

    /**
     * 回复
     */
    private String reply;

    /**
     * 反馈状态： 0：已开启  1：进行中  2：已完成  3：已拒绝
     */
    private Integer feedbackStatus;

}
