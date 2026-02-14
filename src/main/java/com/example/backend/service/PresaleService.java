package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.query.presale.PresaleListQuery;
import com.example.backend.query.presale.PresaleSaveQuery;
import com.example.backend.response.presale.PresaleDetailResponse;
import com.example.backend.response.presale.PresaleListItemResponse;

public interface PresaleService {

  PresaleDetailResponse getDetail(Integer id);

  IPage<PresaleListItemResponse> list(PresaleListQuery query);

  void save(PresaleSaveQuery query);

  void remove(Integer id);
}
