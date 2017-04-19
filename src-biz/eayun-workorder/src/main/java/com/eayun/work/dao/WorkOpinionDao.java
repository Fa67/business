package com.eayun.work.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.work.model.BaseWorkOpinion;
import com.eayun.work.model.WorkOpinion;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkOpinionDao extends IRepository<BaseWorkOpinion, String> {

    @Query("select o from  BaseWorkOpinion o where o.workId = ?1 order by opinionTime desc ")
    public List<BaseWorkOpinion> findByCreDateDesc(String workId) throws AppException ;

}
