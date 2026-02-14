package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.entity.Character;
import com.example.backend.enumerate.DubbingVersionEnum;
import com.example.backend.enumerate.MovieReleaseState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.*;
import com.example.backend.query.SaveMovieQuery;
import com.example.backend.utils.MessageUtils;
import com.example.backend.query.CharacterSaveQuery;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
class MovieResponse {
  private  Integer id;
}

@Slf4j
@Service
public class MovieService extends ServiceImpl<MovieMapper, Movie> {
  @Autowired
  private GenericService<Movie> genericService;

  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private MovieSpecMapper movieSpecMapper;
  @Autowired
  MovieStaffMapper movieStaffMapper;

  @Autowired
  MovieCharacterMapper movieCharacterMapper;

  @Autowired
  private MovieStaffService movieStaffService;

  @Autowired
  private  MovieCharacterService movieCharacterService;

  @Autowired
  private MovieVersionService movieVersionService;
  
  @Autowired
  private MovieVersionCharacterMapper movieVersionCharacterMapper;
  
  @Autowired
  private MovieVersionCharacterStaffMapper movieVersionCharacterStaffMapper;

  @Autowired
  private HelloMovieService helloMovieService;

  @Autowired
  private MovieTagTagsService movieTagTagsService;

  @Autowired
  private  MovieTagTagsMapper movieTagTagsMapper;

  @Autowired
  private  HelloMovieMapper helloMovieMapper;

  @Transactional
  public void saveMovie(Movie movie, SaveMovieQuery query) {
    if (query.getId() == null) {
      movieMapper.insert(movie);

    } else  {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      movieMapper.update(movie, updateQueryWrapper);
    }
    int movieId = query.getId() == null ? movie.getId() : query.getId();

    if (query.getSpec() != null) {
      movieSpecMapper.deleteSpec(movieId);

      query.getSpec().forEach(item -> {
        MovieSpec movieSpec = new MovieSpec();
        movieSpec.setSpecId(item);
        movieSpec.setMovieId(movieId);

        movieSpecMapper.insert(movieSpec);
      });
    }

    if (query.getStaffList() != null) {
      movieStaffMapper.deleteStaff(movieId);

      movieStaffService.saveBatch(
        query.getStaffList().stream()
          .map(item -> {
            MovieStaff data = new MovieStaff();
            data.setMovieId(movieId);
            data.setStaffId(item.getStaffId());
            data.setPositionId(item.getPositionId());

            return data;
          })
          .collect(Collectors.toList())
      );
    }
    
    // 保存电影版本信息（新结构）
    if (query.getVersions() != null && !query.getVersions().isEmpty()) {
      query.getVersions().forEach(versionQuery -> {
        MovieVersion movieVersion;
        
        if (versionQuery.getId() != null) {
          // 更新现有版本
          movieVersion = movieVersionService.getById(versionQuery.getId());
          if (movieVersion != null) {
            movieVersion.setVersionCode(versionQuery.getVersionCode());
            movieVersion.setStartDate(versionQuery.getStartDate());
            movieVersion.setEndDate(versionQuery.getEndDate());
            movieVersion.setLanguageId(versionQuery.getLanguageId());
            movieVersionService.updateById(movieVersion);
          }
        } else {
          // 创建新版本
          movieVersion = new MovieVersion();
          movieVersion.setMovieId(movieId);
          movieVersion.setVersionCode(versionQuery.getVersionCode());
          movieVersion.setStartDate(versionQuery.getStartDate());
          movieVersion.setEndDate(versionQuery.getEndDate());
          movieVersion.setLanguageId(versionQuery.getLanguageId());
          movieVersionService.save(movieVersion);
        }
        
        Integer versionId = movieVersion.getId();
        
        // 删除该版本的旧角色关联
        QueryWrapper<MovieVersionCharacter> deleteCharWrapper = new QueryWrapper<>();
        deleteCharWrapper.eq("movie_version_id", versionId);
        movieVersionCharacterMapper.delete(deleteCharWrapper);
        
        // 删除该版本的旧演员关联
        QueryWrapper<MovieVersionCharacterStaff> deleteStaffWrapper = new QueryWrapper<>();
        deleteStaffWrapper.eq("movie_version_id", versionId);
        movieVersionCharacterStaffMapper.delete(deleteStaffWrapper);
        
        // 保存角色和演员关联
        if (versionQuery.getCharacters() != null) {
          versionQuery.getCharacters().forEach(character -> {
            // 保存版本-角色关联
            MovieVersionCharacter mvc = new MovieVersionCharacter();
            mvc.setMovieVersionId(versionId);
            mvc.setCharacterId(character.getId());
            movieVersionCharacterMapper.insert(mvc);
            
            // 保存角色-演员关联
            if (character.getStaffIds() != null) {
              character.getStaffIds().forEach(staffId -> {
                MovieVersionCharacterStaff mvcs = new MovieVersionCharacterStaff();
                mvcs.setMovieVersionId(versionId);
                mvcs.setCharacterId(character.getId());
                mvcs.setStaffId(staffId);
                movieVersionCharacterStaffMapper.insert(mvcs);
              });
            }
          });
        }
      });
    }
    
    // 向后兼容：保存旧的 characterList 到默认版本（如果 versions 为空）
    if ((query.getVersions() == null || query.getVersions().isEmpty()) 
        && query.getCharacterList() != null && !query.getCharacterList().isEmpty()) {
      // 1. 创建或获取默认版本（原版）
      QueryWrapper<MovieVersion> versionWrapper = new QueryWrapper<>();
      versionWrapper.eq("movie_id", movieId);
      versionWrapper.eq("version_code", DubbingVersionEnum.ORIGINAL.getValue()); // 原版
      MovieVersion movieVersion = movieVersionService.getOne(versionWrapper);
      
      if (movieVersion == null) {
        // 创建默认版本
        movieVersion = new MovieVersion();
        movieVersion.setMovieId(movieId);
        movieVersion.setVersionCode(DubbingVersionEnum.ORIGINAL.getValue()); // 原版
        movieVersionService.save(movieVersion);
      }
      
      Integer versionId = movieVersion.getId();
      
      // 2. 删除该版本的旧角色关联
      QueryWrapper<MovieVersionCharacter> deleteWrapper = new QueryWrapper<>();
      deleteWrapper.eq("movie_version_id", versionId);
      movieVersionCharacterMapper.delete(deleteWrapper);
      
      // 3. 批量插入新的角色关联
      query.getCharacterList().forEach(item -> {
        MovieVersionCharacter mvc = new MovieVersionCharacter();
        mvc.setMovieVersionId(versionId);
        mvc.setCharacterId(item.getCharacterId());
        movieVersionCharacterMapper.insert(mvc);
      });
    }
    
    // 保存标签
    if (query.getTags() != null) {
      movieTagTagsMapper.deleteMovieTags(movieId);

      movieTagTagsService.saveBatch(
        query.getTags().stream()
          .map(item -> {
            MovieTagTags data = new MovieTagTags();

            data.setMovieId(movieId);
            data.setMovieTagId(item);

            return data;
          })
          .collect(Collectors.toList())
      );
    }

    if (query.getHelloMovie() != null) {
      helloMovieMapper.deleteHelloMovie(movieId);

      helloMovieService.saveBatch(
        query.getHelloMovie().stream()
          .map(item -> {
            HelloMovie data = new HelloMovie();
            data.setMovieId(movieId);
            data.setCode(item.getCode());
            data.setDate(item.getDate());

            return data;
          })
          .collect(Collectors.toList())
      );
    }

  }
  public RestBean<Object> save(SaveMovieQuery query) {
    Movie movie = new Movie();
    String name = query.getOriginalName() == null ? query.getName() : query.getOriginalName();

    movie.setId(query.getId());
    movie.setStatus(query.getStatus() == null ? 1 : query.getStatus());
    movie.setTime(query.getTime());
    movie.setLevelId(query.getLevelId());
    movie.setOriginalName(query.getOriginalName());
    movie.setCover(query.getCover());
    movie.setName(query.getName());
    movie.setDescription(query.getDescription());
    movie.setStartDate(query.getStartDate());
    movie.setEndDate(query.getEndDate());
    movie.setHomePage(query.getHomePage());

    // 添加的去重查询条件
    QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("name", name);
    if (query.getId() != null) {
      queryWrapper.ne("id", query.getId());
    }

    // 执行去重查询
    int count = Math.toIntExact(movieMapper.selectCount(queryWrapper));

    if (count > 0) {
      return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.REPEAT));
    }

    // 保存电影信息
    saveMovie(movie, query);
    MovieResponse movieResponse = new MovieResponse();
    movieResponse.setId(movie.getId());

    return RestBean.success(movieMapper.movieDetail(movie.getId()), MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
  }

  /**
   * 定时任务：根据 startDate/endDate 更新电影上映状态（上映中、已结束）。
   * 仅处理日期格式为 yyyy-MM-dd 的电影。
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateMovieReleaseState() {
    QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
    queryWrapper.ne("status", MovieReleaseState.ended.getType());
    List<Movie> movieList = list(queryWrapper);
    String regex = "^\\d{4}-\\d{2}-\\d{2}$";
    List<Movie> toUpdate = movieList.stream()
        .filter(item -> (item.getStartDate() != null && Pattern.matches(regex, item.getStartDate()))
            || (item.getEndDate() != null && Pattern.matches(regex, item.getEndDate())))
        .map(item -> {
          LocalDate currentDate = LocalDate.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          try {
            if (item.getStartDate() != null && item.getEndDate() != null) {
              LocalDate startDate = LocalDate.parse(item.getStartDate(), formatter);
              LocalDate endDate = LocalDate.parse(item.getEndDate(), formatter);
              if (!currentDate.isBefore(startDate) && currentDate.isBefore(endDate)) {
                item.setStatus(MovieReleaseState.nowShowing.getType());
              } else if (currentDate.isAfter(endDate)) {
                item.setStatus(MovieReleaseState.ended.getType());
              }
            } else if (item.getStartDate() != null) {
              LocalDate startDate = LocalDate.parse(item.getStartDate(), formatter);
              if (!currentDate.isBefore(startDate)) {
                item.setStatus(MovieReleaseState.nowShowing.getType());
              }
            }
          } catch (Exception e) {
            log.warn("日期解析错误: {}", e.getMessage());
          }
          return item;
        })
        .collect(Collectors.toList());
    if (!toUpdate.isEmpty()) {
      updateBatchById(toUpdate, toUpdate.size());
    }
  }
}
