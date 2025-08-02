package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.Evaluation;

import java.util.List;

public interface EvaluationService {
    // 获取所有评价
    List<Evaluation> getAllEvaluations();

    // 获取评价详情
    Evaluation getEvaluationById(int id);

    // 添加评价
    int addEvaluation(Evaluation evaluation);

    // 更新评价信息
    int updateEvaluation(Evaluation evaluation);

    // 删除评价
    int deleteEvaluation(int id);
}
