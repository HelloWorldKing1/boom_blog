package com.ra.boomblog.xo.service.impl;

import com.ra.boomblog.commons.entity.CommentReport;
import com.ra.boomblog.xo.mapper.CommentReportMapper;
import com.ra.boomblog.xo.service.CommentReportService;
import com.ra.boomblog.base.serviceImpl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 评论举报表 服务实现类
 *
 * @author 陌溪
 * @date 2020年1月12日15:47:47
 */
@Service
public class CommentReportServiceImpl extends SuperServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {

}
