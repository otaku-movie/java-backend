package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.CinemaPriceConfig;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.CinemaSpecSpecMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 票价计算服务
 * 票价 = 票种基础价(2D) + 3D加价(若有) + 规格加价(IMAX/Dolby等)
 */
@Service
@RequiredArgsConstructor
public class TicketPriceService {

    private final MovieTicketTypeMapper movieTicketTypeMapper;
    private final CinemaPriceConfigMapper cinemaPriceConfigMapper;
    private final CinemaSpecSpecMapper cinemaSpecSpecMapper;

    /**
     * 计算票价
     *
     * @param cinemaId              影院ID
     * @param movieTicketTypeId     票种ID（成人/儿童等）
     * @param dimensionType         放映类型 dict_item.code，1=2D 2=3D
     * @param specIds               规格ID列表（IMAX/Dolby等），可为空，多个规格加价累加
     * @return 最终票价
     */
    public BigDecimal calculatePrice(Integer cinemaId, Integer movieTicketTypeId,
                                     Integer dimensionType, List<Integer> specIds) {
        MovieTicketType ticketType = movieTicketTypeMapper.selectById(movieTicketTypeId);
        if (ticketType == null || ticketType.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = ticketType.getPrice();

        // 3D 加价
        if (dimensionType != null) {
            BigDecimal surcharge = cinemaPriceConfigMapper.getSurcharge(cinemaId, dimensionType);
            if (surcharge != null) {
                total = total.add(surcharge);
            }
        }

        // 规格加价（IMAX、Dolby 等），多个规格累加
        if (specIds != null && !specIds.isEmpty()) {
            for (Integer specId : specIds) {
                if (specId == null) continue;
                QueryWrapper<CinemaSpecSpec> qw = new QueryWrapper<>();
                qw.eq("cinema_id", cinemaId).eq("spec_id", specId).last("LIMIT 1");
                CinemaSpecSpec specSpec = cinemaSpecSpecMapper.selectOne(qw);
                if (specSpec != null && specSpec.getPlusPrice() != null) {
                    total = total.add(BigDecimal.valueOf(specSpec.getPlusPrice()));
                }
            }
        }

        return total;
    }
}
