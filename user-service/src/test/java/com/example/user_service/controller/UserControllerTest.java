package com.example.user_service.controller;

import com.example.user_service.dto.UserRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequest validUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        validUserRequest = new UserRequest();
        validUserRequest.setName("John Doe");
        validUserRequest.setEmail("john@example.com");
        validUserRequest.setAge(30);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setAge(30);
        userResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.age", is(30)));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setAge(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {

        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: 999"));

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        UserResponse user2 = new UserResponse();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");
        user2.setAge(25);
        user2.setCreatedAt(LocalDateTime.now());

        List<UserResponse> users = Arrays.asList(userResponse, user2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane Doe")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("John Updated");
        updatedResponse.setEmail("john.updated@example.com");
        updatedResponse.setAge(35);
        updatedResponse.setCreatedAt(LocalDateTime.now());

        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(updatedResponse);


        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")))
                .andExpect(jsonPath("$.age", is(35)));

        verify(userService, times(1)).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("User not found with id: 999"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: 999"));

        verify(userService, times(1)).deleteUser(999L);
    }
}