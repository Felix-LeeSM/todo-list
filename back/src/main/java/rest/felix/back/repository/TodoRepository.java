package rest.felix.back.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.dto.internal.UpdateTodoDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.exception.throwable.notfound.ResourceNotFoundException;

@Repository
@AllArgsConstructor
public class TodoRepository {

  private final EntityManager em;

  public List<TodoDTO> getTodosInGroup(long groupId) {
    return em.createQuery("""
            SELECT
                t
            FROM
                Group g
            JOIN
                g.todos t
            JOIN FETCH
                t.author
            WHERE
                g.id = :groupId
            """, Todo.class)
        .setParameter("groupId", groupId)
        .getResultList()
        .stream()
        .map(todo -> new TodoDTO(todo.getId(), todo.getTitle(), todo.getDescription(),
            todo.getTodoStatus(), todo.getAuthor().getId(), groupId))
        .toList();
  }

  public Optional<TodoDTO> getTodoInGroup(long groupId, long todoId) {
    return em.createQuery("""
            SELECT
                t
            FROM
                Group g
            JOIN
                g.todos t
            WHERE
                g.id = :groupId AND
                t.id = :todoId
            """, Todo.class)
        .setParameter("groupId", groupId)
        .setParameter("todoId", todoId)
        .getResultList()
        .stream()
        .findFirst()
        .map(todo -> new TodoDTO(todo.getId(), todo.getTitle(), todo.getDescription(),
            todo.getTodoStatus(), todo.getAuthor().getId(), groupId));

  }

  public TodoDTO createTodo(CreateTodoDTO createTodoDTO) {
    try {
      Todo todo = new Todo();

      User author = em.getReference(User.class, createTodoDTO.getAuthorId());
      Group group = em.getReference(Group.class, createTodoDTO.getGroupId());

      todo.setAuthor(author);
      todo.setGroup(group);
      todo.setTitle(createTodoDTO.getTitle());
      todo.setDescription(createTodoDTO.getDescription());

      em.persist(todo);

      return new TodoDTO(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getTodoStatus(),
          todo.getAuthor().getId(), todo.getGroup().getId());
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }

  public void deleteTodo(long todoId) {
    em.createQuery("""
            DELETE
            FROM Todo t
            WHERE t.id = :todoId
            """)
        .setParameter("todoId", todoId)
        .executeUpdate();

  }

  public TodoDTO updateTodo(UpdateTodoDTO updateTodoDTO) {
    return em.createQuery("""
            SELECT
                t
            FROM
                Todo t
            WHERE
                t.id = :todoId
            """, Todo.class)
        .setParameter("todoId", updateTodoDTO.getId())
        .getResultList()
        .stream()
        .findFirst()
        .map(todo -> {
          todo.setTodoStatus(updateTodoDTO.getStatus());
          todo.setDescription(updateTodoDTO.getDescription());
          todo.setTitle(updateTodoDTO.getTitle());
          return todo;
        })
        .map(todo -> new TodoDTO(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getTodoStatus(),
            todo.getAuthor().getId(),
            todo.getGroup().getId()))
        .orElseThrow(ResourceNotFoundException::new);

  }

  public void deleteByGroupId(long groupId) {
    em.createQuery("""
            DELETE
            FROM
              Todo t
            WHERE
              t.group.id =:groupId
            """)
        .setParameter("groupId", groupId)
        .executeUpdate();
  }
}
