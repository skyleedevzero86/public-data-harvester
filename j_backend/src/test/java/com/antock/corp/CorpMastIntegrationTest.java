package com.antock.corp;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.show-sql=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
@EnableJpaRepositories(basePackages = "com.antock.api.coseller.infrastructure")
@DisplayName("법인 정보 검색 통합 테스트")
class CorpMastIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private CorpMastRepository corpMastRepository;

        @BeforeEach
        void setUp() {
                corpMastRepository.deleteAll();

                CorpMast corp1 = CorpMast.builder()
                                .sellerId("2025-서울강남-01714")
                                .bizNm("주식회사 뮤직턴")
                                .bizNo("140-81-99474")
                                .corpRegNo("1101110918053")
                                .regionCd("1168010100")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .username("admin")
                                .build();

                CorpMast corp2 = CorpMast.builder()
                                .sellerId("2025-서울강남-01726")
                                .bizNm("주식회사 뷰타민")
                                .bizNo("510-86-03231")
                                .corpRegNo("1101110932733")
                                .regionCd("1168010100")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .username("admin")
                                .build();

                CorpMast corp3 = CorpMast.builder()
                                .sellerId("2025-부산해운-01001")
                                .bizNm("부산물류주식회사")
                                .bizNo("220-88-12345")
                                .corpRegNo("2101110111111")
                                .regionCd("2600010100")
                                .siNm("부산광역시")
                                .sggNm("해운대구")
                                .username("manager")
                                .build();

                corpMastRepository.save(corp1);
                corpMastRepository.save(corp2);
                corpMastRepository.save(corp3);
        }

        @Test
        @DisplayName("웹 - 법인 검색 페이지 전체 플로우")
        void webSearchFlow_ShouldWorkEndToEnd() throws Exception {
                mockMvc.perform(get("/corp/search"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("corp/search"))
                                .andExpect(model().attributeExists("cities"));

                mockMvc.perform(get("/corp/search").param("bizNm", "뮤직턴"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("corp/search"))
                                .andExpect(model().attributeExists("corpList"))
                                .andExpect(model().attributeExists("statistics"));

                mockMvc.perform(get("/corp/search").param("city", "서울특별시").param("district", "강남구"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("corp/search"));

                mockMvc.perform(get("/corp/search").param("bizNm", "주식회사").param("page", "0").param("size", "1"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("corp/search"));
        }

        @Test
        @DisplayName("웹 - 법인 상세 페이지 플로우")
        void webDetailFlow_ShouldWorkEndToEnd() throws Exception {
                CorpMast savedCorp = corpMastRepository.findByBizNo("140-81-99474").orElseThrow();

                mockMvc.perform(get("/corp/detail/{id}", savedCorp.getId()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("corp/detail"))
                                .andExpect(model().attributeExists("corp"));
        }

        @Test
        @DisplayName("웹 - AJAX 구/군 조회")
        void webAjaxDistrictsFlow_ShouldWork() throws Exception {
                mockMvc.perform(get("/corp/districts/{city}", "서울특별시"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("API - 법인 검색 전체 플로우")
        void apiSearchFlow_ShouldWorkEndToEnd() throws Exception {
                mockMvc.perform(get("/api/v1/corp/search").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.totalElements").value(3));

                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "뮤직턴")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalElements").value(1))
                                .andExpect(jsonPath("$.data.content[0].bizNm").value("주식회사 뮤직턴"));

                mockMvc.perform(get("/api/v1/corp/search").param("city", "서울특별시").param("district", "강남구")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalElements").value(2));

                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "주식회사").param("city", "서울특별시")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("API - 법인 상세 조회 플로우")
        void apiDetailFlow_ShouldWorkEndToEnd() throws Exception {
                CorpMast savedCorp = corpMastRepository.findByBizNo("140-81-99474").orElseThrow();

                mockMvc.perform(get("/api/v1/corp/{id}", savedCorp.getId()).contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.bizNm").value("주식회사 뮤직턴"));

                mockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", "140-81-99474")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.bizNo").value("140-81-99474"));

                mockMvc.perform(get("/api/v1/corp/regno/{corpRegNo}", "1101110918053")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.corpRegNo").value("1101110918053"));
        }

        @Test
        @DisplayName("API - 메타데이터 조회 플로우")
        void apiMetadataFlow_ShouldWorkEndToEnd() throws Exception {
                mockMvc.perform(get("/api/v1/corp/cities").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[?(@=='서울특별시')]").exists())
                                .andExpect(jsonPath("$.data[?(@=='부산광역시')]").exists());

                mockMvc.perform(get("/api/v1/corp/districts/{city}", "서울특별시").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[?(@=='강남구')]").exists());

                mockMvc.perform(get("/api/v1/corp/statistics").param("bizNm", "주식회사")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalCount").exists());
        }

        @Test
        @DisplayName("데이터베이스 검색 쿼리 성능 테스트")
        void databaseSearchPerformance_ShouldBeEfficient() throws Exception {
                long startTime = System.currentTimeMillis();

                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "주식회사").param("city", "서울특별시")
                                .param("page", "0").param("size", "100").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                assert executionTime < 500 : "검색 쿼리 실행 시간이 너무 깁니다: " + executionTime + "ms";
        }

        @Test
        @DisplayName("페이징 경계값 테스트")
        void pagingBoundaryTest_ShouldHandleCorrectly() throws Exception {
                mockMvc.perform(get("/api/v1/corp/search").param("page", "0").param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content.length()").value(2))
                                .andExpect(jsonPath("$.data.first").value(true));

                mockMvc.perform(get("/api/v1/corp/search").param("page", "1").param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content.length()").value(1))
                                .andExpect(jsonPath("$.data.last").value(true));

                mockMvc.perform(get("/api/v1/corp/search").param("page", "10").param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        @DisplayName("동시성 테스트 - 동일한 검색 요청")
        void concurrentSearchTest_ShouldHandleMultipleRequests() throws Exception {
                Thread[] threads = new Thread[5];
                Exception[] exceptions = new Exception[5];

                for (int i = 0; i < 5; i++) {
                        final int threadIndex = i;
                        threads[i] = new Thread(() -> {
                                try {
                                        mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "주식회사")
                                                        .contentType(MediaType.APPLICATION_JSON))
                                                        .andExpect(status().isOk())
                                                        .andExpect(jsonPath("$.success").value(true));
                                } catch (Exception e) {
                                        exceptions[threadIndex] = e;
                                }
                        });
                }

                for (Thread thread : threads)
                        thread.start();
                for (Thread thread : threads)
                        thread.join();
                for (int i = 0; i < 5; i++) {
                        if (exceptions[i] != null)
                                throw new AssertionError("Thread " + i + " failed", exceptions[i]);
                }
        }

        @Test
        @DisplayName("특수문자 검색 테스트")
        void specialCharacterSearchTest_ShouldHandleCorrectly() throws Exception {
                corpMastRepository.save(CorpMast.builder()
                                .sellerId("2025-서울강남-특수")
                                .bizNm("(주)테스트&개발")
                                .bizNo("999-99-99999")
                                .corpRegNo("9999999999999")
                                .regionCd("1168010100")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .username("admin")
                                .build());

                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "(주)")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "&").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("대소문자 구분 없는 검색 테스트")
        void caseInsensitiveSearchTest_ShouldWork() throws Exception {
                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "music")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "MUSIC")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
                mockMvc.perform(get("/api/v1/corp/search").param("bizNm", "Music")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("사업자번호 형식 변환 테스트")
        void bizNoFormatTest_ShouldHandleDifferentFormats() throws Exception {
                mockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", "140-81-99474")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", "14081999474")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/corp/search").param("bizNo", "140-81-99474")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/corp/search").param("bizNo", "14081999474")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("정렬 기능 테스트")
        void sortingTest_ShouldWorkCorrectly() throws Exception {
                mockMvc.perform(get("/api/v1/corp/search").param("sort", "id,desc")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/corp/search").param("sort", "bizNm,asc")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("에러 시나리오 테스트")
        void errorScenarioTest_ShouldHandleGracefully() throws Exception {
                mockMvc.perform(get("/api/v1/corp/{id}", 99999L).contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
                mockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", "000-00-00000")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
                mockMvc.perform(get("/api/v1/corp/{id}", "invalid-id").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }
}