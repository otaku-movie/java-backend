package com.example.backend;

import com.example.backend.entity.Cinema;
import com.example.backend.entity.Movie;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.MovieMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@Resource
	private CinemaMapper cinemaMapper;
	@Test
	public void test() {
		Cinema cinema = new Cinema();
		cinema.setName("11");
		cinema.setAddress("1");
		cinema.setTel("1");
		cinemaMapper.insert(cinema);
	}

}
