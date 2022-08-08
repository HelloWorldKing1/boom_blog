package com.ra.boomblog.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ra.boomblog.commons.entity.SysLog;
import com.ra.boomblog.xo.vo.SysLogVO;
import com.ra.boomblog.base.service.SuperService;

/**
 * 操作日志 服务类
 *
 * @author limbo
 * @date 2018-09-30
 */
public interface SysLogService extends SuperService<SysLog> {

    /**
     * 获取操作日志列表
     *
     * @param sysLogVO
     * @return
     */
    public IPage<SysLog> getPageList(SysLogVO sysLogVO);
}
