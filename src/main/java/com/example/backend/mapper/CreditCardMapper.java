package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.CreditCard;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardMapper extends BaseMapper<CreditCard> {

    /**
     * 根据用户ID查询信用卡列表
     */
    @Select("SELECT * FROM credit_cards WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, create_time DESC")
    List<CreditCard> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID和信用卡ID查询信用卡
     */
    @Select("SELECT * FROM credit_cards WHERE id = #{cardId} AND user_id = #{userId} AND deleted = 0")
    CreditCard selectByIdAndUserId(@Param("cardId") Integer cardId, @Param("userId") Integer userId);

    /**
     * 取消用户的默认信用卡设置
     */
    @Select("UPDATE credit_cards SET is_default = 0 WHERE user_id = #{userId} AND deleted = 0")
    void clearDefaultCard(@Param("userId") Integer userId);
}
