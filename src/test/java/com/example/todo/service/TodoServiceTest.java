package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    void getAllTodos_returnsList() {
        when(todoRepository.findAll()).thenReturn(List.of(
                new Todo(1L, "Task 1", "Desc", false),
                new Todo(2L, "Task 2", "Desc", true)
        ));

        List<Todo> todos = todoService.getAllTodos();

        assertEquals(2, todos.size());
    }

    @Test
    void getTodoById_whenExists_returnsTodo() {
        Todo todo = new Todo(1L, "Task", "Desc", false);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        Todo result = todoService.getTodoById(1L);

        assertEquals("Task", result.getTitle());
    }

    @Test
    void getTodoById_whenMissing_throwsException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> todoService.getTodoById(99L));
    }

    @Test
    void createTodo_resetsIdAndSaves() {
        Todo input = new Todo(100L, "New Task", "Desc", false);
        Todo saved = new Todo(1L, "New Task", "Desc", false);
        when(todoRepository.save(any(Todo.class))).thenReturn(saved);

        Todo result = todoService.createTodo(input);

        assertNull(input.getId());
        assertEquals(1L, result.getId());
    }

    @Test
    void updateTodo_updatesFieldsAndSaves() {
        Todo existing = new Todo(1L, "Old", "Old Desc", false);
        Todo update = new Todo(null, "New", "New Desc", true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo result = todoService.updateTodo(1L, update);

        assertEquals("New", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        assertTrue(result.isCompleted());
    }

    @Test
    void deleteTodo_whenExists_deletesEntity() {
        Todo existing = new Todo(1L, "Task", "Desc", false);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(existing));

        todoService.deleteTodo(1L);

        verify(todoRepository, times(1)).delete(existing);
    }
}
