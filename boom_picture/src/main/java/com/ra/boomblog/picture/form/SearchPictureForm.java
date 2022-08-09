package com.ra.boomblog.picture.form;

import com.ra.boomblog.base.vo.FileVO;
import lombok.Data;

@Data
public class SearchPictureForm extends FileVO {
    private String searchKey;
    private Integer count;
}
