package rest.felix.back.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.repository.TodoRepository;

import java.util.List;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoDTO> getTodosInGroup(long groupId) {

        return todoRepository.getTodosInGroup(groupId);
    }

    public TodoDTO createTodo(CreateTodoDTO createTodoDTO) {

        return todoRepository.createTodo(createTodoDTO);

    }
}
