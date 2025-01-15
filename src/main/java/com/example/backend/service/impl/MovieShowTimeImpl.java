package com.example.backend.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.query.MovieShowTimeQuery;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
public class MovieShowTimeImpl implements MovieShowTimeService {

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  public List<MovieShowTime> getSortedMovieShowTimes(MovieShowTimeQuery query, String format) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());
    wrapper.eq("theater_hall_id", query.getTheaterHallId());

    // 如果是编辑的时候，则不考虑当前区间
    if (query.getId() != null) {
      wrapper.ne("id", query.getId());
    }

    List<MovieShowTime> list = movieShowTimeMapper.selectList(wrapper);
    list.sort((o1, o2) -> {
      try {
        Date o1StartTimestamp = Utils.getTimestamp(o1.getStartTime(), format);
        Date o2StartTimestamp = Utils.getTimestamp(o2.getStartTime(), format);
        return Long.compare(o1StartTimestamp.getTime(), o2StartTimestamp.getTime());
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    });
    return list;
  }

  public void saveMovieShowTimeIfNotExists(MovieShowTimeQuery query, String format) throws ParseException {
    MovieShowTime movieShowTime = new MovieShowTime();

    movieShowTime.setCinemaId(query.getCinemaId());
    movieShowTime.setTheaterHallId(query.getTheaterHallId());
    movieShowTime.setMovieId(query.getMovieId());
    movieShowTime.setStartTime(query.getStartTime());
    movieShowTime.setEndTime(query.getEndTime());
    movieShowTime.setOpen(query.getOpen());
    movieShowTime.setSpecId(query.getSpecId());
    movieShowTime.setSubtitleId(query.getSubtitleId());
    movieShowTime.setShowTimeTagId(query.getShowTimeTagId());
//    movieShowTime.set

    if (query.getShowTimeTagId() != null) {
//      movieShowTime.setShowTimeTagId( query.getShowTimeTagId());
    }
    if (query.getSubtitleId() != null) {
//      movieShowTime.setSubtitleId(query.getSubtitleId());
    }

    if (query.getId() == null) {
      movieShowTimeMapper.insert(movieShowTime);
      System.out.println("可以插入");
    } else {
      query.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      movieShowTimeMapper.update(movieShowTime, updateQueryWrapper);
    }
  }

  public boolean check(List<MovieShowTime> list, String format, MovieShowTimeQuery query) {
    try {
      // 获取传入的开始和结束时间
      Date queryStartTime = Utils.getTimestamp(query.getStartTime(), format);
      Date queryEndTime = Utils.getTimestamp(query.getEndTime(), format);

      // 遍历已有的时间区间，检查是否有重叠
      for (int i = 0; i < list.size(); i++) {
        MovieShowTime item = list.get(i);
        Date startTime = Utils.getTimestamp(item.getStartTime(), format);
        Date endTime = Utils.getTimestamp(item.getEndTime(), format);

        // 边界检查，避免访问越界
        Date nextStartTime = (i + 1 < list.size()) ? Utils.getTimestamp(list.get(i + 1).getStartTime(), format) : null;

        if (nextStartTime != null) {
          System.out.println("下个时间为：" + Utils.format(nextStartTime, format));
        }

        // 检查当前时间区间与传入时间区间是否有重叠
        if (!endTime.before(queryStartTime) && !startTime.after(queryEndTime)) {
          return false; // 时间区间有重叠，返回false
        }

        // 如果当前时间区间的结束时间在传入时间区间的开始时间之前，且
        // 传入时间区间的结束时间在下一个时间区间的开始时间之前，则可以插入
        if (nextStartTime == null || (!endTime.after(queryStartTime) && !nextStartTime.before(queryEndTime))) {
          saveMovieShowTimeIfNotExists(query, format);
          return true;
        }
      }

      // 如果待插入的时间区间的开始时间在所有时间区间的结束时间之后，可以直接插入到最后一个时间区间之后
      saveMovieShowTimeIfNotExists(query, format);
      return true;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

//  public  boolean check (List<MovieShowTime> list, String format, MovieShowTimeQuery query) {
//    try {
//      // 遍历已有的时间区间，检查是否有重叠
//      for (int i = 0; i < list.size(); i++) {
//        MovieShowTime item = list.get(i);
//        Date queryStartTime = Utils.getTimestamp(query.getStartTime(), format);
//        Date queryEndTime = Utils.getTimestamp(query.getEndTime(), format);
//        Date startTime = Utils.getTimestamp(item.getStartTime(), format);
//        Date endTime = Utils.getTimestamp(item.getEndTime(), format);
//        if (list.get(i + 1) != null) {
//          Date nextStartTime = Utils.getTimestamp(list.get(i + 1).getStartTime(), format);
//          System.out.println("i =======" + i);
//          System.out.println("传参区间为: " + query.getStartTime() + "===" + query.getEndTime());
//          System.out.println("遍历区间为: " + Utils.format(startTime, format) + "===" + Utils.format(endTime, format));
//          System.out.println("下个时间为：" + Utils.format(nextStartTime, format));
//
//          Boolean has = !endTime.after(queryStartTime) && (i == list.size() - 1 || !nextStartTime.before(queryEndTime));
//          // 如果新时间区间的开始时间在当前时间区间的结束时间之后，且新时间区间的结束时间在下一个时间区间的开始时间之前，
//          // 则可以将新时间区间插入到当前时间区间之后
//          if (has) {
//            saveMovieShowTimeIfNotExists(query, format);
//            return true;
//          }
//          // 如果新时间小于数据任意时间
//          if (!endTime.before(queryEndTime) && !queryEndTime.before(queryStartTime)) {
//            saveMovieShowTimeIfNotExists(query, format);
//            return true;
//          }
//          // 如果新时间区间与当前时间区间有重叠，则直接返回时间冲突的错误信息
//          if (!endTime.before(queryStartTime) && !startTime.after(queryEndTime)) {
//            return false;
//          }
//        }
//
//      }
//      // 如果待插入的时间区间的开始时间在所有时间区间的结束时间之后，可以直接插入到最后一个时间区间之后
//      saveMovieShowTimeIfNotExists(query, format);
//      return  true;
//    } catch (ParseException e) {
//      throw new RuntimeException(e);
//    }
//  }
}