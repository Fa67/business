package com.eayun.file.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.file.model.BaseEayunFile;

public interface FileDao extends IRepository<BaseEayunFile, String> {

    @Query("select count(*) from BaseEayunFile bef where bef.fileMD5 = ?")
    public int getMD5count(String MD5);
    
    @Query("from BaseEayunFile bef where bef.fileMD5 = ? ")
    public List<BaseEayunFile> findListByMD5(String md5);
}
