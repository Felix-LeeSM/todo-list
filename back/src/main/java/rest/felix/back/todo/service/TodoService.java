package rest.felix.back.todo.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rest.felix.back.todo.dto.CreateTodoDTO;
import rest.felix.back.todo.dto.TodoDTO;
import rest.felix.back.todo.dto.UpdateTodoDTO;
import rest.felix.back.common.exception.throwable.notfound.ResourceNotFoundException;
import rest.felix.back.todo.repository.TodoRepository;

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
