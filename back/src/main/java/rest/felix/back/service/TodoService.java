package rest.felix.back.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Service;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.dto.internal.UpdateTodoDTO;
import rest.felix.back.exception.throwable.notfound.ResourceNotFoundException;
import rest.felix.back.repository.TodoRepository;

@Service
@Transactional
@AllArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  public List<TodoDTO> getTodosInGroup(long groupId) {

    return todoRepository.getTodosInGroup(groupId);
  }

  public TodoDTO getTodoInGroup(long groupId, long todoId) {

    return todoRepository.getTodoInGroup(groupId, todoId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public TodoDTO createTodo(CreateTodoDTO createTodoDTO) {

    return todoRepository.createTodo(createTodoDTO);
  }

  public void deleteTodo(long todoId) {

    todoRepository.deleteTodo(todoId);
  }

  public TodoDTO updateTodo(UpdateTodoDTO updateTodoDTO) {

    return todoRepository.updateTodo(updateTodoDTO);
  }
}
