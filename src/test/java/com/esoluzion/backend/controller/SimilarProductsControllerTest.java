package com.esoluzion.backend.controller;

import com.esoluzion.backend.model.ProductDetail;
import com.esoluzion.backend.service.SimilarProductsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimilarProductsController.class)
class SimilarProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimilarProductsService similarProductsService;

    @Test
    void shouldReturnSimilarProducts() throws Exception {
        when(similarProductsService.getSimilarProducts("1"))
                .thenReturn(List.of(new ProductDetail("2", "Dress", BigDecimal.valueOf(19.99), true)));

        mockMvc.perform(get("/product/1/similar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Dress"));
    }
}
